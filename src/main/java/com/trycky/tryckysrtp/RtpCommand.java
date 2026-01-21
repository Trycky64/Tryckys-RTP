package com.trycky.tryckysrtp;

import com.mojang.brigadier.CommandDispatcher;
import com.trycky.tryckysrtp.rtp.RtpSafeTeleport;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.time.Duration;
import java.util.UUID;

/**
 * W04 — /rtp status
 * W05 — /rtp reload
 * W12 — /rtp help
 * Refactor: clean subcommands, single execution flow.
 */
public final class RtpCommand {
    private RtpCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("rtp")
                        .requires(src -> true)
                        .executes(ctx -> executeRtp(ctx.getSource()))
                        .then(Commands.literal("help").executes(ctx -> help(ctx.getSource())))
                        .then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
                        .then(Commands.literal("reload")
                                .requires(RtpPermissions::canReload)
                                .executes(ctx -> reload(ctx.getSource())))
        );
    }

    private static int help(CommandSourceStack src) {
        src.sendSuccess(() -> Component.literal("Trycky's RTP — Commands").withStyle(ChatFormatting.GOLD), false);

        src.sendSuccess(() -> Component.literal("/rtp")
                .append(Component.literal(" — Random teleport").withStyle(ChatFormatting.GRAY)), false);

        src.sendSuccess(() -> Component.literal("/rtp status")
                .append(Component.literal(" — Show cooldown and dimension status").withStyle(ChatFormatting.GRAY)), false);

        src.sendSuccess(() -> Component.literal("/rtp reload")
                .append(Component.literal(" — Reload config + clear caches (admin)").withStyle(ChatFormatting.GRAY)), false);

        src.sendSuccess(() -> Component.literal("Permissions:").withStyle(ChatFormatting.YELLOW), false);
        src.sendSuccess(() -> Component.literal("- " + RtpPermissions.BYPASS_COOLDOWN + " (fallback OP level " + RtpConfig.PERM_BYPASS_COOLDOWN_LEVEL.get() + ")").withStyle(ChatFormatting.GRAY), false);
        src.sendSuccess(() -> Component.literal("- " + RtpPermissions.BYPASS_UNSAFE + " (fallback OP level " + RtpConfig.PERM_BYPASS_UNSAFE_LEVEL.get() + ")").withStyle(ChatFormatting.GRAY), false);
        src.sendSuccess(() -> Component.literal("- " + RtpPermissions.COMMAND_RELOAD + " (fallback OP level " + RtpConfig.PERM_RELOAD_LEVEL.get() + ")").withStyle(ChatFormatting.GRAY), false);

        return 1;
    }

    private static int status(CommandSourceStack src) {
        final ServerPlayer player;
        try {
            player = src.getPlayerOrException();
        } catch (Exception e) {
            src.sendFailure(Component.literal("Only players can use this command."));
            return 0;
        }

        final ServerLevel level = player.serverLevel();
        final String dimId = level.dimension().location().toString();
        final boolean allowed = RtpSafeTeleport.isRtpAllowedInDimension(level);

        final UUID id = player.getUUID();
        final long now = System.currentTimeMillis();
        final long nextAllowed = RtpCooldownData.get(level).getNextAllowedEpochMillis(id);

        final boolean bypassCooldown = RtpPermissions.canBypassCooldown(src, player);
        final boolean bypassUnsafe = RtpPermissions.canBypassUnsafe(src, player);

        player.displayClientMessage(
                Component.literal("RTP Status").withStyle(ChatFormatting.GOLD),
                false
        );

        player.displayClientMessage(
                Component.literal("Dimension: ").withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal(dimId).withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(allowed ? "ALLOWED" : "BLOCKED").withStyle(allowed ? ChatFormatting.GREEN : ChatFormatting.RED))
                        .append(Component.literal(")").withStyle(ChatFormatting.GRAY)),
                false
        );

        if (bypassCooldown) {
            player.displayClientMessage(Component.literal("Cooldown: BYPASS").withStyle(ChatFormatting.GREEN), false);
        } else if (nextAllowed <= now) {
            player.displayClientMessage(Component.literal("Cooldown: READY").withStyle(ChatFormatting.GREEN), false);
        } else {
            final Duration remaining = Duration.ofMillis(nextAllowed - now);
            player.displayClientMessage(
                    Component.literal("Cooldown: ").withStyle(ChatFormatting.YELLOW)
                            .append(Component.literal(RtpMessages.formatDuration(remaining)).withStyle(ChatFormatting.RED)),
                    false
            );
        }

        player.displayClientMessage(
                Component.literal("Bypass unsafe: ").withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal(bypassUnsafe ? "YES" : "NO").withStyle(bypassUnsafe ? ChatFormatting.GREEN : ChatFormatting.GRAY)),
                false
        );

        return 1;
    }

    private static int reload(CommandSourceStack src) {
        RtpRuntime.reloadAll();
        src.sendSuccess(() -> Component.literal("Tryckys RTP: config reloaded (best-effort) + caches cleared.").withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int executeRtp(CommandSourceStack src) {
        final ServerPlayer player;
        try {
            player = src.getPlayerOrException();
        } catch (Exception e) {
            src.sendFailure(Component.literal("Only players can use this command."));
            return 0;
        }

        final ServerLevel level = player.serverLevel();
        final String dimId = level.dimension().location().toString();

        if (!RtpSafeTeleport.isRtpAllowedInDimension(level)) {
            if (!RtpMessages.isSilent()) {
                player.displayClientMessage(Component.literal("You cannot use /rtp in this dimension (" + dimId + ").").withStyle(ChatFormatting.RED), false);
            }
            return 0;
        }

        final boolean bypassCooldown = RtpPermissions.canBypassCooldown(src, player);
        final boolean bypassUnsafe = RtpPermissions.canBypassUnsafe(src, player);

        final UUID id = player.getUUID();
        final long now = System.currentTimeMillis();
        final long nextAllowed = RtpCooldownData.get(level).getNextAllowedEpochMillis(id);

        if (!bypassCooldown && nextAllowed > now) {
            if (!RtpMessages.isSilent()) {
                final Duration remaining = Duration.ofMillis(nextAllowed - now);
                player.displayClientMessage(
                        Component.literal("You must wait " + RtpMessages.formatDuration(remaining) + " before using /rtp again.").withStyle(ChatFormatting.RED),
                        false
                );
            }
            return 0;
        }

        if (!RtpMessages.isSilent()) {
            player.displayClientMessage(Component.literal("Searching for a safe spot...").withStyle(ChatFormatting.YELLOW), false);
        }

        final RtpSafeTeleport.Result result = RtpSafeTeleport.findSafeDestination(level, player, bypassUnsafe);
        if (!result.success) {
            if (!RtpMessages.isSilent()) {
                player.displayClientMessage(Component.literal(result.errorMessage).withStyle(ChatFormatting.RED), false);
            }
            return 0;
        }

        try {
            RtpSafeTeleport.teleportPlayer(player, level, result.pos);
        } catch (Exception ex) {
            RtpLogger.error(TryckysRTP.LOGGER, "RTP teleport failed for {}", player.getGameProfile().getName(), ex);
            if (!RtpMessages.isSilent()) {
                player.displayClientMessage(Component.literal("Teleport failed. Check server logs.").withStyle(ChatFormatting.RED), false);
            }
            return 0;
        }

        if (!bypassCooldown) {
            RtpCooldowns.startCooldown(src.getServer(), id);
        }

        final String newDimId = player.serverLevel().dimension().location().toString();

        // W01 success message from config
        if (!RtpMessages.isSilent()) {
            final String msg = RtpMessages.success(player, result.pos, newDimId);
            if (!msg.isEmpty()) player.displayClientMessage(Component.literal(msg).withStyle(ChatFormatting.GREEN), false);
        }

        // W02/W03 arrival feedback
        RtpFeedback.onArrival(player, result.pos);

        // W08 logs
        RtpLogger.info(
                TryckysRTP.LOGGER,
                "RTP: {} -> ({}, {}, {}) in {}",
                player.getGameProfile().getName(),
                result.pos.getX(), result.pos.getY(), result.pos.getZ(),
                newDimId
        );

        return 1;
    }
}
