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
import net.neoforged.neoforge.event.tick.ServerTickEvent;
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

        RtpLogger.info(LOGGER, "{} loaded", MODID);
    }

    private void onConfigLoading(final ModConfigEvent.Loading event) {
        if (event.getConfig().getType() != ModConfig.Type.COMMON) return;
        RtpConfigValidator.validate();
        RtpLogger.info(LOGGER, "{} config loaded", MODID);
    }

    private void onConfigReloading(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getType() != ModConfig.Type.COMMON) return;
        RtpRuntime.clearCaches();
        RtpConfigValidator.validate();
        RtpLogger.info(LOGGER, "{} config reloaded", MODID);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        RtpCommand.register(event.getDispatcher());
    }

    // W03/W11 — actionbar cooldown service (cheap, internally throttled)
    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
        if (event.getServer() == null) return;
        RtpActionbarCooldownService.onServerTick(event.getServer());
    }
}
