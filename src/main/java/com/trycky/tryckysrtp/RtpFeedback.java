package com.trycky.tryckysrtp;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.Objects;

/**
 * W02: sound & particle feedback on RTP arrival.
 * Safety goals:
 * - Never crash on bad config (skip + warn once per invalid id).
 * - Target only the teleported player (no broadcast).
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
        if (id.isEmpty()) return;

        final ParticleOptions particle = resolveParticle(id);
        if (particle == null) return;

        final int count = RtpConfig.FEEDBACK_PARTICLES_COUNT.get();
        if (count <= 0) return;

        // Spawn at player center (more reliable than feet pos, avoids being inside blocks)
        final double x = player.getX();
        final double y = player.getY() + 0.8D;
        final double z = player.getZ();

        final float dx = (float) (double) RtpConfig.FEEDBACK_PARTICLES_SPREAD_X.get();
        final float dy = (float) (double) RtpConfig.FEEDBACK_PARTICLES_SPREAD_Y.get();
        final float dz = (float) (double) RtpConfig.FEEDBACK_PARTICLES_SPREAD_Z.get();
        final float speed = (float) (double) RtpConfig.FEEDBACK_PARTICLES_SPEED.get();

        final boolean force = RtpConfig.FEEDBACK_PARTICLES_FORCE.get();

        // Player-only, packet-based (robuste)
        // Note: client settings can still hide particles if the player disabled them.
        final ClientboundLevelParticlesPacket pkt =
                new ClientboundLevelParticlesPacket(particle, force, x, y, z, dx, dy, dz, speed, count);

        player.connection.send(pkt);
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
            TryckysRTP.LOGGER.warn("Invalid feedback.sound.event '{}' (sound not found). Sound feedback disabled for this id.", id);
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
                    "Invalid feedback.particles.type '{}' (particle not found or not a SimpleParticleType). Particle feedback disabled for this id.",
                    id
            );
        }

        return cachedParticle;
    }
}
