/*
 * Decompiled with CFR 0.152.
 */
package com.swiftclient.config;

import com.swiftclient.config.ClientConfig;

public final class Theme {
    private static int accent = -12872002;
    public static final int[] ACCENTS = new int[]{-12872002, -12861868, -1666755, -4702746, -2408384};
    public static final int BACKGROUND = -16053490;
    public static final int SIDEBAR = -401797872;
    public static final int PANEL = -15592938;
    public static final int PANEL_LIGHT = -14935006;
    public static final int BORDER = -13027015;
    public static final int TEXT = -855308;
    public static final int TEXT_DIM = -7697776;
    public static final int HUD_BG = -16448249;
    public static final int GREEN = -12861868;
    public static final int RED = -2408384;

    private Theme() {
    }

    public static int accent() {
        return accent;
    }

    public static void load(int color) {
        accent = color;
    }

    public static void setAccent(int color) {
        accent = color;
        ClientConfig.setTheme(color);
    }

    public static void cycleAccent() {
        Theme.setAccent(ACCENTS[(Theme.accIndex() + 1) % ACCENTS.length]);
    }

    public static int accIndex() {
        for (int i = 0; i < ACCENTS.length; ++i) {
            if (ACCENTS[i] != accent) continue;
            return i;
        }
        return 0;
    }
}

