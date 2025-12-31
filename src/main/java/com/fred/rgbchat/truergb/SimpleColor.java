package com.fred.rgbchat.truergb;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record SimpleColor(int alpha, int red, int green, int blue) implements IColor {
    public SimpleColor(int red, int green, int blue) {
        this(255, red, green, blue);
    }

    public static IColor of(String s) {
        if (s.length() == 6) {
            return new SimpleColor(Integer.valueOf(s.substring(0, 2), 16), Integer.valueOf(s.substring(2, 4), 16), Integer.valueOf(s.substring(4, 6), 16));
        }
        if (s.length() == 8) {
            return new SimpleColor(Integer.valueOf(s.substring(0, 2), 16), Integer.valueOf(s.substring(2, 4), 16), Integer.valueOf(s.substring(4, 6), 16), Integer.valueOf(s.substring(6, 8), 16));
        }
        return FormatColor.WHITE;
    }

    @Override
    public int toInt() {
        return this.red << 16 | this.green << 8 | this.blue;
    }
}
