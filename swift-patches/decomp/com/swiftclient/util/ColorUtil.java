/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package com.swiftclient.util;

import net.minecraft.class_332;

public final class ColorUtil {
    private ColorUtil() {
    }

    public static int withAlpha(int rgb, int a) {
        return a << 24 | rgb & 0xFFFFFF;
    }

    public static int alpha(int color) {
        return color >>> 24 & 0xFF;
    }

    public static int red(int color) {
        return color >> 16 & 0xFF;
    }

    public static int green(int color) {
        return color >> 8 & 0xFF;
    }

    public static int blue(int color) {
        return color & 0xFF;
    }

    public static int rgb(int r, int g, int b) {
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    public static int mix(int c1, int c2, float t) {
        if (t <= 0.0f) {
            return c1;
        }
        if (t >= 1.0f) {
            return c2;
        }
        int a = ColorUtil.mix(ColorUtil.alpha(c1), ColorUtil.alpha(c2), t);
        int r = ColorUtil.mix(ColorUtil.red(c1), ColorUtil.red(c2), t);
        int g = ColorUtil.mix(ColorUtil.green(c1), ColorUtil.green(c2), t);
        int b = ColorUtil.mix(ColorUtil.blue(c1), ColorUtil.blue(c2), t);
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static int shade(int color, float factor) {
        return ColorUtil.rgb((int)Math.min(255.0f, (float)ColorUtil.red(color) * factor), (int)Math.min(255.0f, (float)ColorUtil.green(color) * factor), (int)Math.min(255.0f, (float)ColorUtil.blue(color) * factor));
    }

    public static void gradient(class_332 c, int x1, int y1, int x2, int y2, int top, int bottom) {
        c.method_25296(x1, y1, x2, y2, top, bottom);
    }

    private static float mix(float a, float b, float t) {
        return a + (b - a) * t;
    }
}

