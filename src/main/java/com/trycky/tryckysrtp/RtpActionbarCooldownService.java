package com.trycky.tryckysrtp;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * W03 — Actionbar cooldown (no spam).
 * W11 — Progressive cooldown display (optional bar).
 *
 * Strategy:
 * - Runs at most once per updatePeriodTicks (default 20).
 * - Sends actionbar only if remainingSeconds changed (default).
 * - Stops naturally when cooldown reaches 0.
 */
public final class RtpActionbarCooldownService {
    private RtpActionbarCooldownService() {}

    private static int tickCounter = 0;
    private static final Map<UUID, Long> lastShownSeconds = new HashMap<>();

    public static void onServerTick(MinecraftServer server) {
        if (!RtpConfig.ACTIONBAR_COOLDOWN_ENABLED.get()) return;

        final int period = Math.max(1, RtpConfig.ACTIONBAR_COOLDOWN_UPDATE_PERIOD_TICKS.get());
        tickCounter++;
        if (tickCounter < period) return;
        tickCounter = 0;

        final long now = System.currentTimeMillis();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player == null) continue;

            final long nextAllowed = RtpCooldownData.get(player.serverLevel()).getNextAllowedEpochMillis(player.getUUID());
            if (nextAllowed <= now) {
                lastShownSeconds.remove(player.getUUID());
                continue;
            }

            final long remainingMs = nextAllowed - now;
            final long remainingSec = (long) Math.ceil(remainingMs / 1000.0);

            if (RtpConfig.ACTIONBAR_COOLDOWN_ONLY_WHEN_CHANGED.get()) {
                final Long last = lastShownSeconds.get(player.getUUID());
                if (last != null && last == remainingSec) continue;
            }
            lastShownSeconds.put(player.getUUID(), remainingSec);

            if (RtpMessages.isSilent()) continue;

            final String msg = buildCooldownMessage(player, Duration.ofMillis(remainingMs), remainingSec);
            if (msg.isEmpty()) continue;

            player.displayClientMessage(Component.literal(msg), true); // true => actionbar
        }
    }

    private static String buildCooldownMessage(ServerPlayer player, Duration remaining, long remainingSec) {
        String base = RtpMessages.actionbarCooldown(player, remaining);
        if (base.isEmpty()) return base;

        if (!RtpConfig.ACTIONBAR_COOLDOWN_PROGRESSIVE.get()) {
            return base;
        }

        // Progressive bar (cheap, purely text)
        final int total = Math.max(1, RtpConfig.COOLDOWN_SECONDS.get());
        final double ratio = 1.0 - Math.min(1.0, Math.max(0.0, remainingSec / (double) total));
        final int width = Math.max(5, Math.min(30, RtpConfig.ACTIONBAR_COOLDOWN_BAR_WIDTH.get()));

        final int filled = (int) Math.round(ratio * width);
        final String bar = "[" + "#".repeat(Math.max(0, filled)) + "-".repeat(Math.max(0, width - filled)) + "]";

        return base.replace("%bar%", bar);
    }

    public static void clearPlayer(UUID uuid) {
        if (uuid != null) lastShownSeconds.remove(uuid);
    }

    public static void clearAll() {
        lastShownSeconds.clear();
        tickCounter = 0;
    }
}
