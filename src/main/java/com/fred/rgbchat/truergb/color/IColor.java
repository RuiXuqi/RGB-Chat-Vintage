package com.fred.rgbchat.truergb.color;

public interface IColor {
    int red();

    int green();

    int blue();

    default int toInt() {
        return 0xFF000000 | (this.red() << 16) | (this.green() << 8) | this.blue();
    }
}
