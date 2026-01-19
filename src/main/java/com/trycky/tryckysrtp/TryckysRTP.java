package com.trycky.tryckysrtp;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(TryckysRTP.MODID)
public final class TryckysRTP {
    public static final String MODID = "tryckysrtp";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TryckysRTP(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, RtpConfig.SPEC);

        modEventBus.addListener(this::onConfigLoading);
        modEventBus.addListener(this::onConfigReloading);

        NeoForge.EVENT_BUS.register(this);

        LOGGER.info("{} loaded", MODID);
    }

    private void onConfigLoading(final ModConfigEvent.Loading event) {
        if (event.getConfig().getType() != ModConfig.Type.COMMON) return;
        validateConfig();
        LOGGER.info("{} config loaded", MODID);
    }

    private void onConfigReloading(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getType() != ModConfig.Type.COMMON) return;
        validateConfig();
        LOGGER.info("{} config reloaded", MODID);
    }

    private static void validateConfig() {
        final int min = RtpConfig.RADIUS_MIN.get();
        final int max = RtpConfig.RADIUS_MAX.get();
        if (max < min) {
            LOGGER.warn("Invalid config: radiusMax ({}) < radiusMin ({}). Values will be swapped at runtime.", max, min);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("{} server starting", MODID);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        RtpCommand.register(event.getDispatcher());
    }
}
