package com.trycky.tryckysrtp;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.time.Duration;
import java.util.Locale;
import java.util.Objects;

/**
 * W01 — Configurable messages.
 * W07 — Silent mode.
 * W09 — i18n-ready: config messages are optional, defaults can be translatable (command uses them).
 * W10 — Safe formatting: never crashes if placeholders missing.
 */
public final class RtpMessages {
    private RtpMessages() {}

    public static boolean isSilent() {
        return RtpConfig.MESSAGES_SILENT.get();
    }

    public static String success(ServerPlayer player, BlockPos pos, String dimensionId) {
        return formatTemplate(RtpConfig.MSG_SUCCESS.get(), player, pos, dimensionId, null);
    }

    public static String fail(ServerPlayer player, String dimensionId) {
        return formatTemplate(RtpConfig.MSG_FAIL.get(), player, null, dimensionId, null);
    }

    public static String actionbarCooldown(ServerPlayer player, Duration remaining) {
        return formatTemplate(RtpConfig.ACTIONBAR_COOLDOWN_MESSAGE.get(), player, null, null, remaining);
    }

    public static String title(ServerPlayer player, BlockPos pos, String dimensionId) {
        return formatTemplate(RtpConfig.TITLE_TEXT.get(), player, pos, dimensionId, null);
    }

    public static String subtitle(ServerPlayer player, BlockPos pos, String dimensionId) {
        return formatTemplate(RtpConfig.SUBTITLE_TEXT.get(), player, pos, dimensionId, null);
    }

    private static String formatTemplate(String template, ServerPlayer player, BlockPos pos, String dimensionId, Duration remaining) {
        String out = Objects.toString(template, "");
        if (out.isEmpty()) return out;

        final String name = (player != null) ? player.getGameProfile().getName() : "";
        final String dim = Objects.toString(dimensionId, "");

        out = out.replace("%player%", name);
        out = out.replace("%dimension%", dim);

        if (pos != null) {
            out = out.replace("%x%", Integer.toString(pos.getX()));
            out = out.replace("%y%", Integer.toString(pos.getY()));
            out = out.replace("%z%", Integer.toString(pos.getZ()));
        }

        if (remaining != null) {
            out = out.replace("%remaining%", formatDuration(remaining));
            out = out.replace("%remaining_seconds%", Long.toString(Math.max(0, remaining.toSeconds())));
        }

        return out;
    }

    public static String formatDuration(Duration d) {
        long seconds = Math.max(0, d.getSeconds());
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        if (hours > 0) return String.format(Locale.ROOT, "%dh %dm %ds", hours, minutes, seconds);
        if (minutes > 0) return String.format(Locale.ROOT, "%dm %ds", minutes, seconds);
        return String.format(Locale.ROOT, "%ds", seconds);
    }
}
