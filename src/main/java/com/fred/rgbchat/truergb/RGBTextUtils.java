package com.fred.rgbchat.truergb;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;

public final class RGBTextUtils {
    /**
     * 剥离文本中的所有 RGB 颜色代码（#RRGGBB 格式），保留 MC 原版格式代码（§x）。
     */
    @Nonnull
    public static String stripRGB(@Nullable String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder(str.length());
        Matcher matcher = RGBSettings.PATTERN.matcher(str);
        int index = 0;

        while (matcher.find(index)) {
            result.append(str, index, matcher.start());

            String format = matcher.group();
            if (format.startsWith("§")) {
                result.append(format);
            }

            index = matcher.end();
        }

        result.append(str.substring(index));
        return result.toString();
    }

    /**
     * 将 RGB 格式代码注入到按宽度换行后的文本行中。
     *
     * @param listed  原版按宽度换行后的文本行列表
     * @param strList 原始文本段队列
     * @param colors  序列化后的 RGB 设置队列（与 strList 一一对应）
     * @return 注入了 RGB 格式代码的文本行列表
     */
    @Nonnull
    public static List<String> mapListedString(List<String> listed, Deque<String> strList, Deque<String> colors) {
        List<String> mapped = new ArrayList<>(listed.size());
        String currentSegment = strList.pollFirst();
        String currentColor = colors.pollFirst();
        int segmentIndex = 0;

        for (String line : listed) {
            if (line.isEmpty()) {
                mapped.add("");
                continue;
            }

            StringBuilder sb = new StringBuilder(line.length() + 16);
            int lineIndex = 0;
            while (lineIndex < line.length()) {
                if (currentSegment == null || currentColor == null) {
                    sb.append(line.substring(lineIndex));
                    break;
                }

                while (lineIndex == 0 && segmentIndex < currentSegment.length() && currentSegment.charAt(segmentIndex) == ' ') {
                    segmentIndex++;
                }

                if (segmentIndex >= currentSegment.length()) {
                    currentSegment = strList.pollFirst();
                    currentColor = colors.pollFirst();
                    segmentIndex = 0;
                    continue;
                }

                sb.append(currentColor);
                while (lineIndex < line.length() && segmentIndex < currentSegment.length()) {
                    char expected = currentSegment.charAt(segmentIndex);
                    char actual = line.charAt(lineIndex);
                    if (expected != actual) {
                        break;
                    }

                    sb.append(actual);
                    lineIndex++;
                    segmentIndex++;
                }

                if (segmentIndex >= currentSegment.length()) {
                    currentSegment = strList.pollFirst();
                    currentColor = colors.pollFirst();
                    segmentIndex = 0;
                }
            }

            mapped.add(sb.toString());
        }

        return mapped;
    }
}
