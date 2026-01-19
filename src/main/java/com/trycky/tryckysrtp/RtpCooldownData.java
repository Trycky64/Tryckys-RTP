package com.trycky.tryckysrtp;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class RtpCooldownData extends SavedData {

    private static final String DATA_NAME = TryckysRTP.MODID + "_cooldowns";

    private final Map<String, Long> nextAllowedMillisByUuid = new HashMap<>();

    public RtpCooldownData() {}

    /**
     * Persist to overworld so cooldown survives dimension changes.
     */
    public static RtpCooldownData get(ServerLevel anyLevel) {
        final ServerLevel overworld = anyLevel.getServer().getLevel(Level.OVERWORLD);
        final ServerLevel storageLevel = (overworld != null) ? overworld : anyLevel;

        return storageLevel.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        RtpCooldownData::new,
                        RtpCooldownData::load
                ),
                DATA_NAME
        );
    }

    public long getNextAllowedEpochMillis(UUID uuid) {
        return nextAllowedMillisByUuid.getOrDefault(uuid.toString(), 0L);
    }

    public void setNextAllowedEpochMillis(UUID uuid, long nextAllowedMillis) {
        nextAllowedMillisByUuid.put(uuid.toString(), nextAllowedMillis);
        setDirty();
    }

    public long getCooldownMillis() {
        final long seconds = Math.max(0L, (long) RtpConfig.COOLDOWN_SECONDS.get());
        return seconds * 1000L;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag mapTag = new CompoundTag();
        for (var e : nextAllowedMillisByUuid.entrySet()) {
            mapTag.putLong(e.getKey(), e.getValue());
        }
        tag.put("nextAllowedMillisByUuid", mapTag);
        return tag;
    }

    public static RtpCooldownData load(CompoundTag tag, HolderLookup.Provider provider) {
        RtpCooldownData data = new RtpCooldownData();
        CompoundTag mapTag = tag.getCompound("nextAllowedMillisByUuid");
        for (String key : mapTag.getAllKeys()) {
            data.nextAllowedMillisByUuid.put(key, mapTag.getLong(key));
        }
        return data;
    }
}
