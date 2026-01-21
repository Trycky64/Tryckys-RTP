package com.trycky.tryckysrtp;

import net.minecraft.core.BlockPos;

/**
 * W01: server-side configurable messages.
 */
public final class RtpMessages {
    private RtpMessages() {}

    public static String success(String playerName, BlockPos pos, String dimensionId) {
        return applyPlaceholders(RtpConfig.MESSAGE_SUCCESS.get(), playerName, pos, dimensionId);
    }

    public static String fail(String playerName, String dimensionId) {
        return applyPlaceholders(RtpConfig.MESSAGE_FAIL.get(), playerName, null, dimensionId);
    }

    private static String applyPlaceholders(String template, String playerName, BlockPos pos, String dimensionId) {
        if (template == null || template.isEmpty()) return "";

        String out = template;
        out = out.replace("%player%", playerName == null ? "" : playerName);
        out = out.replace("%dimension%", dimensionId == null ? "" : dimensionId);

        if (pos != null) {
            out = out.replace("%x%", Integer.toString(pos.getX()));
            out = out.replace("%y%", Integer.toString(pos.getY()));
            out = out.replace("%z%", Integer.toString(pos.getZ()));
        } else {
            out = out.replace("%x%", "");
            out = out.replace("%y%", "");
            out = out.replace("%z%", "");
        }

        return out;
    }
}
