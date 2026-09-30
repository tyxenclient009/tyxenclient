/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10799
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 *  net.minecraft.class_429
 *  net.minecraft.class_437
 *  net.minecraft.class_500
 *  net.minecraft.class_526
 *  net.minecraft.class_5348
 */
package com.swiftclient.gui.screens;

import com.swiftclient.gui.Sprites;
import com.swiftclient.gui.SwiftAccountsScreen;
import com.swiftclient.gui.SwiftClickGui;
import com.swiftclient.util.SwiftText;
import com.swiftclient.util.Ui;
import com.swiftclient.util.UiScale;
import net.minecraft.class_10799;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_429;
import net.minecraft.class_437;
import net.minecraft.class_500;
import net.minecraft.class_526;
import net.minecraft.class_5348;

public final class SwiftMainMenuScreen
extends class_437 {
    private static final class_2960 BACKGROUND = class_2960.method_60655((String)"swiftclient", (String)"textures/gui/background.png");
    private static final class_2960 TILE_CLIENT = class_2960.method_60655((String)"swiftclient", (String)"textures/gui/icons/tile_grid_10.png");
    private static final class_2960 TILE_OPTIONS = class_2960.method_60655((String)"swiftclient", (String)"textures/gui/icons/tile_settings_10.png");
    private static final class_2960 TILE_QUIT = class_2960.method_60655((String)"swiftclient", (String)"textures/gui/icons/tile_power_10.png");
    private static final class_2960 TILE_USER = class_2960.method_60655((String)"swiftclient", (String)"textures/gui/icons/icon_user_10.png");
    private static final int BTN_W = 270;
    private static final int BTN_H = 24;
    private static final int BTN_GAP = 8;
    private static final String[] LABELS = new String[]{"SINGLEPLAYER", "MULTIPLAYER", "CLIENT"};
    private static final int TILE = 16;
    private long openedAt;
    private float anim;

    public SwiftMainMenuScreen() {
        super((class_2561)class_2561.method_43470((String)"Tyxen X Swift Client"));
    }

    protected void method_25426() {
        this.openedAt = System.nanoTime();
        this.anim = 0.0f;
    }

    private static float ease(float f) {
        float f2 = 1.0f - f;
        return 1.0f - f2 * f2 * f2;
    }

    public boolean method_25421() {
        return false;
    }

    public boolean method_25422() {
        return false;
    }

    private int heroY() {
        return this.field_22790 / 2 - 120 + (int)((1.0f - this.anim) * 16.0f);
    }

    private int btnY(int n) {
        return this.heroY() + 88 + n * 32;
    }

    private boolean inBtn(int n, int n2, int n3) {
        int n4 = this.field_22789 / 2 - 135;
        int n5 = this.btnY(n3);
        return n >= n4 && n < n4 + 270 && n2 >= n5 && n2 < n5 + 24;
    }

    private int iconIndex(int n, int n2) {
        int n3 = 18;
        int n4 = 8;
        int n5 = 3 * (n3 + n4) - n4;
        int n6 = this.field_22789 / 2 - n5 / 2;
        int n7 = this.field_22790 - 38;
        if (n2 < n7 || n2 >= n7 + n3) {
            return -1;
        }
        int n8 = (n - n6) / (n3 + n4);
        if (n8 < 0 || n8 > 2) {
            return -1;
        }
        int n9 = n6 + n8 * (n3 + n4);
        return n >= n9 && n < n9 + n3 ? n8 : -1;
    }

    private int cornerAt(int n, int n2) {
        if (n >= this.field_22789 - 10 - 16 && n < this.field_22789 - 10 && n2 >= 10 && n2 < 26) {
            return 0;
        }
        if (n >= this.field_22789 - 10 - 16 && n < this.field_22789 - 10 && n2 >= this.field_22790 - 10 - 16 && n2 < this.field_22790 - 10) {
            return 1;
        }
        if (n >= 10 && n < 26 && n2 >= 10 && n2 < 26) {
            return 2;
        }
        return -1;
    }

    public boolean method_25402(class_11909 class_119092, boolean bl) {
        int n;
        int n2;
        if (class_119092.method_74245() != 0) {
            return false;
        }
        int n3 = UiScale.unX((int)class_119092.comp_4798(), this.field_22789, this.field_22790);
        int n4 = this.cornerAt(n3, n2 = UiScale.unY((int)class_119092.comp_4799(), this.field_22789, this.field_22790));
        if (n4 == 0) {
            this.field_22787.method_1507((class_437)new class_429((class_437)this, this.field_22787.field_1690));
            return true;
        }
        if (n4 == 1) {
            this.field_22787.method_1592();
            return true;
        }
        if (n4 == 2) {
            this.field_22787.method_1507((class_437)new SwiftAccountsScreen(this));
            return true;
        }
        for (n = 0; n < LABELS.length; ++n) {
            if (!this.inBtn(n3, n2, n)) continue;
            this.press(n);
            return true;
        }
        n = this.iconIndex(n3, n2);
        if (n == 0) {
            this.field_22787.method_1507((class_437)new SwiftClickGui());
            return true;
        }
        if (n == 1) {
            this.field_22787.method_1507((class_437)new class_429((class_437)this, this.field_22787.field_1690));
            return true;
        }
        if (n == 2) {
            this.field_22787.method_1592();
            return true;
        }
        return false;
    }

    private void press(int n) {
        if (n == 0) {
            this.field_22787.method_1507((class_437)new class_526((class_437)this));
        } else if (n == 1) {
            this.field_22787.method_1507((class_437)new class_500((class_437)this));
        } else {
            this.field_22787.method_1507((class_437)new SwiftClickGui());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void method_25394(class_332 class_3322, int n, int n2, float f) {
        this.anim = SwiftMainMenuScreen.ease(Math.min(1.0f, (float)(System.nanoTime() - this.openedAt) / 2.2E8f));
        int n3 = n;
        int n4 = n2;
        n = UiScale.unX(n, this.field_22789, this.field_22790);
        n2 = UiScale.unY(n2, this.field_22789, this.field_22790);
        float f2 = Math.max((float)this.field_22789 / 1440.0f, (float)this.field_22790 / 615.0f);
        int n5 = (int)((float)this.field_22789 / f2);
        int n6 = (int)((float)this.field_22790 / f2);
        float f3 = (float)(1440 - n5) / 2.0f;
        float f4 = (float)(615 - n6) / 2.0f;
        class_3322.method_25302(class_10799.field_56883, BACKGROUND, 0, 0, f3, f4, this.field_22789, this.field_22790, n5, n6, 1440, 615);
        class_3322.method_25294(0, 0, this.field_22789, this.field_22790, 1845955350);
        UiScale.begin(class_3322, this.field_22789, this.field_22790);
        try {
            Object object;
            int n7;
            int n8;
            int n9;
            int n10;
            int n11 = this.field_22789 / 2;
            int n12 = this.heroY();
            Ui.crescent(class_3322, n11, n12, 24, -723718);
            Ui.spacedText(class_3322, this.field_22793, "TYXEN X SWIFT", n11, n12 + 36, -855305, 3);
            for (n10 = 0; n10 < LABELS.length; ++n10) {
                n9 = n11 - 135;
                n8 = this.btnY(n10);
                n7 = this.inBtn(n, n2, n10);
                if (n10 == 2) {
                    Ui.sprite(class_3322, n7 != 0 ? Sprites.BTN_RED_HOVER : Sprites.BTN_RED, n9, n8, 270, 24);
                } else {
                    Ui.sprite(class_3322, n7 != 0 ? Sprites.BTN_HOVER : Sprites.BTN, n9, n8, 270, 24);
                }
                class_3322.method_25300(this.field_22793, LABELS[n10], n11, n8 + 8, -855305);
            }
            n10 = 18;
            n9 = 8;
            n8 = 3 * (n10 + n9) - n9;
            n7 = n11 - n8 / 2;
            int n13 = this.field_22790 - 38;
            int n14 = this.iconIndex(n, n2);
            class_2960[] class_2960Array = new class_2960[]{TILE_CLIENT, TILE_OPTIONS, TILE_QUIT};
            for (int i = 0; i < 3; ++i) {
                int n15 = n7 + i * (n10 + n9);
                Ui.sprite(class_3322, n14 == i ? Sprites.TILE18_HOVER : Sprites.TILE18, n15, n13, n10, n10);
                class_3322.method_25290(class_10799.field_56883, class_2960Array[i], n15 + 4, n13 + 4, 0.0f, 0.0f, 10, 10, 10, 10);
            }
            int[][] nArrayArray = new int[][]{{10, 10}, {this.field_22789 - 10 - 16, 10}, {10, this.field_22790 - 10 - 16}, {this.field_22789 - 10 - 16, this.field_22790 - 10 - 16}};
            class_2960[] class_2960Array2 = new class_2960[]{TILE_USER, TILE_OPTIONS, null, TILE_QUIT};
            int n16 = this.cornerAt(n, n2);
            for (int i = 0; i < 4; ++i) {
                object = nArrayArray[i];
                boolean bl = i == 0 && n16 == 2 || i == 1 && n16 == 0 || i == 3 && n16 == 1;
                Ui.sprite(class_3322, bl ? Sprites.TILE16_HOVER : Sprites.TILE16, object[0], object[1], 16, 16);
                if (class_2960Array2[i] == null) continue;
                class_3322.method_25290(class_10799.field_56883, class_2960Array2[i], object[0] + 3, object[1] + 3, 0.0f, 0.0f, 10, 10, 10, 10);
            }
            String string = this.field_22787.method_1548().method_1676();
            if (string != null && !string.isEmpty()) {
                class_3322.method_27535(this.field_22793, SwiftText.of(string), 34, 14, -4605239);
            }
            class_3322.method_27535(this.field_22793, SwiftText.of("Tyxen X Swift Client"), 12, this.field_22790 - 22, -7697506);
            class_3322.method_27535(this.field_22793, SwiftText.of("by PixelEatsPancake  \u2022  Horizon Studios"), 12, this.field_22790 - 12, -10592396);
            object = "Not affiliated with Mojang or Lunar Client";
            class_3322.method_27535(this.field_22793, SwiftText.of((String)object), this.field_22789 - this.field_22793.method_27525((class_5348)SwiftText.of((String)object)) - 12, this.field_22790 - 12, -10592396);
        }
        finally {
            UiScale.end(class_3322);
        }
        super.method_25394(class_3322, n3, n4, f);
    }
}

