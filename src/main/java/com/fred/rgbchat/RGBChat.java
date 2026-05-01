package com.fred.rgbchat;

import com.fred.rgbchat.truergb.CachedRGBFontRenderer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

@Mod(
        modid = Reference.MOD_ID,
        name = Reference.MOD_NAME,
        version = Reference.VERSION,
        clientSideOnly = true,
        acceptableRemoteVersions = "*",
        dependencies = "required-after:mixinbooter@[8.0,)",
        customProperties = {
                @Mod.CustomProperty(k = "license", v = "LGPL-3.0"),
                @Mod.CustomProperty(k = "issueTrackerUrl", v = "https://github.com/RuiXuqi/RGB-Chat-Vintage/issues")
        }
)
public class RGBChat {
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        CachedRGBFontRenderer.overrideFontRenderer();
    }
}
