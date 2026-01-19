package com.trycky.tryckysrtp;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Cooldown helper built on {@link RtpCooldownData}.
 */
public final class RtpCooldowns {
    private RtpCooldowns() {}

    /**
     * @return remaining millis, or 0 if allowed
     */
    public static long getRemainingMillis(MinecraftServer server, UUID playerUuid, long nowEpochMillis) {
        final String key = playerUuid.toString();
        final long next = RtpCooldownData.get(server).getNextAllowedEpochMillis(key);
        return Math.max(0L, next - nowEpochMillis);
    }

    /**
     * Consumes the cooldown if allowed.
     *
     * @return remaining millis if denied, or 0 if allowed and consumed
     */
    public static long tryConsume(ServerPlayer player, long nowEpochMillis, int cooldownSeconds, boolean bypass) {
        if (bypass || cooldownSeconds <= 0) return 0L;

        final MinecraftServer server = player.getServer();
        if (server == null) return 0L;

        final UUID uuid = player.getUUID();
        final String key = uuid.toString();

        final RtpCooldownData data = RtpCooldownData.get(server);
        final long nextAllowed = data.getNextAllowedEpochMillis(key);
        final long remaining = nextAllowed - nowEpochMillis;
        if (remaining > 0L) return remaining;

        final long next = nowEpochMillis + (cooldownSeconds * 1000L);
        data.setNextAllowedEpochMillis(key, next);
        return 0L;
    }
}
