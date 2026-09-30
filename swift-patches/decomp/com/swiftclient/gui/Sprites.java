/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2960
 */
package com.swiftclient.gui;

import net.minecraft.class_2960;

public final class Sprites {
    public static final class_2960 PANEL = Sprites.id("panel");
    public static final class_2960 CARD = Sprites.id("card");
    public static final class_2960 BTN = Sprites.id("btn");
    public static final class_2960 BTN_HOVER = Sprites.id("btn_hover");
    public static final class_2960 BTN_RED = Sprites.id("btn_red");
    public static final class_2960 BTN_RED_HOVER = Sprites.id("btn_red_hover");
    public static final class_2960 BTN_BLUE = Sprites.id("btn_blue");
    public static final class_2960 BTN_BLUE_HOVER = Sprites.id("btn_blue_hover");
    public static final class_2960 PILL_GREEN = Sprites.id("pill_green");
    public static final class_2960 PILL_OFF = Sprites.id("pill_off");
    public static final class_2960 SW_ON = Sprites.id("sw_on");
    public static final class_2960 SW_OFF = Sprites.id("sw_off");
    public static final class_2960 SW_KNOB = Sprites.id("sw_knob");
    public static final class_2960 TILE = Sprites.id("tile");
    public static final class_2960 TILE_HOVER = Sprites.id("tile_hover");
    public static final class_2960 TILE16 = Sprites.id("tile16");
    public static final class_2960 TILE16_HOVER = Sprites.id("tile16_hover");
    public static final class_2960 TILE18 = Sprites.id("tile18");
    public static final class_2960 TILE18_HOVER = Sprites.id("tile18_hover");
    public static final class_2960 TILE22 = Sprites.id("tile22");
    public static final class_2960 TILE22_HOVER = Sprites.id("tile22_hover");
    public static final class_2960 CHIP_DARK = Sprites.id("chip_dark");
    public static final class_2960 TAB_SEL = Sprites.id("tab_sel");
    public static final class_2960 OPT = Sprites.id("opt");
    public static final class_2960 KNOB = Sprites.id("knob");

    private Sprites() {
    }

    public static class_2960 id(String name) {
        return class_2960.method_60655((String)"swiftclient", (String)name);
    }

    public static class_2960 chipAccent(int i) {
        return Sprites.id("chip_accent_" + Math.max(0, Math.min(4, i)));
    }

    public static class_2960 cardHover(int i) {
        return Sprites.id("card_hover_" + Math.max(0, Math.min(4, i)));
    }
}

