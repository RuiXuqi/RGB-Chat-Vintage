package com.fred.rgbchat.truergb;

import com.fred.rgbchat.truergb.color.FormatColor;
import com.fred.rgbchat.truergb.color.IColor;
import com.fred.rgbchat.truergb.color.SimpleColor;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class RGBSettings {
    public static final Pattern PATTERN = Pattern.compile("(#(?<rgb>([0-9a-fA-F]{6})(-([0-9a-fA-F]{6}))*)|§(?<format>[0-9a-fA-FklmnorKLMNOR]))");
    public static final RGBSettings EMPTY = new RGBSettings(Collections.emptyList(), false, false, false, false, false);
    private final List<IColor> colors;
    private final boolean bold;
    private final boolean italic;
    private final boolean underlined;
    private final boolean strikethrough;
    private final boolean obfuscated;

    private RGBSettings(List<IColor> colors, boolean bold, boolean italic, boolean underlined, boolean strikethrough, boolean obfuscated) {
        this.colors = colors;
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.strikethrough = strikethrough;
        this.obfuscated = obfuscated;
    }

    public RGBSettings withColors(IColor newColor) {
        return new RGBSettings(Collections.singletonList(newColor), false, false, false, false, false);
    }

    public RGBSettings withColors(List<IColor> newColors) {
        return new RGBSettings(newColors, false, false, false, false, false);
    }

    public RGBSettings withFormat(TextFormatting formatting) {
        if (formatting.isColor()) return this.withColors(FormatColor.of(formatting));
        if (formatting == TextFormatting.RESET) return EMPTY;

        boolean newBold = formatting == TextFormatting.BOLD || this.bold;
        boolean newItalic = formatting == TextFormatting.ITALIC || this.italic;
        boolean newUnderline = formatting == TextFormatting.UNDERLINE || this.underlined;
        boolean newStrike = formatting == TextFormatting.STRIKETHROUGH || this.strikethrough;
        boolean newObfuscated = formatting == TextFormatting.OBFUSCATED || this.obfuscated;
        return new RGBSettings(this.colors, newBold, newItalic, newUnderline, newStrike, newObfuscated);
    }

    public boolean isFixedColor() {
        return this.colors.size() <= 1;
    }

    @Nonnull
    public List<IColor> getColors() {
        return this.colors;
    }

    @Nonnull
    public String getFormatString() {
        StringBuilder result = new StringBuilder(10);
        if (this.bold) result.append(TextFormatting.BOLD);
        if (this.italic) result.append(TextFormatting.ITALIC);
        if (this.underlined) result.append(TextFormatting.UNDERLINE);
        if (this.strikethrough) result.append(TextFormatting.STRIKETHROUGH);
        if (this.obfuscated) result.append(TextFormatting.OBFUSCATED);
        return result.toString();
    }

    @Nonnull
    public String getColorString() {
        StringBuilder result = new StringBuilder();
        boolean hasColor = false;
        for (IColor color : this.colors) {
            result.append(hasColor ? '-' : '#');
            appendHexColor(result, color.toInt());
            hasColor = true;
        }
        return result.toString();
    }

    @Nonnull
    public String getColorAndFormatString() {
        return this.getColorString() + this.getFormatString();
    }

    @Nullable
    static TextFormatting formattingOf(char c) {
        int index = "0123456789abcdefklmnor".indexOf(Character.toLowerCase(c));
        return index < 0 ? null : TextFormatting.values()[index];
    }

    @Nonnull
    static List<IColor> parseColors(String rgbList) {
        if (rgbList.indexOf('-') < 0) {
            return Collections.singletonList(SimpleColor.of(rgbList));
        }

        List<IColor> result = new ArrayList<>();
        int index = 0;
        while (index <= rgbList.length()) {
            int nextDelimiter = rgbList.indexOf('-', index);
            boolean hasDelimiter = nextDelimiter >= 0;
            result.add(SimpleColor.of(hasDelimiter ? rgbList.substring(index, nextDelimiter) : rgbList.substring(index)));

            if (!hasDelimiter) break;
            index = nextDelimiter + 1;
        }
        return result;
    }

    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    private static void appendHexColor(StringBuilder result, int rgb) {
        for (int shift = 20; shift >= 0; shift -= 4) {
            result.append(HEX_DIGITS[(rgb >> shift) & 0x0F]);
        }
    }
}
