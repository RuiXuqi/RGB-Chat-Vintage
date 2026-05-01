package com.fred.rgbchat.truergb.color;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record SimpleColor(int red, int green, int blue) implements IColor {
    public static IColor of(String s) {
        if (s.length() != 6) {
            return FormatColor.WHITE;
        }

        int rgb = Integer.parseInt(s, 16);
        return new SimpleColor(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF);
    }
}
