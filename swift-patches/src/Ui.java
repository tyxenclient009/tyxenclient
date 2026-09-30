/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10799
 *  net.minecraft.class_2960
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_5348
 */
package com.swiftclient.util;

import com.swiftclient.gui.Sprites;
import com.swiftclient.util.SwiftText;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_5348;

public final class Ui {
    private Ui() {
    }

    private static final class_2960 MENU_BG = class_2960.method_60655((String)"swiftclient", (String)"textures/gui/background.png");

    /**
     * TYX addition — shared menu backdrop: the main-menu artwork,
     * cover-cropped to any screen, under a readability dim. Same look as
     * SwiftMainMenuScreen, reused by every list/settings/pause screen.
     */
    public static void menuBg(class_332 c, int w, int h) {
        float scale = Math.max((float)w / 1440.0f, (float)h / 615.0f);
        int rw = (int)((float)w / scale);
        int rh = (int)((float)h / scale);
        float u = (float)(1440 - rw) / 2.0f;
        float v = (float)(615 - rh) / 2.0f;
        c.method_25302(class_10799.field_56883, MENU_BG, 0, 0, u, v, w, h, rw, rh, 1440, 615);
        c.method_25294(0, 0, w, h, 1845955350);
    }

    public static void sprite(class_332 c, class_2960 id, int x, int y, int w, int h) {
        c.method_52706(class_10799.field_56883, id, x, y, w, h);
    }

    public static void rect(class_332 c, int x, int y, int w, int h, int color) {
        c.method_25294(x, y, x + w, y + h, color);
    }

    public static void outline(class_332 c, int x, int y, int w, int h, int color) {
        c.method_25294(x, y, x + w, y + 1, color);
        c.method_25294(x, y + h - 1, x + w, y + h, color);
        c.method_25294(x, y, x + 1, y + h, color);
        c.method_25294(x + w - 1, y, x + w, y + h, color);
    }

    public static void rounded(class_332 c, int x, int y, int w, int h, int r, int color, int ambient) {
        c.method_25294(x, y, x + w, y + h, color);
        if (r > 0) {
            Ui.corner(c, x, y, 1, 1, r, ambient);
            Ui.corner(c, x + w - 1, y, -1, 1, r, ambient);
            Ui.corner(c, x, y + h - 1, 1, -1, r, ambient);
            Ui.corner(c, x + w - 1, y + h - 1, -1, -1, r, ambient);
        }
    }

    private static void corner(class_332 c, int x, int y, int sx, int sy, int r, int ambient) {
        for (int i = 0; i < r; ++i) {
            int j = r - 1 - i;
            c.method_25294(x + sx * i, y + sy * j, x + sx * i + 1, y + sy * j + 1, ambient);
        }
    }

    public static void roundedOutline(class_332 c, int x, int y, int w, int h, int r, int border, int inner, int backdrop) {
        Ui.rounded(c, x, y, w, h, r, border, backdrop);
        if (w > 2 && h > 2) {
            Ui.rounded(c, x + 1, y + 1, w - 2, h - 2, Math.max(0, r - 1), inner, border);
        }
    }

    public static void toggle(class_332 c, int x, int y, int w, int h, boolean on) {
        Ui.sprite(c, on ? Sprites.SW_ON : Sprites.SW_OFF, x, y, w, h);
        int size = h - 4;
        int kx = on ? x + w - size - 2 : x + 2;
        Ui.sprite(c, Sprites.SW_KNOB, kx, y + 2, size, size);
    }

    public static int slider(class_332 c, int x, int y, int w, int value, int min, int max) {
        int ty = y + 11;
        c.method_25294(x, ty, x + w, ty + 2, -14013382);
        float t = (float)(value - min) / (float)Math.max(1, max - min);
        int kx = x + (int)(t * (float)(w - 10));
        Ui.sprite(c, Sprites.KNOB, kx, ty - 4, 10, 10);
        return kx + 5;
    }

    public static void circle(class_332 c, int cx, int cy, int r, int color) {
        for (int y = -r; y <= r; ++y) {
            int dx = (int)Math.sqrt(r * r - y * y);
            c.method_25294(cx - dx, cy + y, cx + dx + 1, cy + y + 1, color);
        }
    }

    public static void ring(class_332 c, int cx, int cy, int r, int t, int color) {
        for (int y = -r; y <= r; ++y) {
            int dxIn;
            int dx = (int)Math.sqrt(r * r - y * y);
            int inner = (r - t) * (r - t) - y * y;
            int n = dxIn = inner > 0 ? (int)Math.sqrt(inner) : -1;
            if (dxIn >= dx) continue;
            c.method_25294(cx - dx, cy + y, cx - dxIn, cy + y + 1, color);
            c.method_25294(cx + dxIn + 1, cy + y, cx + dx + 1, cy + y + 1, color);
        }
    }

    public static void crescent(class_332 c, int cx, int cy, int r, int color) {
        Ui.ring(c, cx, cy, r, 3, color);
        int inR = r - 6;
        int ox = cx + inR / 2;
        int oy = cy - inR / 4;
        int cutR = inR - 2;
        for (int y = -inR; y <= inR; ++y) {
            int dx = (int)Math.sqrt(inR * inR - y * y);
            for (int x = -dx; x <= dx; ++x) {
                int ddx = cx + x - ox;
                int ddy = cy + y - oy;
                if (ddx * ddx + ddy * ddy <= cutR * cutR) continue;
                c.method_25294(cx + x, cy + y, cx + x + 1, cy + y + 1, color);
            }
        }
    }

    public static void spacedText(class_332 c, class_327 tr, String text, int cx, int y, int color, int spacing) {
        int total = tr.method_27525((class_5348)SwiftText.bold(text)) + spacing * (text.length() - 1);
        int x = cx - total / 2;
        for (int i = 0; i < text.length(); ++i) {
            String ch = text.substring(i, i + 1);
            c.method_27535(tr, SwiftText.bold(ch), x, y, color);
            x += tr.method_27525((class_5348)SwiftText.bold(ch)) + spacing;
        }
    }
}

