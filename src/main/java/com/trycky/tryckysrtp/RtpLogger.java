package com.trycky.tryckysrtp;

import org.slf4j.Logger;

/**
 * W08 — Configurable logs: OFF / INFO / DEBUG.
 *
 * IMPORTANT: NeoForge config values cannot be read before config load.
 * So we keep a runtime level that becomes effective once configs are loaded/reloaded.
 */
public final class RtpLogger {
    private RtpLogger() {}

    public enum Level {
        OFF,
        INFO,
        DEBUG
    }

    // Default before config load
    private static volatile Level runtimeLevel = Level.INFO;

    public static void applyConfig() {
        // Called after config is loaded/reloaded (safe time)
        try {
            runtimeLevel = RtpConfig.LOG_LEVEL.get();
        } catch (Throwable ignored) {
            // Keep previous/runtime default
        }
    }

    public static void info(Logger logger, String msg, Object... args) {
        final Level lvl = runtimeLevel;
        if (lvl == Level.INFO || lvl == Level.DEBUG) {
            logger.info(msg, args);
        }
    }

    public static void debug(Logger logger, String msg, Object... args) {
        if (runtimeLevel == Level.DEBUG) {
            logger.debug(msg, args);
        }
    }

    public static void warn(Logger logger, String msg, Object... args) {
        logger.warn(msg, args);
    }

    public static void error(Logger logger, String msg, Object... args) {
        logger.error(msg, args);
    }
}
