package com.fred.rgbchat.truergb;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * 带有缓存的字体渲染器，来自星核.
 *
 * @author Kasumi_Nova
 * @see <a href="https://github.com/NovaEngineering-Source/StellarCore/blob/47477615e390eed9a55c140e8af57f8cf209da70/src/main/java/github/kasuminova/stellarcore/client/gui/font/CachedRGBFontRenderer.java">GitHub</a>
 */
public class CachedRGBFontRenderer extends FontRenderer {
    private static final Map<TextRenderInfo, List<TextRenderFunction>> TEXT_RENDER_CACHE = new WeakHashMap<>();
    private static final Map<TextWrapInfo, List<String>> LISTED_CACHE = new WeakHashMap<>();

    CachedRGBFontRenderer(final GameSettings gameSettingsIn, final ResourceLocation location, final TextureManager textureManagerIn, final boolean unicode) {
        super(gameSettingsIn, location, textureManagerIn, unicode);
    }

    public static void overrideFontRenderer() {
        final Minecraft mc = Minecraft.getMinecraft();
        final CachedRGBFontRenderer renderer = new CachedRGBFontRenderer(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), mc.renderEngine, false);
        renderer.setUnicodeFlag(mc.fontRenderer.getUnicodeFlag());
        renderer.setBidiFlag(mc.fontRenderer.getBidiFlag());
        mc.fontRenderer = renderer;

        ((SimpleReloadableResourceManager) mc.getResourceManager()).registerReloadListener(renderer);
    }

    public static List<String> mapListedString(List<String> listed, LinkedList<String> strList, LinkedList<String> colors) {
        List<String> mapped = new ArrayList<>();

        StringBuilder mapping = new StringBuilder();

        int lastMappedLineIndex = 0;
        int lastMappedLineStrIndex = 0;
        for (int i = 0; i < listed.size(); i++) {
            final String s = listed.get(i);

            int lastMappedStrIndex = 0;
            StringBuilder sb = new StringBuilder();
            char[] charArray = s.toCharArray();
            for (int j = 0, strLen = charArray.length; j < strLen; j++) {
                final char ch = charArray[j];
                mapping.append(ch);

                String strFirst = strList.peekFirst();
                if (strFirst == null || colors.getFirst() == null) {
                    sb.append(ch);
                    continue;
                }

                if (strFirst.contentEquals(mapping) || (j + 1 >= strLen && strFirst.equals(mapping.toString() + ' '))) {
                    String color = colors.pollFirst();
                    sb.insert(lastMappedStrIndex, color);
                    sb.append(ch);

                    if (lastMappedLineIndex + 1 == i) {
                        StringBuilder lastMappedStrSb = new StringBuilder(mapped.get(lastMappedLineIndex));
                        lastMappedStrSb.insert(lastMappedLineStrIndex, color);
                        mapped.set(lastMappedLineIndex, lastMappedStrSb.toString());
                    }

                    lastMappedLineIndex = i;
                    lastMappedStrIndex = sb.length();
                    strList.pollFirst();
                    mapping.setLength(0);
                } else {
                    sb.append(ch);
                }
            }

            lastMappedLineStrIndex = lastMappedStrIndex;
            mapped.add(sb.toString());
        }

        return mapped;
    }

    /**
     * 如果有看不懂的代码，缓存就是终极的解决方案。
     */
    @Override
    public int drawString(@Nullable String text, float x, float y, int color, boolean dropShadow) {
        if (text == null) {
            return 0; // 如果文本为null，则直接返回0
        }
        TextRenderInfo textRenderInfo = new TextRenderInfo(text, color);
        x += 1; // 大概没问题

        List<TextRenderFunction> cachedRenderFunction = TEXT_RENDER_CACHE.get(textRenderInfo);
        if (cachedRenderFunction != null) {
            return fastDrawString(x, y, dropShadow, cachedRenderFunction);
        }

        cachedRenderFunction = new LinkedList<>();

        // 将文本拆分为字符串和相应的 RGB 设置
        List<Tuple<String, RGBSettings>> settings = RGBSettings.split(text);
        int currentColorHex = color; // 初始颜色为传递的颜色参数
        this.posX = x; // 设置 X 坐标起始位置

        for (final Tuple<String, RGBSettings> setting : settings) {
            String s = setting.getFirst(); // 获取字符串
            RGBSettings set = setting.getSecond(); // 获取 RGB 设置

            // 如果 RGB 设置中指定了固定颜色，使用该颜色绘制整个字符串
            if (set.isFixedColor()) {
                currentColorHex = Optional.ofNullable(set.getColorAt(0)).map(IColor::toInt).orElse(color);

                Color currentColor = new Color(currentColorHex);
                final int finalCurrentColor = new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), color >> 24 & 255).getRGB();
                final String toRender = set.getFormatString() + s;
                cachedRenderFunction.add((_y, _dropShadow) -> super.drawString(toRender, this.posX, _y, finalCurrentColor, _dropShadow));
                continue;
            }

            // 如果 RGB 设置中没有指定固定颜色，为每个字符分别指定颜色并绘制
            for (int i = 0; i < s.length(); ++i) {
                currentColorHex = Optional.ofNullable(set.getColorAt(i)).map(IColor::toInt).orElse(color);

                Color currentColor = new Color(currentColorHex);
                final int finalCurrentColor = new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), color >> 24 & 255).getRGB();
                final String toRender = set.getFormatString() + s.charAt(i);
                cachedRenderFunction.add((_y, _dropShadow) -> super.drawString(toRender, this.posX, _y, finalCurrentColor, _dropShadow));
            }
        }

        TEXT_RENDER_CACHE.put(textRenderInfo, cachedRenderFunction);
        return fastDrawString(x, y, dropShadow, cachedRenderFunction); // 返回 X 坐标的最终位置
    }

    public int fastDrawString(float x, float y, boolean dropShadow, final List<TextRenderFunction> renderFunctions) {
        this.posX = x;

        for (final TextRenderFunction renderFunction : renderFunctions) {
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

        StringBuilder sb = new StringBuilder();
        for (Tuple<String, RGBSettings> stringRGBSettingsTuple : RGBSettings.split(text)) {
            String first = stringRGBSettingsTuple.getFirst();
            RGBSettings settings = stringRGBSettingsTuple.getSecond();
            sb.append("§r"); // 先加重置代码，不然会随上一段变成粗体，使用粗体计算宽度
            sb.append(settings.getFormatString()); // 再添加原有格式代码
            sb.append(first); // 最后添加文字
        }
        return super.getStringWidth(sb.toString());
    }

    @Nonnull
    @Override
    public List<String> listFormattedStringToWidth(@Nonnull final String content, final int wrapWidth) {
        TextWrapInfo wrapInfo = new TextWrapInfo(content, wrapWidth);
        List<String> cachedListed = LISTED_CACHE.get(wrapInfo);
        if (cachedListed != null) {
            return cachedListed;
        }

        StringBuilder sb = new StringBuilder();

        LinkedList<String> strList = new LinkedList<>();
        LinkedList<String> rgbSettingList = new LinkedList<>();

        for (Tuple<String, String> strSettings : RGBSettingsUtils.splitRGBAndContents(content)) {
            rgbSettingList.add(strSettings.getFirst());
            String str = strSettings.getSecond();
            sb.append(str);
            strList.add(str.replace("\n", ""));
        }

        cachedListed = mapListedString(super.listFormattedStringToWidth(sb.toString(), wrapWidth), strList, rgbSettingList);
        LISTED_CACHE.put(wrapInfo, cachedListed);
        return cachedListed;
    }
}
