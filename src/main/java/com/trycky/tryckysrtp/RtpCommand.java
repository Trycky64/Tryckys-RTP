package com.trycky.tryckysrtp;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Brigadier command registration for /rtp.
 */
public final class RtpCommand {
    private RtpCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("rtp")
                        // accessible to everyone; ops (permission level 2) bypass cooldown.
                        .executes(ctx -> execute(ctx.getSource()))
        );
    }

    private static int execute(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("tryckysrtp.rtp.only_players"));
            return 0;
        }

        final boolean bypass = source.hasPermission(2);
        final long now = System.currentTimeMillis();
        final int cooldownSeconds = RtpConfig.COOLDOWN_SECONDS.get();

        final long remainingMillis = RtpCooldowns.tryConsume(player, now, cooldownSeconds, bypass);
        if (remainingMillis > 0L) {
            source.sendFailure(Component.translatable(
                    "tryckysrtp.rtp.cooldown",
                    formatDuration(remainingMillis)
            ));
            return 0;
        }

        // P3 will implement actual safe random teleport.
        source.sendSuccess(() -> Component.translatable("tryckysrtp.rtp.searching"), false);
        source.sendSuccess(() -> Component.translatable("tryckysrtp.rtp.not_implemented"), false);
        return 1;
    }

    private static String formatDuration(long millis) {
        long seconds = millis / 1000L;
        if (seconds <= 0) return "0s";

        final long hours = seconds / 3600L;
        seconds -= hours * 3600L;
        final long minutes = seconds / 60L;
        seconds -= minutes * 60L;

        if (hours > 0) return hours + "h " + minutes + "m";
        if (minutes > 0) return minutes + "m " + seconds + "s";
        return seconds + "s";
    }
}
