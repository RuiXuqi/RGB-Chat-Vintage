//package com.fred.rgbchat;
//
//import com.fred.rgbchat.truergb.CachedRGBFontRenderer;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.entity.EntityPlayerSP;
//import net.minecraft.client.gui.GuiMainMenu;
//import net.minecraft.util.text.TextComponentString;
//import net.minecraftforge.client.event.GuiOpenEvent;
//import net.minecraftforge.event.entity.EntityJoinWorldEvent;
//import net.minecraftforge.fml.common.Mod;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//import net.minecraftforge.fml.relauncher.Side;
//
//@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Reference.MOD_ID)
//public final class RGBChatTestMessages {
//    private static final String PREFIX = "\u00A77[RGBChat Test]\u00A7r ";
//    private static final String[] TEST_MESSAGES = {
//            PREFIX + "Plain RGB blocks: #FF5555Red #55FF55Green #5599FFBlue",
//            PREFIX + "Multi-stop gradient: #FF0000-FFFF00-00FF00-00FFFF-0000FF-FF00FFRainbowGradientShowcase",
//            PREFIX + "Mixed with vanilla codes: #FFAA00RGB orange \u00A79vanilla blue \u00A7lvanilla bold \u00A7r#55FF88back to RGB green",
//            PREFIX + "Wrap test: #FF6B6BThis long line is meant to wrap inside the chat box so we can verify that RGB state survives automatic line breaks, #FFD93Dcontinues cleanly after spaces and punctuation, \u00A7bbriefly switches to vanilla blue, \u00A7r#6BCB77and then returns to RGB green for the final stretch of the sentence.",
//            PREFIX + "Explicit newline:\n#FF8888First RGB line\n\u00A79Second vanilla line\n\u00A7r#88FF88Third RGB line again"
//    };
//
//    private static boolean sentForCurrentSession;
//
//    private RGBChatTestMessages() {
//    }
//
//    @SubscribeEvent
//    public static void onMainMenuOpened(GuiOpenEvent event) {
//        if (event.getGui() instanceof GuiMainMenu) {
//            sentForCurrentSession = false;
//        }
//    }
//
//    @SubscribeEvent
//    public static void onLocalPlayerJoinWorld(EntityJoinWorldEvent event) {
//        if (sentForCurrentSession || !event.getWorld().isRemote || !(event.getEntity() instanceof EntityPlayerSP)) {
//            return;
//        }
//
//        Minecraft mc = Minecraft.getMinecraft();
//        if (!mc.isIntegratedServerRunning()) {
//            return;
//        }
//
//        sentForCurrentSession = true;
//        mc.addScheduledTask(RGBChatTestMessages::sendTestMessages);
//    }
//
//    private static void sendTestMessages() {
//        Minecraft mc = Minecraft.getMinecraft();
//        if (mc.ingameGUI == null) {
//            return;
//        }
//
//        CachedRGBFontRenderer.clearCaches();
//        for (String message : TEST_MESSAGES) {
//            mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
//        }
//    }
//}
