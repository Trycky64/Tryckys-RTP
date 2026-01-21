package com.trycky.tryckysrtp;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * W10 — Config validation (warnings, no crash).
 * Goal: keep server safe even with bad config.
 */
public final class RtpConfigValidator {
    private RtpConfigValidator() {}

    public static void validate() {
        // radius min/max sanity
        final int min = RtpConfig.RADIUS_MIN.get();
        final int max = RtpConfig.RADIUS_MAX.get();
        if (max < min) {
            RtpLogger.warn(TryckysRTP.LOGGER, "Invalid config: radiusMax ({}) < radiusMin ({}). Values will be swapped at runtime.", max, min);
        }

        // sound volume/pitch sanity
        final double volume = RtpConfig.FEEDBACK_SOUND_VOLUME.get();
        final double pitch = RtpConfig.FEEDBACK_SOUND_PITCH.get();
        if (volume < 0.0 || volume > 10.0) {
            RtpLogger.warn(TryckysRTP.LOGGER, "Config warning: feedback.sound.volume={} (recommended range 0..10).", volume);
        }
        if (pitch <= 0.0 || pitch > 10.0) {
            RtpLogger.warn(TryckysRTP.LOGGER, "Config warning: feedback.sound.pitch={} (recommended range >0..10).", pitch);
        }

        // particles sanity
        final int count = RtpConfig.FEEDBACK_PARTICLES_COUNT.get();
        if (count < 0 || count > 500) {
            RtpLogger.warn(TryckysRTP.LOGGER, "Config warning: feedback.particles.count={} (recommended 0..500).", count);
        }

        // dimension list parsing
        validateDimensionList("allowedDimensions", RtpConfig.ALLOWED_DIMENSIONS.get());
        validateDimensionList("blockedDimensions", RtpConfig.BLOCKED_DIMENSIONS.get());

        // actionbar settings
        final int period = RtpConfig.ACTIONBAR_COOLDOWN_UPDATE_PERIOD_TICKS.get();
        if (period < 1) {
            RtpLogger.warn(TryckysRTP.LOGGER, "Config warning: feedback.actionbarCooldown.updatePeriodTicks={} (must be >=1).", period);
        }
    }

    private static void validateDimensionList(String name, List<? extends String> list) {
        for (String s : list) {
            if (s == null || s.isBlank()) continue;
            if (ResourceLocation.tryParse(s) == null) {
                RtpLogger.warn(TryckysRTP.LOGGER, "Config warning: {} contains invalid resource location: '{}'", name, s);
            }
        }
    }
}
