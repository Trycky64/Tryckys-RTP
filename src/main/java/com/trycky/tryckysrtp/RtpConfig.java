package com.trycky.tryckysrtp;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/**
 * Common (server) config for /rtp.
 * Focus: clarity for server admins.
 */
public final class RtpConfig {
    private RtpConfig() {}

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public enum DimensionMode { ALLOWLIST, DENYLIST }
    public enum SafeHeightMode { MOTION_BLOCKING_NO_LEAVES, MOTION_BLOCKING, WORLD_SURFACE, OCEAN_FLOOR }

    // W08
    public static final ModConfigSpec.EnumValue<RtpLogger.Level> LOG_LEVEL = BUILDER
            .comment("Logging level for Tryckys RTP: OFF / INFO / DEBUG.")
            .defineEnum("logging.level", RtpLogger.Level.INFO);

    // Core
    public static final ModConfigSpec.IntValue COOLDOWN_SECONDS = BUILDER
            .comment("Cooldown in seconds between two /rtp uses for the same player.")
            .defineInRange("cooldownSeconds", 3600, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue RADIUS_MIN = BUILDER
            .comment("Minimum random teleport radius (blocks).")
            .defineInRange("radiusMin", 500, 0, 2_000_000);

    public static final ModConfigSpec.IntValue RADIUS_MAX = BUILDER
            .comment("Maximum random teleport radius (blocks). Must be >= radiusMin.")
            .defineInRange("radiusMax", 5000, 0, 2_000_000);

    public static final ModConfigSpec.IntValue ATTEMPTS_MAX = BUILDER
            .comment("How many candidate positions we try before giving up.")
            .defineInRange("attemptsMax", 30, 1, 10_000);

    public static final ModConfigSpec.IntValue MIN_DISTANCE_FROM_SPAWN = BUILDER
            .comment("Minimum distance from world spawn (blocks). 0 disables this check.")
            .defineInRange("minDistanceFromSpawn", 200, 0, 2_000_000);

    // Dimensions
    public static final ModConfigSpec.EnumValue<DimensionMode> DIMENSION_MODE = BUILDER
            .comment("Dimension filtering mode: ALLOWLIST or DENYLIST.")
            .defineEnum("dimensionMode", DimensionMode.DENYLIST);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> ALLOWED_DIMENSIONS = BUILDER
            .comment("Dimensions allowed when dimensionMode=ALLOWLIST. Example: minecraft:overworld")
            .defineListAllowEmpty("allowedDimensions", List.of("minecraft:overworld"), o -> o instanceof String);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED_DIMENSIONS = BUILDER
            .comment("Dimensions blocked when dimensionMode=DENYLIST. Example: minecraft:the_nether")
            .defineListAllowEmpty("blockedDimensions", List.of(), o -> o instanceof String);

    public static final ModConfigSpec.BooleanValue ALLOW_IN_NETHER = BUILDER
            .comment("If false, /rtp is disabled in minecraft:the_nether. Applied in addition to allow/deny lists.")
            .define("allowInNether", true);

    public static final ModConfigSpec.BooleanValue ALLOW_IN_END = BUILDER
            .comment("If false, /rtp is disabled in minecraft:the_end. Applied in addition to allow/deny lists.")
            .define("allowInEnd", true);

    // Safety
    public static final ModConfigSpec.BooleanValue AVOID_LIQUIDS = BUILDER
            .comment("If true, the safe-spot search rejects positions in water/lava.")
            .define("avoidLiquids", true);

    public static final ModConfigSpec.BooleanValue REQUIRE_SOLID_GROUND = BUILDER
            .comment("If true, the safe-spot search requires a solid block under the player.")
            .define("requireSolidGround", true);

    public static final ModConfigSpec.EnumValue<SafeHeightMode> SAFE_HEIGHT_MODE = BUILDER
            .comment("Heightmap used to find the top safe Y coordinate for a random (x,z).")
            .defineEnum("safeHeightMode", SafeHeightMode.MOTION_BLOCKING_NO_LEAVES);

    public static final ModConfigSpec.BooleanValue KEEP_YAW_PITCH = BUILDER
            .comment("If true, keeps the player's yaw/pitch on teleport. Otherwise resets to 0.")
            .define("keepYawPitch", true);

    public static final ModConfigSpec.BooleanValue SURFACE_ONLY_IN_SKYLIGHT_DIMS = BUILDER
            .comment("If true, in skylight dimensions the destination must see the sky (avoid caves in overworld).")
            .define("surfaceOnlyInSkylightDims", true);

    public static final ModConfigSpec.IntValue MAX_CEILING_CLEARANCE = BUILDER
            .comment("Ceiling dims (e.g. Nether): reject destinations too open above. 0 disables.")
            .defineInRange("maxCeilingClearance", 32, 0, 512);

    // W01 + W07 (messages)
    public static final ModConfigSpec.BooleanValue MESSAGES_SILENT = BUILDER
            .comment("If true, disables player chat/actionbar/title messages (useful for RP servers).")
            .define("messages.silent", false);

    public static final ModConfigSpec.ConfigValue<String> MSG_SUCCESS = BUILDER
            .comment("Success message. Placeholders: %x% %y% %z% %dimension% %player%")
            .define("messages.success", "Teleported to %x% %y% %z% (%dimension%).");

    public static final ModConfigSpec.ConfigValue<String> MSG_FAIL = BUILDER
            .comment("Failure message. Placeholders: %dimension% %player%")
            .define("messages.fail", "RTP failed.");

    // W02 feedback: sound
    public static final ModConfigSpec.BooleanValue FEEDBACK_SOUND_ENABLED = BUILDER
            .comment("Enable arrival sound.")
            .define("feedback.sound.enabled", true);

    public static final ModConfigSpec.ConfigValue<String> FEEDBACK_SOUND_EVENT = BUILDER
            .comment("Sound event id. Example: minecraft:entity_enderman_teleport")
            .define("feedback.sound.event", "minecraft:entity_enderman_teleport");

    public static final ModConfigSpec.DoubleValue FEEDBACK_SOUND_VOLUME = BUILDER
            .comment("Sound volume.")
            .defineInRange("feedback.sound.volume", 1.0, 0.0, 10.0);

    public static final ModConfigSpec.DoubleValue FEEDBACK_SOUND_PITCH = BUILDER
            .comment("Sound pitch.")
            .defineInRange("feedback.sound.pitch", 1.0, 0.1, 10.0);

    // W02 feedback: particles
    public static final ModConfigSpec.BooleanValue FEEDBACK_PARTICLES_ENABLED = BUILDER
            .comment("Enable arrival particles.")
            .define("feedback.particles.enabled", true);

    public static final ModConfigSpec.ConfigValue<String> FEEDBACK_PARTICLES_TYPE = BUILDER
            .comment("Particle type id (SimpleParticleType). Example: minecraft:portal")
            .define("feedback.particles.type", "minecraft:portal");

    public static final ModConfigSpec.IntValue FEEDBACK_PARTICLES_COUNT = BUILDER
            .comment("Particles count.")
            .defineInRange("feedback.particles.count", 40, 0, 500);

    public static final ModConfigSpec.DoubleValue FEEDBACK_PARTICLES_SPREAD_X = BUILDER
            .defineInRange("feedback.particles.spreadX", 0.8, 0.0, 10.0);

    public static final ModConfigSpec.DoubleValue FEEDBACK_PARTICLES_SPREAD_Y = BUILDER
            .defineInRange("feedback.particles.spreadY", 1.0, 0.0, 10.0);

    public static final ModConfigSpec.DoubleValue FEEDBACK_PARTICLES_SPREAD_Z = BUILDER
            .defineInRange("feedback.particles.spreadZ", 0.8, 0.0, 10.0);

    public static final ModConfigSpec.DoubleValue FEEDBACK_PARTICLES_SPEED = BUILDER
            .defineInRange("feedback.particles.speed", 0.1, 0.0, 10.0);

    // W03 title
    public static final ModConfigSpec.BooleanValue TITLE_ENABLED = BUILDER
            .comment("Enable title/subtitle on RTP arrival.")
            .define("feedback.title.enabled", true);

    public static final ModConfigSpec.ConfigValue<String> TITLE_TEXT = BUILDER
            .comment("Title text. Placeholders: %x% %y% %z% %dimension% %player%")
            .define("feedback.title.title", "RTP");

    public static final ModConfigSpec.ConfigValue<String> SUBTITLE_TEXT = BUILDER
            .comment("Subtitle text. Placeholders: %x% %y% %z% %dimension% %player%")
            .define("feedback.title.subtitle", "%x% %y% %z% (%dimension%)");

    public static final ModConfigSpec.IntValue TITLE_FADE_IN_TICKS = BUILDER
            .defineInRange("feedback.title.fadeInTicks", 10, 0, 200);

    public static final ModConfigSpec.IntValue TITLE_STAY_TICKS = BUILDER
            .defineInRange("feedback.title.stayTicks", 40, 0, 400);

    public static final ModConfigSpec.IntValue TITLE_FADE_OUT_TICKS = BUILDER
            .defineInRange("feedback.title.fadeOutTicks", 10, 0, 200);

    // W03 + W11 actionbar cooldown
    public static final ModConfigSpec.BooleanValue ACTIONBAR_COOLDOWN_ENABLED = BUILDER
            .comment("Show cooldown in actionbar.")
            .define("feedback.actionbarCooldown.enabled", true);

    public static final ModConfigSpec.ConfigValue<String> ACTIONBAR_COOLDOWN_MESSAGE = BUILDER
            .comment("Actionbar message. Placeholders: %remaining% %remaining_seconds% %player% %bar%")
            .define("feedback.actionbarCooldown.message", "Cooldown: %remaining% %bar%");

    public static final ModConfigSpec.IntValue ACTIONBAR_COOLDOWN_UPDATE_PERIOD_TICKS = BUILDER
            .comment("How often to update actionbar (ticks). 20 = once per second.")
            .defineInRange("feedback.actionbarCooldown.updatePeriodTicks", 20, 1, 200);

    public static final ModConfigSpec.BooleanValue ACTIONBAR_COOLDOWN_ONLY_WHEN_CHANGED = BUILDER
            .comment("If true, only sends actionbar when the remaining seconds change (anti-spam).")
            .define("feedback.actionbarCooldown.onlyWhenChanged", true);

    public static final ModConfigSpec.BooleanValue ACTIONBAR_COOLDOWN_PROGRESSIVE = BUILDER
            .comment("If true, adds a simple progress bar using %bar%.")
            .define("feedback.actionbarCooldown.progressive", true);

    public static final ModConfigSpec.IntValue ACTIONBAR_COOLDOWN_BAR_WIDTH = BUILDER
            .comment("Progress bar width (characters).")
            .defineInRange("feedback.actionbarCooldown.barWidth", 16, 5, 30);

    // W06 permission fallback levels
    public static final ModConfigSpec.IntValue PERM_BYPASS_COOLDOWN_LEVEL = BUILDER
            .comment("Fallback OP level to bypass cooldown when PermissionAPI is not used.")
            .defineInRange("permissions.bypassCooldownLevel", 2, 0, 4);

    public static final ModConfigSpec.IntValue PERM_BYPASS_UNSAFE_LEVEL = BUILDER
            .comment("Fallback OP level to bypass safety restrictions (relaxed checks).")
            .defineInRange("permissions.bypassUnsafeLevel", 2, 0, 4);

    public static final ModConfigSpec.IntValue PERM_RELOAD_LEVEL = BUILDER
            .comment("Fallback OP level required for /rtp reload.")
            .defineInRange("permissions.reloadLevel", 3, 0, 4);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
