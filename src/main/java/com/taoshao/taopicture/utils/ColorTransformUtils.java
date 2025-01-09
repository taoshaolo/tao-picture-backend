package com.taoshao.taopicture.utils;

/**
 * 颜色转换工具类
 *
 * @Author taoshao
 * @Date 2025/1/9
 */
public class ColorTransformUtils {

    private ColorTransformUtils() {
    }

    /**
     * 获取标准颜色（将数据万象的 5 位色值转为 6 位）
     *
     * @param color
     * @return
     */
    public static String getStandardColor(String color) {
        if (color.length() == 7) {
            color = color.substring(0, 4) + "0" + color.substring(4, 7);
        }
        return color;
    }

}
