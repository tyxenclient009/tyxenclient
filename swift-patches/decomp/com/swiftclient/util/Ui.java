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
    private static final class_2960 MENU_BG = class_2960.method_60655((String)"swiftclient", (String)"textures/gui/background.png");

    private Ui() {
    }

    public static void menuBg(class_332 class_3322, int n, int n2) {
        float f = Math.max((float)n / 1440.0f, (float)n2 / 615.0f);
        int n3 = (int)((float)n / f);
        int n4 = (int)((float)n2 / f);
        float f2 = (float)(1440 - n3) / 2.0f;
        float f3 = (float)(615 - n4) / 2.0f;
        class_3322.method_25302(class_10799.field_56883, MENU_BG, 0, 0, f2, f3, n, n2, n3, n4, 1440, 615);
        class_3322.method_25294(0, 0, n, n2, 1845955350);
    }

    public static void sprite(class_332 class_3322, class_2960 class_29602, int n, int n2, int n3, int n4) {
        class_3322.method_52706(class_10799.field_56883, class_29602, n, n2, n3, n4);
    }

    public static void rect(class_332 class_3322, int n, int n2, int n3, int n4, int n5) {
        class_3322.method_25294(n, n2, n + n3, n2 + n4, n5);
    }

    public static void outline(class_332 class_3322, int n, int n2, int n3, int n4, int n5) {
        class_3322.method_25294(n, n2, n + n3, n2 + 1, n5);
        class_3322.method_25294(n, n2 + n4 - 1, n + n3, n2 + n4, n5);
        class_3322.method_25294(n, n2, n + 1, n2 + n4, n5);
        class_3322.method_25294(n + n3 - 1, n2, n + n3, n2 + n4, n5);
    }

    public static void rounded(class_332 class_3322, int n, int n2, int n3, int n4, int n5, int n6, int n7) {
        class_3322.method_25294(n, n2, n + n3, n2 + n4, n6);
        if (n5 > 0) {
            Ui.corner(class_3322, n, n2, 1, 1, n5, n7);
            Ui.corner(class_3322, n + n3 - 1, n2, -1, 1, n5, n7);
            Ui.corner(class_3322, n, n2 + n4 - 1, 1, -1, n5, n7);
            Ui.corner(class_3322, n + n3 - 1, n2 + n4 - 1, -1, -1, n5, n7);
        }
    }

    private static void corner(class_332 class_3322, int n, int n2, int n3, int n4, int n5, int n6) {
        for (int i = 0; i < n5; ++i) {
            int n7 = n5 - 1 - i;
            class_3322.method_25294(n + n3 * i, n2 + n4 * n7, n + n3 * i + 1, n2 + n4 * n7 + 1, n6);
        }
    }

    public static void roundedOutline(class_332 class_3322, int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8) {
        Ui.rounded(class_3322, n, n2, n3, n4, n5, n6, n8);
        if (n3 > 2 && n4 > 2) {
            Ui.rounded(class_3322, n + 1, n2 + 1, n3 - 2, n4 - 2, Math.max(0, n5 - 1), n7, n6);
        }
    }

    public static void toggle(class_332 class_3322, int n, int n2, int n3, int n4, boolean bl) {
        Ui.sprite(class_3322, bl ? Sprites.SW_ON : Sprites.SW_OFF, n, n2, n3, n4);
        int n5 = n4 - 4;
        int n6 = bl ? n + n3 - n5 - 2 : n + 2;
        Ui.sprite(class_3322, Sprites.SW_KNOB, n6, n2 + 2, n5, n5);
    }

    public static int slider(class_332 class_3322, int n, int n2, int n3, int n4, int n5, int n6) {
        int n7 = n2 + 11;
        class_3322.method_25294(n, n7, n + n3, n7 + 2, -14013382);
        float f = (float)(n4 - n5) / (float)Math.max(1, n6 - n5);
        int n8 = n + (int)(f * (float)(n3 - 10));
        Ui.sprite(class_3322, Sprites.KNOB, n8, n7 - 4, 10, 10);
        return n8 + 5;
    }

    public static void circle(class_332 class_3322, int n, int n2, int n3, int n4) {
        for (int i = -n3; i <= n3; ++i) {
            int n5 = (int)Math.sqrt(n3 * n3 - i * i);
            class_3322.method_25294(n - n5, n2 + i, n + n5 + 1, n2 + i + 1, n4);
        }
    }

    public static void ring(class_332 class_3322, int n, int n2, int n3, int n4, int n5) {
        for (int i = -n3; i <= n3; ++i) {
            int n6 = (int)Math.sqrt(n3 * n3 - i * i);
            int n7 = (n3 - n4) * (n3 - n4) - i * i;
            int n8 = n7 > 0 ? (int)Math.sqrt(n7) : -1;
            int n9 = n8;
            if (n8 >= n6) continue;
            class_3322.method_25294(n - n6, n2 + i, n - n8, n2 + i + 1, n5);
            class_3322.method_25294(n + n8 + 1, n2 + i, n + n6 + 1, n2 + i + 1, n5);
        }
    }

    public static void crescent(class_332 class_3322, int n, int n2, int n3, int n4) {
        Ui.ring(class_3322, n, n2, n3, 3, n4);
        int n5 = n3 - 6;
        int n6 = n + n5 / 2;
        int n7 = n2 - n5 / 4;
        int n8 = n5 - 2;
        for (int i = -n5; i <= n5; ++i) {
            int n9 = (int)Math.sqrt(n5 * n5 - i * i);
            for (int j = -n9; j <= n9; ++j) {
                int n10 = n + j - n6;
                int n11 = n2 + i - n7;
                if (n10 * n10 + n11 * n11 <= n8 * n8) continue;
                class_3322.method_25294(n + j, n2 + i, n + j + 1, n2 + i + 1, n4);
            }
        }
    }

    public static void spacedText(class_332 class_3322, class_327 class_3272, String string, int n, int n2, int n3, int n4) {
        int n5 = class_3272.method_27525((class_5348)SwiftText.bold(string)) + n4 * (string.length() - 1);
        int n6 = n - n5 / 2;
        for (int i = 0; i < string.length(); ++i) {
            String string2 = string.substring(i, i + 1);
            class_3322.method_27535(class_3272, SwiftText.bold(string2), n6, n2, n3);
            n6 += class_3272.method_27525((class_5348)SwiftText.bold(string2)) + n4;
        }
    }
}
