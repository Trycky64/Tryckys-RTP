package com.trycky.tryckysrtp;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/**
 * Common (server) config for /rtp.
 */
public final class RtpConfig {
    private RtpConfig() {}

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public enum DimensionMode {
        ALLOWLIST,
        DENYLIST
    }

    public enum SafeHeightMode {
        MOTION_BLOCKING_NO_LEAVES,
        MOTION_BLOCKING,
        WORLD_SURFACE,
        OCEAN_FLOOR
    }

    public static final ModConfigSpec.IntValue COOLDOWN_SECONDS = BUILDER
            .comment("Cooldown in seconds between two /rtp uses for the same player.")
            .defineInRange("cooldownSeconds", 3600, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue RADIUS_MIN = BUILDER
            .comment("Minimum random teleport radius (blocks).")
            .defineInRange("radiusMin", 500, 0, 2_000_000);

    public static final ModConfigSpec.IntValue RADIUS_MAX = BUILDER
            .comment("Maximum random teleport radius (blocks).", "Must be >= radiusMin.")
            .defineInRange("radiusMax", 5000, 0, 2_000_000);

    public static final ModConfigSpec.IntValue ATTEMPTS_MAX = BUILDER
            .comment("How many candidate positions we try before giving up.")
            .defineInRange("attemptsMax", 30, 1, 10_000);

    public static final ModConfigSpec.IntValue MIN_DISTANCE_FROM_SPAWN = BUILDER
            .comment("Minimum distance from world spawn (blocks).", "0 disables this check.")
            .defineInRange("minDistanceFromSpawn", 200, 0, 2_000_000);

    public static final ModConfigSpec.EnumValue<DimensionMode> DIMENSION_MODE = BUILDER
            .comment("Dimension filtering mode: ALLOWLIST or DENYLIST.")
            .defineEnum("dimensionMode", DimensionMode.DENYLIST);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> ALLOWED_DIMENSIONS = BUILDER
            .comment(
                    "Dimensions allowed to use /rtp when dimensionMode=ALLOWLIST.",
                    "Values are resource locations, e.g.: minecraft:overworld"
            )
            .defineListAllowEmpty(
                    "allowedDimensions",
                    List.of("minecraft:overworld"),
                    o -> o instanceof String
            );

    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED_DIMENSIONS = BUILDER
            .comment(
                    "Dimensions blocked from using /rtp when dimensionMode=DENYLIST.",
                    "Values are resource locations, e.g.: minecraft:the_nether"
            )
            .defineListAllowEmpty(
                    "blockedDimensions",
                    List.of(),
                    o -> o instanceof String
            );

    public static final ModConfigSpec.BooleanValue ALLOW_IN_NETHER = BUILDER
            .comment(
                    "If false, /rtp is disabled in minecraft:the_nether.",
                    "This is applied in addition to dimensionMode allow/deny lists."
            )
            .define("allowInNether", true);

    public static final ModConfigSpec.BooleanValue ALLOW_IN_END = BUILDER
            .comment(
                    "If false, /rtp is disabled in minecraft:the_end.",
                    "This is applied in addition to dimensionMode allow/deny lists."
            )
            .define("allowInEnd", true);

    public static final ModConfigSpec.BooleanValue AVOID_LIQUIDS = BUILDER
            .comment("If true, the safe-spot search rejects positions in water/lava.")
            .define("avoidLiquids", true);

    public static final ModConfigSpec.BooleanValue REQUIRE_SOLID_GROUND = BUILDER
            .comment("If true, the safe-spot search requires a solid block under the player.")
            .define("requireSolidGround", true);

    public static final ModConfigSpec.EnumValue<SafeHeightMode> SAFE_HEIGHT_MODE = BUILDER
            .comment("Heightmap used to find the top safe Y coordinate for a random (x,z).")
            .defineEnum("safeHeightMode", SafeHeightMode.MOTION_BLOCKING_NO_LEAVES);

    public static final ModConfigSpec.BooleanValue KEEP_YAW_PITCH = BUILDER
            .comment("If true, keeps the player's yaw/pitch on teleport. Otherwise resets to 0.")
            .define("keepYawPitch", true);

    public static final ModConfigSpec.BooleanValue LOG_SUCCESS = BUILDER
            .comment(
                    "If true, logs one INFO line per successful /rtp.",
                    "If false, success logs are DEBUG only."
            )
            .define("logSuccess", false);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
