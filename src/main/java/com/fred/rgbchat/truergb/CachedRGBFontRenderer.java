package com.fred.rgbchat.truergb;

import com.fred.rgbchat.RGBChatConfig;
import com.fred.rgbchat.truergb.color.IColor;
import com.github.bsideup.jabel.Desugar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 带有缓存的字体渲染器，来自星核.
 *
 * @author Kasumi_Nova
 * @see <a href="https://github.com/NovaEngineering-Source/StellarCore/blob/47477615e390eed9a55c140e8af57f8cf209da70/src/main/java/github/kasuminova/stellarcore/client/gui/font/CachedRGBFontRenderer.java">GitHub</a>
 */
public class CachedRGBFontRenderer extends FontRenderer {
    private static final int TEXT_RENDER_CACHE_LIMIT = 2048;
    private static final int LISTED_CACHE_LIMIT = 1024;
    private static final Map<TextRenderInfo, List<TextRenderFunction>> TEXT_RENDER_CACHE = createCache(TEXT_RENDER_CACHE_LIMIT);
    private static final Map<WrapCacheKey, List<String>> LISTED_CACHE = createCache(LISTED_CACHE_LIMIT);
    private static DisplayMode displayMode = DisplayMode.NORMAL;

    CachedRGBFontRenderer(GameSettings gameSettings, ResourceLocation texture, TextureManager textureManager, boolean unicode) {
        super(gameSettings, texture, textureManager, unicode);
    }

    private static <K, V> Map<K, V> createCache(int maxSize) {
        return new LinkedHashMap<>(maxSize, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return this.size() > maxSize;
            }
        };
    }

    public static void overrideFontRenderer() {
        setDisplayMode(RGBChatConfig.displayMode);

        Minecraft mc = Minecraft.getMinecraft();
        CachedRGBFontRenderer renderer = new CachedRGBFontRenderer(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), mc.renderEngine, false);
        renderer.setUnicodeFlag(mc.fontRenderer.getUnicodeFlag());
        renderer.setBidiFlag(mc.fontRenderer.getBidiFlag());
        mc.fontRenderer = renderer;

        ((SimpleReloadableResourceManager) mc.getResourceManager()).registerReloadListener(renderer);
    }

    public static void setDisplayMode(@Nonnull DisplayMode mode) {
        DisplayMode newMode = Objects.requireNonNull(mode, "mode");
        if (displayMode != newMode) {
            displayMode = newMode;
            clearCaches();
        }
    }

    public static void clearCaches() {
        TEXT_RENDER_CACHE.clear();
        LISTED_CACHE.clear();
    }

    private static int withAlphaFrom(int rgb, int baseColor) {
        return (baseColor & 0xFF000000) | (rgb & 0x00FFFFFF);
    }

    @Override
    public int drawString(@Nullable String text, float x, float y, int color, boolean dropShadow) {
        if (text == null) {
            return 0; // 如果文本为null，则直接返回0
        }
        x += 1; // 大概没问题
        TextRenderInfo textRenderInfo = new TextRenderInfo(text, color, displayMode);

        List<TextRenderFunction> cachedRenderFunction = TEXT_RENDER_CACHE.get(textRenderInfo);
        if (cachedRenderFunction != null) {
            return this.fastDrawString(x, y, dropShadow, cachedRenderFunction);
        }

        cachedRenderFunction = new ArrayList<>(Math.max(1, text.length()));

        if (displayMode == DisplayMode.IGNORE_RGB) {
            String stripped = RGBTextUtils.stripRGB(text);
            cachedRenderFunction.add((_y, _dropShadow) -> super.drawString(stripped, this.posX, _y, color, _dropShadow));
            TEXT_RENDER_CACHE.put(textRenderInfo, cachedRenderFunction);
            return this.fastDrawString(x, y, dropShadow, cachedRenderFunction);
        }

        if (displayMode == DisplayMode.CHAR_BY_CHAR) {
            for (int i = 0; i < text.length(); ++i) {
                String toRender = String.valueOf(text.charAt(i));
                cachedRenderFunction.add((_y, _dropShadow) -> super.drawString(toRender, this.posX, _y, color, _dropShadow));
            }
            TEXT_RENDER_CACHE.put(textRenderInfo, cachedRenderFunction);
            return this.fastDrawString(x, y, dropShadow, cachedRenderFunction);
        }

        // 将文本拆分为字符串和相应的 RGB 设置
        for (ParsedTextSegment segment : RGBTextParser.parse(text)) {
            String content = segment.text(); // 获取字符串
            RGBSettings settings = segment.settings(); // 获取 RGB 设置
            String formatString = settings.getFormatString();

            // 如果 RGB 设置中指定了固定颜色，使用该颜色绘制整个字符串
            if (settings.isFixedColor()) {
                IColor fixedColor = segment.getColorAt(0);
                int finalColor = fixedColor == null ? color : withAlphaFrom(fixedColor.toInt(), color);
                String toRender = formatString + content;
                cachedRenderFunction.add((_y, _dropShadow) -> super.drawString(toRender, this.posX, _y, finalColor, _dropShadow));
                continue;
            }

            // 如果 RGB 设置中没有指定固定颜色，为每个字符分别指定颜色并绘制
            for (int i = 0; i < content.length(); ++i) {
                IColor gradientColor = segment.getColorAt(i);
                int finalColor = gradientColor == null ? color : withAlphaFrom(gradientColor.toInt(), color);
                String toRender = formatString + content.charAt(i);
                cachedRenderFunction.add((_y, _dropShadow) -> super.drawString(toRender, this.posX, _y, finalColor, _dropShadow));
            }
        }

        TEXT_RENDER_CACHE.put(textRenderInfo, cachedRenderFunction);
        return this.fastDrawString(x, y, dropShadow, cachedRenderFunction); // 返回 X 坐标的最终位置
    }

    public int fastDrawString(float x, float y, boolean dropShadow, List<TextRenderFunction> renderFunctions) {
        this.posX = x;

        for (TextRenderFunction renderFunction : renderFunctions) {
            renderFunction.renderText(y, dropShadow);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F);
        return (int) this.posX;
    }

    // 获取字符串的宽度
    @Override
    public int getStringWidth(@Nullable String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        if (displayMode == DisplayMode.IGNORE_RGB) {
            return super.getStringWidth(RGBTextUtils.stripRGB(text));
        }

        if (displayMode == DisplayMode.CHAR_BY_CHAR) {
            int maxWidth = 0;
            int currentWidth = 0;
            for (int i = 0; i < text.length(); ++i) {
                char ch = text.charAt(i);
                if (ch == '\r') {
                    continue;
                }
                if (ch == '\n') {
                    maxWidth = Math.max(maxWidth, currentWidth);
                    currentWidth = 0;
                    continue;
                }
                currentWidth += this.getCharByCharWidth(ch);
            }
            return Math.max(maxWidth, currentWidth);
        }

        StringBuilder sb = new StringBuilder();
        for (ParsedTextSegment segment : RGBTextParser.parse(text)) {
            sb.append("§r"); // 先加重置代码，不然会随上一段变成粗体，使用粗体计算宽度
            sb.append(segment.settings().getFormatString()); // 再添加原有格式代码
            sb.append(segment.text()); // 最后添加文字
        }
        return super.getStringWidth(sb.toString());
    }

    @Nonnull
    @Override
    public List<String> listFormattedStringToWidth(@Nonnull String content, int wrapWidth) {
        WrapCacheKey wrapInfo = new WrapCacheKey(content, wrapWidth, displayMode);
        List<String> cachedListed = LISTED_CACHE.get(wrapInfo);
        if (cachedListed != null) {
            return cachedListed;
        }

        if (displayMode == DisplayMode.IGNORE_RGB) {
            cachedListed = super.listFormattedStringToWidth(RGBTextUtils.stripRGB(content), wrapWidth);
            LISTED_CACHE.put(wrapInfo, cachedListed);
            return cachedListed;
        }

        if (displayMode == DisplayMode.CHAR_BY_CHAR) {
            cachedListed = this.listCharByCharToWidth(content, wrapWidth);
            LISTED_CACHE.put(wrapInfo, cachedListed);
            return cachedListed;
        }

        StringBuilder plainText = new StringBuilder();
        Deque<String> strList = new ArrayDeque<>();
        Deque<String> rgbSettingList = new ArrayDeque<>();

        for (ParsedTextSegment segment : RGBTextParser.parse(content)) {
            plainText.append(segment.text());
            strList.add(segment.text().replace("\n", ""));
            rgbSettingList.add(segment.settings().getColorAndFormatString());
        }

        cachedListed = RGBTextUtils.mapListedString(super.listFormattedStringToWidth(plainText.toString(), wrapWidth), strList, rgbSettingList);
        LISTED_CACHE.put(wrapInfo, cachedListed);
        return cachedListed;
    }

    @Nonnull
    private List<String> listCharByCharToWidth(@Nonnull String content, int wrapWidth) {
        if (content.isEmpty()) {
            return Collections.singletonList("");
        }
        if (wrapWidth <= 0) {
            return Collections.singletonList(content);
        }

        List<String> result = new ArrayList<>();
        int start = 0;
        while (start <= content.length()) {
            int newlineIndex = content.indexOf('\n', start);
            if (newlineIndex < 0) {
                this.wrapCharByCharLine(content.substring(start), wrapWidth, result);
                break;
            }

            String line = content.substring(start, newlineIndex);
            this.wrapCharByCharLine(line, wrapWidth, result);
            start = newlineIndex + 1;
        }

        return result;
    }

    private void wrapCharByCharLine(@Nonnull String line, int wrapWidth, @Nonnull List<String> result) {
        if (line.isEmpty()) {
            result.add("");
            return;
        }

        int start = 0;
        while (start < line.length()) {
            int currentWidth = 0;
            int end = start;
            int lastWhitespace = -1;

            while (end < line.length()) {
                char ch = line.charAt(end);
                int charWidth = this.getCharByCharWidth(ch);
                if (currentWidth + charWidth > wrapWidth) {
                    break;
                }
                currentWidth += charWidth;
                if (Character.isWhitespace(ch)) {
                    lastWhitespace = end;
                }
                end++;
            }

            if (end == line.length()) {
                result.add(line.substring(start));
                return;
            }

            if (end == start) {
                result.add(line.substring(start, start + 1));
                start++;
                continue;
            }

            int breakPos = lastWhitespace >= start ? lastWhitespace + 1 : end;
            result.add(line.substring(start, breakPos));
            start = breakPos;
        }
    }

    private int getCharByCharWidth(char ch) {
        if (ch == '§') {
            int glyph = this.glyphWidth[ch] & 0xFF;
            if (glyph == 0) {
                return 0;
            }

            int left = glyph >>> 4;
            int right = glyph & 0x0F;
            return (right + 1 - left) / 2 + 1;
        }

        return Math.max(0, super.getCharWidth(ch));
    }

    @Desugar
    private record TextRenderInfo(String text, int color, DisplayMode mode) {
    }

    @Desugar
    private record WrapCacheKey(String text, int wrapWidth, DisplayMode mode) {
    }
}
