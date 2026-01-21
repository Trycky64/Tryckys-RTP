package com.trycky.tryckysrtp;

import org.slf4j.Logger;

/**
 * W08 — Configurable logs: OFF / INFO / DEBUG.
 * Centralize all mod logging to keep output clean and admin-friendly.
 */
public final class RtpLogger {
    private RtpLogger() {}

    public enum Level {
        OFF,
        INFO,
        DEBUG
    }

    public static void info(Logger logger, String msg, Object... args) {
        if (RtpConfig.LOG_LEVEL.get() == Level.INFO || RtpConfig.LOG_LEVEL.get() == Level.DEBUG) {
            logger.info(msg, args);
        }
    }

    public static void debug(Logger logger, String msg, Object... args) {
        if (RtpConfig.LOG_LEVEL.get() == Level.DEBUG) {
            logger.debug(msg, args);
        }
    }

    public static void warn(Logger logger, String msg, Object... args) {
        // Warnings are always visible (admin needs them)
        logger.warn(msg, args);
    }

    public static void error(Logger logger, String msg, Object... args) {
        logger.error(msg, args);
    }
}
