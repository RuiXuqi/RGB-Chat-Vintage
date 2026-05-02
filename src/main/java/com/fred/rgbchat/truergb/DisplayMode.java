package com.fred.rgbchat.truergb;

public enum DisplayMode {
    /// 正常渲染
    NORMAL,
    /// 忽略 RGB 参数
    IGNORE_RGB,
    /// 原版渲染，保留 RGB 文本并使用原版颜色控制符
    VANILLA,
    /// 逐字渲染，忽略所有
    CHAR_BY_CHAR
}
