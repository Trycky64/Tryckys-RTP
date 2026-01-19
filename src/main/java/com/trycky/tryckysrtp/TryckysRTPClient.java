package com.trycky.tryckysrtp;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Optional client-only entrypoint (safe on dedicated servers).
 * Keeps the NeoForge auto config screen enabled.
 */
@Mod(value = TryckysRTP.MODID, dist = Dist.CLIENT)
public final class TryckysRTPClient {
    public TryckysRTPClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
