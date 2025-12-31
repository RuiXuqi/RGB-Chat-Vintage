package com.fred.rgbchat.truergb;

import com.github.bsideup.jabel.Desugar;

import java.util.Objects;

/**
 * @author Kasumi_Nova
 * @see <a href="https://github.com/NovaEngineering-Source/StellarCore/blob/47477615e390eed9a55c140e8af57f8cf209da70/src/main/java/github/kasuminova/stellarcore/client/gui/font/TextRenderInfo.java">GitHub</a>
 */
@Desugar
public record TextRenderInfo(String info, int color) {
    @Override
    public boolean equals(final Object o) {
        if (o instanceof TextRenderInfo renderInfo) {
            return renderInfo.hashCode() == hashCode();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(info, color);
    }
}
