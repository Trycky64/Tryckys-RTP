package com.trycky.tryckysrtp;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.Objects;

/**
 * W02: sound & particle feedback on RTP arrival.
 * Goal: reliable visuals on dedicated servers.
 */
public final class RtpFeedback {
    private RtpFeedback() {}

    private static String lastSoundId = null;
    private static SoundEvent cachedSound = null;
    private static boolean warnedSound = false;

    private static String lastParticleId = null;
    private static ParticleOptions cachedParticle = null;
    private static boolean warnedParticle = false;

    public static void onArrival(ServerPlayer player, BlockPos feet) {
        if (player == null || feet == null) return;

        playArrivalSound(player);
        spawnArrivalParticles(player);
    }

    private static void playArrivalSound(ServerPlayer player) {
        if (!RtpConfig.FEEDBACK_SOUND_ENABLED.get()) return;

        final String id = Objects.toString(RtpConfig.FEEDBACK_SOUND_EVENT.get(), "").trim();
        if (id.isEmpty()) return;

        final SoundEvent sound = resolveSound(id);
        if (sound == null) return;

        final float volume = RtpConfig.FEEDBACK_SOUND_VOLUME.get().floatValue();
        final float pitch = RtpConfig.FEEDBACK_SOUND_PITCH.get().floatValue();

        // Player-only (no broadcast)
        player.playNotifySound(sound, SoundSource.PLAYERS, volume, pitch);
    }

    private static void spawnArrivalParticles(ServerPlayer player) {
        if (!RtpConfig.FEEDBACK_PARTICLES_ENABLED.get()) return;

        final String id = Objects.toString(RtpConfig.FEEDBACK_PARTICLES_TYPE.get(), "").trim();

        ParticleOptions particle = null;
        if (!id.isEmpty()) {
            particle = resolveParticle(id);
        }

        // Fallback visible particle if config is invalid / unsupported
        if (particle == null) {
            particle = ParticleTypes.HAPPY_VILLAGER;
        }

        final int count = RtpConfig.FEEDBACK_PARTICLES_COUNT.get();
        if (count <= 0) return;

        final ServerLevel level = player.serverLevel();

        // Center on player eye-ish position (more visible than feet)
        final double x = player.getX();
        final double y = player.getY() + 1.0D;
        final double z = player.getZ();

        final double dx = RtpConfig.FEEDBACK_PARTICLES_SPREAD_X.get();
        final double dy = RtpConfig.FEEDBACK_PARTICLES_SPREAD_Y.get();
        final double dz = RtpConfig.FEEDBACK_PARTICLES_SPREAD_Z.get();
        final double speed = RtpConfig.FEEDBACK_PARTICLES_SPEED.get();

        // Reliable vanilla-style send (broadcast to nearby players, includes the player)
        level.sendParticles(particle, x, y, z, count, dx, dy, dz, speed);

        TryckysRTP.LOGGER.debug(
                "RTP particles sent: type={}, count={}, pos=({}, {}, {}), spread=({}, {}, {}), speed={}",
                particle, count, x, y, z, dx, dy, dz, speed
        );
    }

    private static SoundEvent resolveSound(String id) {
        if (!id.equals(lastSoundId)) {
            lastSoundId = id;
            cachedSound = null;
            warnedSound = false;

            final ResourceLocation rl = ResourceLocation.tryParse(id);
            if (rl == null) return null;

            cachedSound = BuiltInRegistries.SOUND_EVENT.getOptional(rl).orElse(null);
        }

        if (cachedSound == null && !warnedSound) {
            warnedSound = true;
            TryckysRTP.LOGGER.warn(
                    "Invalid feedback.sound.event '{}' (sound not found). Sound feedback disabled for this id.",
                    id
            );
        }

        return cachedSound;
    }

    private static ParticleOptions resolveParticle(String id) {
        if (!id.equals(lastParticleId)) {
            lastParticleId = id;
            cachedParticle = null;
            warnedParticle = false;

            final ResourceLocation rl = ResourceLocation.tryParse(id);
            if (rl == null) return null;

            final ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.getOptional(rl).orElse(null);
            if (type instanceof SimpleParticleType simple) {
                cachedParticle = simple;
            } else {
                cachedParticle = null;
            }
        }

        if (cachedParticle == null && !warnedParticle) {
            warnedParticle = true;
            TryckysRTP.LOGGER.warn(
                    "Invalid feedback.particles.type '{}' (particle not found or not a SimpleParticleType). Using fallback 'minecraft:happy_villager'.",
                    id
            );
        }

        return cachedParticle;
    }
}
