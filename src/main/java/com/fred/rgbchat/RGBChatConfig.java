package com.fred.rgbchat;

import com.fred.rgbchat.truergb.CachedRGBFontRenderer;
import com.fred.rgbchat.truergb.DisplayMode;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Config(modid = Reference.MOD_ID)
public class RGBChatConfig {
    public static DisplayMode displayMode = DisplayMode.NORMAL;

    @Mod.EventBusSubscriber(value = Side.CLIENT, modid = Reference.MOD_ID)
    public static class ConfigSyncEvent {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Reference.MOD_ID)) {
                ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
                CachedRGBFontRenderer.setDisplayMode(displayMode);
            }
        }
    }
}
