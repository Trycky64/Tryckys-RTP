package com.trycky.tryckysrtp;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Method;

/**
 * W06 — Advanced permissions.
 *
 * Supports:
 * - Permission API if present (reflection) with nodes:
 *   - tryckysrtp.bypass.cooldown
 *   - tryckysrtp.bypass.unsafe
 *   - tryckysrtp.command.reload
 * Otherwise falls back to OP levels (configurable).
 */
public final class RtpPermissions {
    private RtpPermissions() {}

    public static final String BYPASS_COOLDOWN = "tryckysrtp.bypass.cooldown";
    public static final String BYPASS_UNSAFE = "tryckysrtp.bypass.unsafe";
    public static final String COMMAND_RELOAD = "tryckysrtp.command.reload";

    public static boolean canBypassCooldown(CommandSourceStack src, ServerPlayer player) {
        return hasPermission(player, BYPASS_COOLDOWN, src.hasPermission(RtpConfig.PERM_BYPASS_COOLDOWN_LEVEL.get()));
    }

    public static boolean canBypassUnsafe(CommandSourceStack src, ServerPlayer player) {
        return hasPermission(player, BYPASS_UNSAFE, src.hasPermission(RtpConfig.PERM_BYPASS_UNSAFE_LEVEL.get()));
    }

    public static boolean canReload(CommandSourceStack src) {
        // console allowed too
        if (src.hasPermission(RtpConfig.PERM_RELOAD_LEVEL.get())) return true;
        final ServerPlayer p = src.getPlayer();
        if (p == null) return true;
        return hasPermission(p, COMMAND_RELOAD, false);
    }

    private static boolean hasPermission(ServerPlayer player, String node, boolean fallback) {
        if (player == null) return fallback;

        // Try NeoForge PermissionAPI via reflection (avoid hard dependency / compile risk)
        try {
            final Class<?> api = Class.forName("net.neoforged.neoforge.server.permission.PermissionAPI");
            final Method getPermission = api.getMethod("getPermission", ServerPlayer.class, String.class);
            final Object result = getPermission.invoke(null, player, node);
            if (result instanceof Boolean b) return b;
        } catch (Throwable ignored) {
            // no-op (fallback)
        }

        return fallback;
    }
}
