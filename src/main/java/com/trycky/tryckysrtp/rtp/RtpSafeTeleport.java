package com.trycky.tryckysrtp.rtp;

import com.trycky.tryckysrtp.RtpConfig;
import com.trycky.tryckysrtp.RtpLogger;
import com.trycky.tryckysrtp.TryckysRTP;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Safe RTP destination search.
 * W06: bypassUnsafe => relax some constraints, still guarantees "standing space" (2 air blocks).
 */
public final class RtpSafeTeleport {
    private RtpSafeTeleport() {}

    public static final class Result {
        public final boolean success;
        public final BlockPos pos;
        public final String errorMessage;

        private Result(boolean success, BlockPos pos, String errorMessage) {
            this.success = success;
            this.pos = pos;
            this.errorMessage = errorMessage;
        }

        public static Result ok(BlockPos pos) { return new Result(true, pos, null); }
        public static Result fail(String msg) { return new Result(false, null, msg); }
    }

    private static final Set<Block> DANGEROUS_GROUND = new HashSet<>(Set.of(
            Blocks.MAGMA_BLOCK,
            Blocks.CACTUS,
            Blocks.SOUL_CAMPFIRE,
            Blocks.CAMPFIRE,
            Blocks.FIRE,
            Blocks.SOUL_FIRE,
            Blocks.SWEET_BERRY_BUSH,
            Blocks.WITHER_ROSE
    ));

    private static final ResourceLocation NETHER_ID = ResourceLocation.parse("minecraft:the_nether");
    private static final ResourceLocation END_ID = ResourceLocation.parse("minecraft:the_end");

    public static boolean isRtpAllowedInDimension(ServerLevel level) {
        final ResourceKey<Level> dimKey = level.dimension();
        final ResourceLocation dimId = dimKey.location();

        if (!RtpConfig.ALLOW_IN_NETHER.get() && dimId.equals(NETHER_ID)) return false;
        if (!RtpConfig.ALLOW_IN_END.get() && dimId.equals(END_ID)) return false;

        final RtpConfig.DimensionMode mode = RtpConfig.DIMENSION_MODE.get();
        final List<? extends String> allowed = RtpConfig.ALLOWED_DIMENSIONS.get();
        final List<? extends String> blocked = RtpConfig.BLOCKED_DIMENSIONS.get();

        final String id = dimId.toString();
        if (mode == RtpConfig.DimensionMode.ALLOWLIST) return allowed.contains(id);
        return !blocked.contains(id);
    }

    public static Result findSafeDestination(ServerLevel level, ServerPlayer player, boolean bypassUnsafe) {
        if (!isRtpAllowedInDimension(level)) return Result.fail("You cannot use /rtp in this dimension.");

        final int attemptsMax = RtpConfig.ATTEMPTS_MAX.get();
        final int minDistFromSpawn = RtpConfig.MIN_DISTANCE_FROM_SPAWN.get();

        int minR = Math.max(0, RtpConfig.RADIUS_MIN.get());
        int maxR = Math.max(0, RtpConfig.RADIUS_MAX.get());
        if (maxR < minR) { int t = maxR; maxR = minR; minR = t; }

        final BlockPos center = level.getSharedSpawnPos();
        final RandomSource rng = level.getRandom();

        final int yMin = level.getMinBuildHeight() + 1;
        final int yMax = level.getMaxBuildHeight() - 2;
        final int targetY = Mth.clamp(player.blockPosition().getY(), yMin, yMax);

        for (int attempt = 1; attempt <= attemptsMax; attempt++) {
            final BlockPos candidateXZ = pickRandomXZInAnnulus(rng, center, minR, maxR, minDistFromSpawn);
            final BlockPos safe = resolveBestSafeSpotInColumn(level, candidateXZ, targetY, bypassUnsafe);
            if (safe != null) return Result.ok(safe);
        }

        return Result.fail("No safe spot found. Try again later or increase attempts/radius.");
    }

    private static BlockPos pickRandomXZInAnnulus(RandomSource rng, BlockPos center, int minR, int maxR, int minDistanceFromCenter) {
        if (maxR <= 0) return new BlockPos(center.getX(), 0, center.getZ());

        final double theta = rng.nextDouble() * (Math.PI * 2.0);

        final double r0 = Math.max(0, minR);
        final double R = Math.max(r0, maxR);
        final double u = rng.nextDouble();
        final double r = Math.sqrt(u * (R * R - r0 * r0) + r0 * r0);

        int dx = (int) Math.round(Math.cos(theta) * r);
        int dz = (int) Math.round(Math.sin(theta) * r);

        int x = center.getX() + dx;
        int z = center.getZ() + dz;

        if (minDistanceFromCenter > 0) {
            final int cdX = x - center.getX();
            final int cdZ = z - center.getZ();
            final double dist = Math.sqrt((double) cdX * cdX + (double) cdZ * cdZ);
            if (dist < minDistanceFromCenter) {
                final double scale = (minDistanceFromCenter / Math.max(1.0, dist));
                x = center.getX() + (int) Math.round(cdX * scale);
                z = center.getZ() + (int) Math.round(cdZ * scale);
            }
        }

        return new BlockPos(x, 0, z);
    }

    private static BlockPos resolveBestSafeSpotInColumn(ServerLevel level, BlockPos xz, int targetY, boolean bypassUnsafe) {
        final ChunkPos cp = new ChunkPos(xz);
        level.getChunk(cp.x, cp.z);

        final int yMin = level.getMinBuildHeight() + 1;
        final int yMax = level.getMaxBuildHeight() - 2;

        int startY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, xz.getX(), xz.getZ());
        startY = Mth.clamp(startY + 16, yMin, yMax);

        BlockPos best = null;
        int bestScore = Integer.MAX_VALUE;

        for (int yy = startY; yy >= yMin; yy--) {
            final BlockPos feet = new BlockPos(xz.getX(), yy, xz.getZ());
            if (!isValidFeetPosition(level, feet, bypassUnsafe)) continue;

            if (!bypassUnsafe) {
                if (RtpConfig.SURFACE_ONLY_IN_SKYLIGHT_DIMS.get() && level.dimensionType().hasSkyLight()) {
                    if (!level.canSeeSky(feet)) continue;
                }
                if (level.dimensionType().hasCeiling()) {
                    int maxClear = RtpConfig.MAX_CEILING_CLEARANCE.get();
                    if (maxClear > 0) {
                        int clearance = ceilingClearance(level, feet, maxClear + 1);
                        if (clearance > maxClear) continue;
                    }
                }
            }

            final int score = Math.abs(yy - targetY);
            if (score < bestScore) {
                bestScore = score;
                best = feet;
                if (bestScore == 0) break;
            }
        }

        return best;
    }

    private static int ceilingClearance(ServerLevel level, BlockPos feet, int maxScan) {
        for (int i = 1; i <= maxScan; i++) {
            BlockPos p = feet.above(i);
            if (p.getY() >= level.getMaxBuildHeight()) return maxScan;
            BlockState s = level.getBlockState(p);
            if (!s.getCollisionShape(level, p).isEmpty()) return i;
        }
        return maxScan;
    }

    private static boolean isValidFeetPosition(ServerLevel level, BlockPos feet, boolean bypassUnsafe) {
        if (!isAirLike(level, feet)) return false;
        if (!isAirLike(level, feet.above())) return false;

        final BlockPos groundPos = feet.below();
        final BlockState groundState = level.getBlockState(groundPos);

        // Even in bypassUnsafe we still want something sensible; we only relax optional checks.
        if (!bypassUnsafe && RtpConfig.REQUIRE_SOLID_GROUND.get()) {
            if (!groundState.isFaceSturdy(level, groundPos, Direction.UP)) return false;
        }

        if (!bypassUnsafe) {
            if (groundState.is(BlockTags.LEAVES)) return false;
            if (DANGEROUS_GROUND.contains(groundState.getBlock())) return false;
            if (groundState.getBlock() instanceof CampfireBlock) return false;

            if (RtpConfig.AVOID_LIQUIDS.get()) {
                if (isLiquidAt(level, feet) || isLiquidAt(level, feet.above()) || isLiquidAt(level, groundPos)) return false;
            }
        }

        return true;
    }

    private static boolean isAirLike(ServerLevel level, BlockPos pos) {
        final BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).isEmpty();
    }

    private static boolean isLiquidAt(ServerLevel level, BlockPos pos) {
        final FluidState fluid = level.getFluidState(pos);
        return !fluid.isEmpty();
    }

    public static void teleportPlayer(ServerPlayer player, ServerLevel level, BlockPos feet) {
        final ChunkPos cp = new ChunkPos(feet);
        level.getChunk(cp.x, cp.z);

        final boolean keepRot = RtpConfig.KEEP_YAW_PITCH.get();
        final float yaw = keepRot ? player.getYRot() : 0.0f;
        final float pitch = keepRot ? player.getXRot() : 0.0f;

        final double tx = feet.getX() + 0.5;
        final double ty = feet.getY();
        final double tz = feet.getZ() + 0.5;

        RtpLogger.debug(TryckysRTP.LOGGER, "Teleporting {} to {} in {}", player.getGameProfile().getName(), feet, level.dimension().location());
        player.teleportTo(level, tx, ty, tz, Set.of(), yaw, pitch);
    }
}
