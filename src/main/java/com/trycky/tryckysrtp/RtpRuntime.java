package com.trycky.tryckysrtp;

import net.neoforged.fml.loading.FMLPaths;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * W05 — Clean reload:
 * - Reload config (best-effort)
 * - Clear caches (messages/feedback/actionbar state)
 */
public final class RtpRuntime {
    private RtpRuntime() {}

    public static void reloadAll() {
        reloadConfigBestEffort();
        clearCaches();
        RtpConfigValidator.validate();
    }

    public static void clearCaches() {
        RtpFeedback.clearCaches();
        RtpActionbarCooldownService.clearAll();
    }

    private static void reloadConfigBestEffort() {
        // Best-effort: try ConfigTracker (reflection to avoid hard dependency issues).
        try {
            final Class<?> trackerClz = Class.forName("net.neoforged.fml.config.ConfigTracker");
            final Field instanceField = trackerClz.getField("INSTANCE");
            final Object tracker = instanceField.get(null);

            final Method loadConfigs = trackerClz.getMethod("loadConfigs", net.neoforged.fml.config.ModConfig.Type.class, java.nio.file.Path.class);

            // Reload COMMON configs from config dir
            loadConfigs.invoke(tracker, net.neoforged.fml.config.ModConfig.Type.COMMON, FMLPaths.CONFIGDIR.get());

            RtpLogger.info(TryckysRTP.LOGGER, "Config reload requested via ConfigTracker.");
            return;
        } catch (Throwable ignored) {
            // fallback below
        }

        RtpLogger.warn(TryckysRTP.LOGGER, "Could not trigger live config reload (API not available). Caches were cleared; config may require a server restart.");
    }
}
