package com.trycky.tryckysrtp;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.time.Duration;
import java.util.UUID;

public final class RtpCooldowns {

    private RtpCooldowns() {}

    public static boolean isOnCooldown(MinecraftServer server, UUID playerId) {
        ServerLevel level = server.overworld();
        long now = System.currentTimeMillis();
        long next = RtpCooldownData.get(level).getNextAllowedEpochMillis(playerId);
        return next > now;
    }

    public static Duration remaining(MinecraftServer server, UUID playerId) {
        ServerLevel level = server.overworld();
        long now = System.currentTimeMillis();
        long next = RtpCooldownData.get(level).getNextAllowedEpochMillis(playerId);
        long remainingMs = Math.max(0L, next - now);
        return Duration.ofMillis(remainingMs);
    }

    public static void startCooldown(MinecraftServer server, UUID playerId) {
        ServerLevel level = server.overworld();
        RtpCooldownData data = RtpCooldownData.get(level);
        long now = System.currentTimeMillis();
        data.setNextAllowedEpochMillis(playerId, now + data.getCooldownMillis());
    }
}
