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

public final class RtpCommand {

    private RtpCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("rtp")
                        .requires(src -> true)
                        .executes(ctx -> execute(ctx.getSource()))
        );
    }

    private static int execute(CommandSourceStack src) {
        final ServerPlayer player;
        try {
            player = src.getPlayerOrException();
        } catch (Exception e) {
            src.sendFailure(Component.translatable("tryckysrtp.rtp.only_players"));
            return 0;
        }

        final ServerLevel level = player.serverLevel();
        final String dimId = level.dimension().location().toString();
        final String playerName = player.getGameProfile().getName();

        if (!RtpSafeTeleport.isRtpAllowedInDimension(level)) {
            player.displayClientMessage(
                    Component.translatable("tryckysrtp.rtp.bad_dimension", dimId).withStyle(ChatFormatting.RED),
                    false
            );
            return 0;
        }

        // Cooldown: bypass for ops (permission level 2)
        final boolean bypass = src.hasPermission(2);

        final UUID id = player.getUUID();
        final long now = System.currentTimeMillis();
        final long nextAllowed = RtpCooldownData.get(level).getNextAllowedEpochMillis(id);

        if (!bypass && nextAllowed > now) {
            final Duration remaining = Duration.ofMillis(nextAllowed - now);
            player.displayClientMessage(
                    Component.translatable(
                            "tryckysrtp.rtp.cooldown",
                            formatDuration(remaining)
                    ).withStyle(ChatFormatting.RED),
                    false
            );
            return 0;
        }

        player.displayClientMessage(
                Component.translatable("tryckysrtp.rtp.searching").withStyle(ChatFormatting.YELLOW),
                false
        );

        final RtpSafeTeleport.Result result = RtpSafeTeleport.findSafeDestination(level, player);
        if (!result.success) {
            TryckysRTP.LOGGER.debug("RTP failed for {} in {}: {}", playerName, dimId, result.errorKey);
            player.displayClientMessage(
                    Component.literal(RtpMessages.fail(playerName, dimId)).withStyle(ChatFormatting.RED),
                    false
            );
            return 0;
        }

        try {
            RtpSafeTeleport.teleportPlayer(player, level, result.pos);
        } catch (Exception ex) {
            TryckysRTP.LOGGER.error("RTP teleport failed for {}", playerName, ex);
            player.displayClientMessage(
                    Component.literal(RtpMessages.fail(playerName, dimId)).withStyle(ChatFormatting.RED),
                    false
            );
            return 0;
        }

        if (!bypass) {
            RtpCooldowns.startCooldown(src.getServer(), id);
        }

        // W02: feedback (sound + particles) on arrival, player-only
        RtpFeedback.onArrival(player, result.pos);

        final String newDimId = player.serverLevel().dimension().location().toString();

        player.displayClientMessage(
                Component.literal(RtpMessages.success(playerName, result.pos, newDimId)).withStyle(ChatFormatting.GREEN),
                false
        );

        if (RtpConfig.LOG_SUCCESS.get()) {
            TryckysRTP.LOGGER.info(
                    "RTP: {} -> ({}, {}, {}) in {}",
                    playerName,
                    result.pos.getX(), result.pos.getY(), result.pos.getZ(),
                    newDimId
            );
        } else {
            TryckysRTP.LOGGER.debug(
                    "RTP success for {} -> ({}, {}, {}) in {}",
                    playerName,
                    result.pos.getX(), result.pos.getY(), result.pos.getZ(),
                    newDimId
            );
        }

        return 1;
    }

    private static String formatDuration(Duration d) {
        long seconds = Math.max(0, d.getSeconds());
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        if (hours > 0) return hours + "h " + minutes + "m " + seconds + "s";
        if (minutes > 0) return minutes + "m " + seconds + "s";
        return seconds + "s";
    }
}
