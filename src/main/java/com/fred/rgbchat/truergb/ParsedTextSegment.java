package com.fred.rgbchat.truergb;

import com.fred.rgbchat.truergb.color.IColor;
import com.fred.rgbchat.truergb.color.SimpleColor;
import com.github.bsideup.jabel.Desugar;

import javax.annotation.Nullable;
import java.util.List;

@Desugar
public record ParsedTextSegment(String text, RGBSettings settings, int gradientStartIndex, int gradientTotalLength) {
    @Nullable
    public IColor getColorAt(int charIndexInSegment) {
        List<IColor> colors = this.settings.getColors();
        int colorsSize = colors.size();
        if (colorsSize == 0) return null;
        if (colorsSize == 1) return colors.get(0);

        int absoluteIndex = this.gradientStartIndex + charIndexInSegment;
        int totalLen = Math.max(1, this.gradientTotalLength);
        absoluteIndex = Math.max(0, Math.min(totalLen - 1, absoluteIndex));

        float colorPosition = (colorsSize - 1) * absoluteIndex / (float) Math.max(1, totalLen - 1);
        int preIndex = Math.max(0, Math.min(colorsSize - 1, (int) colorPosition));
        int postIndex = Math.max(0, Math.min(colorsSize - 1, preIndex + 1));
        IColor pre = colors.get(preIndex);
        IColor post = colors.get(postIndex);
        if (pre.equals(post)) return pre;

        float percent = postIndex == preIndex ? 0.0F : colorPosition - preIndex;

        return new SimpleColor(
                mix(pre.red(), post.red(), percent),
                mix(pre.green(), post.green(), percent),
                mix(pre.blue(), post.blue(), percent)
        );
    }

    private static int mix(int pre, int post, float percent) {
        return Math.round(pre * (1.0F - percent) + post * percent);
    }
}
