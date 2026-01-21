package com.trycky.tryckysrtp;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.Objects;

/**
 * W02 — sound & particles
 * W03 — title on arrival
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
        sendArrivalTitle(player, feet);
    }

    public static void clearCaches() {
        lastSoundId = null;
        cachedSound = null;
        warnedSound = false;

        lastParticleId = null;
        cachedParticle = null;
        warnedParticle = false;
    }

    private static void playArrivalSound(ServerPlayer player) {
        if (!RtpConfig.FEEDBACK_SOUND_ENABLED.get()) return;

        final String id = Objects.toString(RtpConfig.FEEDBACK_SOUND_EVENT.get(), "").trim();
        if (id.isEmpty()) return;

        final SoundEvent sound = resolveSound(id);
        if (sound == null) return;

        final float volume = RtpConfig.FEEDBACK_SOUND_VOLUME.get().floatValue();
        final float pitch = RtpConfig.FEEDBACK_SOUND_PITCH.get().floatValue();

        player.playNotifySound(sound, SoundSource.PLAYERS, volume, pitch);
    }

    private static void spawnArrivalParticles(ServerPlayer player) {
        if (!RtpConfig.FEEDBACK_PARTICLES_ENABLED.get()) return;

        final String id = Objects.toString(RtpConfig.FEEDBACK_PARTICLES_TYPE.get(), "").trim();

        ParticleOptions particle = null;
        if (!id.isEmpty()) {
            particle = resolveParticle(id);
        }
        if (particle == null) {
            particle = ParticleTypes.HAPPY_VILLAGER; // fallback visible
        }

        final int count = RtpConfig.FEEDBACK_PARTICLES_COUNT.get();
        if (count <= 0) return;

        final ServerLevel level = player.serverLevel();

        final double x = player.getX();
        final double y = player.getY() + 1.0D;
        final double z = player.getZ();

        final double dx = RtpConfig.FEEDBACK_PARTICLES_SPREAD_X.get();
        final double dy = RtpConfig.FEEDBACK_PARTICLES_SPREAD_Y.get();
        final double dz = RtpConfig.FEEDBACK_PARTICLES_SPREAD_Z.get();
        final double speed = RtpConfig.FEEDBACK_PARTICLES_SPEED.get();

        level.sendParticles(particle, x, y, z, count, dx, dy, dz, speed);
        RtpLogger.debug(TryckysRTP.LOGGER, "RTP particles sent to {} (type={}, count={})", player.getGameProfile().getName(), particle, count);
    }

    private static void sendArrivalTitle(ServerPlayer player, BlockPos feet) {
        if (!RtpConfig.TITLE_ENABLED.get()) return;
        if (RtpMessages.isSilent()) return;

        final String dimId = player.serverLevel().dimension().location().toString();

        final String title = RtpMessages.title(player, feet, dimId);
        final String subtitle = RtpMessages.subtitle(player, feet, dimId);

        if (title.isEmpty() && subtitle.isEmpty()) return;

        final int fadeIn = Math.max(0, RtpConfig.TITLE_FADE_IN_TICKS.get());
        final int stay = Math.max(0, RtpConfig.TITLE_STAY_TICKS.get());
        final int fadeOut = Math.max(0, RtpConfig.TITLE_FADE_OUT_TICKS.get());

        player.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
        if (!title.isEmpty()) player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title)));
        if (!subtitle.isEmpty()) player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(subtitle)));
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
            RtpLogger.warn(TryckysRTP.LOGGER, "Invalid feedback.sound.event '{}' (sound not found).", id);
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
            RtpLogger.warn(TryckysRTP.LOGGER, "Invalid feedback.particles.type '{}' (not found or not SimpleParticleType). Using fallback.", id);
        }

        return cachedParticle;
    }
}
