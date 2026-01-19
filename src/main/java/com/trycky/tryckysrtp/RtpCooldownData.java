package com.trycky.tryckysrtp;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Persistent per-world (Overworld) cooldown storage.
 *
 * Stores a map of player UUID (as String) -> nextAllowedEpochMillis.
 */
public final class RtpCooldownData extends SavedData {
    /**
     * SavedData identifier. File path (in world folder) will be:
     * data/tryckysrtp/rtp_cooldowns.dat
     */
    public static final SavedDataType<RtpCooldownData> ID = new SavedDataType<>(
            "tryckysrtp/rtp_cooldowns",
            RtpCooldownData::new,
            ctx -> CODEC
    );

    private static final Codec<RtpCooldownData> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Codec.unboundedMap(Codec.STRING, Codec.LONG)
                            .fieldOf("cooldowns")
                            .forGetter(sd -> sd.cooldowns)
            )
            .apply(instance, RtpCooldownData::new)
    );

    private final Map<String, Long> cooldowns;

    public RtpCooldownData() {
        this(new HashMap<>());
    }

    private RtpCooldownData(Map<String, Long> cooldowns) {
        this.cooldowns = new HashMap<>(Objects.requireNonNull(cooldowns));
    }

    public static RtpCooldownData get(MinecraftServer server) {
        // Attach to overworld for "global" server data.
        return server.overworld().getDataStorage().computeIfAbsent(ID);
    }

    public long getNextAllowedEpochMillis(String playerUuid) {
        return cooldowns.getOrDefault(playerUuid, 0L);
    }

    public void setNextAllowedEpochMillis(String playerUuid, long nextAllowedEpochMillis) {
        cooldowns.put(playerUuid, nextAllowedEpochMillis);
        setDirty();
    }
}
