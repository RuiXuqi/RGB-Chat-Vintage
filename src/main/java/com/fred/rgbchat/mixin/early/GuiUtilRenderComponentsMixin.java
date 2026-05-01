package com.fred.rgbchat.mixin.early;

import com.fred.rgbchat.truergb.RGBSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

@Mixin(GuiUtilRenderComponents.class)
public class GuiUtilRenderComponentsMixin {
    @Inject(method = "splitText", at = @At("HEAD"), cancellable = true)
    private static void rgbchat$splitText(ITextComponent component, int width, FontRenderer fontRenderer, boolean keepWordBoundary, boolean keepFormatting, CallbackInfoReturnable<List<ITextComponent>> cir) {
        String formatted = component.getFormattedText();
        Matcher matcher = RGBSettings.PATTERN.matcher(formatted);
        boolean hasRgb = false;
        while (matcher.find()) {
            if (matcher.group("rgb") != null) {
                hasRgb = true;
                break;
            }
        }
        if (!hasRgb) return;

        if (!keepFormatting && !Minecraft.getMinecraft().gameSettings.chatColours) {
            formatted = TextFormatting.getTextWithoutFormattingCodes(formatted);
        }

        List<String> wrappedLines = fontRenderer.listFormattedStringToWidth(formatted, width);
        List<ITextComponent> result = new ArrayList<>(wrappedLines.size());
        for (String wrappedLine : wrappedLines) {
            result.add(new TextComponentString(wrappedLine));
        }
        cir.setReturnValue(result);
    }
}
