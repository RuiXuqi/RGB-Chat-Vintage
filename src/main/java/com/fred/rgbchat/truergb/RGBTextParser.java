package com.fred.rgbchat.truergb;

import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

public final class RGBTextParser {
    @Nonnull
    public static List<ParsedTextSegment> parse(@Nullable String string) {
        if (string == null || string.isEmpty()) {
            return Collections.singletonList(new ParsedTextSegment("", RGBSettings.EMPTY, 0, 0));
        }

        List<ParsedTextSegment> result = new ArrayList<>();
        Matcher matcher = RGBSettings.PATTERN.matcher(string);
        RGBSettings currentSettings = RGBSettings.EMPTY;
        int index = 0;
        int currentGradientLength = 0;
        int currentGroupStart = 0;

        while (index < string.length()) {
            if (!matcher.find(index)) {
                currentGradientLength = appendSegment(result, currentSettings, string.substring(index), currentGradientLength);
                break;
            }

            currentGradientLength = appendSegment(result, currentSettings, string.substring(index, matcher.start()), currentGradientLength);

            String format = matcher.group();
            if (format.startsWith("#")) {
                finalizeGradientGroup(result, currentGroupStart, currentGradientLength);
                currentGroupStart = result.size();
                currentGradientLength = 0;
                currentSettings = currentSettings.withColors(RGBSettings.parseColors(matcher.group("rgb")));
            } else {
                TextFormatting formatting = RGBSettings.formattingOf(format.charAt(1));
                if (formatting != null) {
                    currentSettings = currentSettings.withFormat(formatting);
                }
            }
            index = matcher.end();
        }

        finalizeGradientGroup(result, currentGroupStart, currentGradientLength);
        return result;
    }

    private static int appendSegment(List<ParsedTextSegment> result, RGBSettings settings, String text, int currentGradientLength) {
        if (text.isEmpty()) {
            return currentGradientLength;
        }

        result.add(new ParsedTextSegment(text, settings, currentGradientLength, 0));
        return currentGradientLength + text.length();
    }

    private static void finalizeGradientGroup(List<ParsedTextSegment> segments, int groupStartIndex, int gradientTotalLength) {
        for (int i = groupStartIndex; i < segments.size(); i++) {
            ParsedTextSegment segment = segments.get(i);
            segments.set(i, new ParsedTextSegment(segment.text(), segment.settings(), segment.gradientStartIndex(), gradientTotalLength));
        }
    }
}
