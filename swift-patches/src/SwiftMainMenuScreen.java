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

    private static float ease(float x) {
        float inv = 1.0f - x;
        return 1.0f - inv * inv * inv;
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

    private int btnY(int i) {
        return this.heroY() + 88 + i * 32;
    }

    private boolean inBtn(int mx, int my, int i) {
        int x = this.field_22789 / 2 - 135;
        int y = this.btnY(i);
        return mx >= x && mx < x + 270 && my >= y && my < y + 24;
    }

    private int iconIndex(int mx, int my) {
        int s = 18;
        int gap = 8;
        int total = 3 * (s + gap) - gap;
        int x0 = this.field_22789 / 2 - total / 2;
        int y0 = this.field_22790 - 38;
        if (my < y0 || my >= y0 + s) {
            return -1;
        }
        int i = (mx - x0) / (s + gap);
        if (i < 0 || i > 2) {
            return -1;
        }
        int lx = x0 + i * (s + gap);
        return mx >= lx && mx < lx + s ? i : -1;
    }

    private int cornerAt(int mx, int my) {
        if (mx >= this.field_22789 - 10 - 16 && mx < this.field_22789 - 10 && my >= 10 && my < 26) {
            return 0;
        }
        if (mx >= this.field_22789 - 10 - 16 && mx < this.field_22789 - 10 && my >= this.field_22790 - 10 - 16 && my < this.field_22790 - 10) {
            return 1;
        }
        if (mx >= 10 && mx < 26 && my >= 10 && my < 26) {
            return 2;
        }
        return -1;
    }

    public boolean method_25402(class_11909 click, boolean doubleClick) {
        int my;
        if (click.method_74245() != 0) {
            return false;
        }
          int mx = UiScale.unX((int)click.comp_4798(), this.field_22789, this.field_22790);
          int corner = this.cornerAt(mx, my = UiScale.unY((int)click.comp_4799(), this.field_22789, this.field_22790));
        if (corner == 0) {
            this.field_22787.method_1507((class_437)new class_429((class_437)this, this.field_22787.field_1690));
            return true;
        }
        if (corner == 1) {
            this.field_22787.method_1592();
            return true;
        }
        if (corner == 2) {
            this.field_22787.method_1507((class_437)new SwiftAccountsScreen(this));
            return true;
        }
        for (int i = 0; i < LABELS.length; ++i) {
            if (!this.inBtn(mx, my, i)) continue;
            this.press(i);
            return true;
        }
        int icon = this.iconIndex(mx, my);
        if (icon == 0) {
            this.field_22787.method_1507((class_437)new SwiftClickGui());
            return true;
        }
        if (icon == 1) {
            this.field_22787.method_1507((class_437)new class_429((class_437)this, this.field_22787.field_1690));
            return true;
        }
        if (icon == 2) {
            this.field_22787.method_1592();
            return true;
        }
        return false;
    }

    private void press(int i) {
        if (i == 0) {
            this.field_22787.method_1507((class_437)new class_526((class_437)this));
        } else if (i == 1) {
            this.field_22787.method_1507((class_437)new class_500((class_437)this));
        } else {
            this.field_22787.method_1507((class_437)new SwiftClickGui());
        }
    }

    public void method_25394(class_332 c, int mouseX, int mouseY, float delta) {
        this.anim = SwiftMainMenuScreen.ease(Math.min(1.0f, (float)(System.nanoTime() - this.openedAt) / 2.2E8f));
        int ox = mouseX;
        int oy = mouseY;
        mouseX = UiScale.unX(mouseX, this.field_22789, this.field_22790);
        mouseY = UiScale.unY(mouseY, this.field_22789, this.field_22790);
        float scale = Math.max((float)this.field_22789 / 1440.0f, (float)this.field_22790 / 615.0f);
        int rw = (int)((float)this.field_22789 / scale);
        int rh = (int)((float)this.field_22790 / scale);
        float u = (float)(1440 - rw) / 2.0f;
        float v = (float)(615 - rh) / 2.0f;
        c.method_25302(class_10799.field_56883, BACKGROUND, 0, 0, u, v, this.field_22789, this.field_22790, rw, rh, 1440, 615);
        c.method_25294(0, 0, this.field_22789, this.field_22790, 1845955350);
        UiScale.begin(c, this.field_22789, this.field_22790);
        try {
        int cx = this.field_22789 / 2;
        int hy = this.heroY();
        Ui.crescent(c, cx, hy, 24, -723718);
        Ui.spacedText(c, this.field_22793, "TYXEN X SWIFT", cx, hy + 36, -855305, 3);
        for (int i = 0; i < LABELS.length; ++i) {
            int x = cx - 135;
            int y = this.btnY(i);
            boolean hover = this.inBtn(mouseX, mouseY, i);
            if (i == 2) {
                Ui.sprite(c, hover ? Sprites.BTN_RED_HOVER : Sprites.BTN_RED, x, y, 270, 24);
            } else {
                Ui.sprite(c, hover ? Sprites.BTN_HOVER : Sprites.BTN, x, y, 270, 24);
            }
            c.method_25300(this.field_22793, LABELS[i], cx, y + 8, -855305);
        }
        int s = 18;
        int gap = 8;
        int total = 3 * (s + gap) - gap;
        int x0 = cx - total / 2;
        int y0 = this.field_22790 - 38;
        int hover = this.iconIndex(mouseX, mouseY);
        class_2960[] tiles = new class_2960[]{TILE_CLIENT, TILE_OPTIONS, TILE_QUIT};
        for (int i = 0; i < 3; ++i) {
            int x = x0 + i * (s + gap);
            Ui.sprite(c, hover == i ? Sprites.TILE18_HOVER : Sprites.TILE18, x, y0, s, s);
            c.method_25290(class_10799.field_56883, tiles[i], x + 4, y0 + 4, 0.0f, 0.0f, 10, 10, 10, 10);
        }
        int[][] corners = new int[][]{{10, 10}, {this.field_22789 - 10 - 16, 10}, {10, this.field_22790 - 10 - 16}, {this.field_22789 - 10 - 16, this.field_22790 - 10 - 16}};
        class_2960[] cornerIcons = new class_2960[]{TILE_USER, TILE_OPTIONS, null, TILE_QUIT};
        int hovCorner = this.cornerAt(mouseX, mouseY);
        for (int k = 0; k < 4; ++k) {
            int[] corner = corners[k];
            boolean hov = k == 0 && hovCorner == 2 || k == 1 && hovCorner == 0 || k == 3 && hovCorner == 1;
            Ui.sprite(c, hov ? Sprites.TILE16_HOVER : Sprites.TILE16, corner[0], corner[1], 16, 16);
            if (cornerIcons[k] == null) continue;
            c.method_25290(class_10799.field_56883, cornerIcons[k], corner[0] + 3, corner[1] + 3, 0.0f, 0.0f, 10, 10, 10, 10);
        }
        String user = this.field_22787.method_1548().method_1676();
        if (user != null && !user.isEmpty()) {
            c.method_27535(this.field_22793, SwiftText.of(user), 34, 14, -4605239);
        }
        c.method_27535(this.field_22793, SwiftText.of("Tyxen X Swift Client"), 12, this.field_22790 - 22, -7697506);
        c.method_27535(this.field_22793, SwiftText.of("by PixelEatsPancake  \u2022  Horizon Studios"), 12, this.field_22790 - 12, -10592396);
        String copy = "Not affiliated with Mojang or Lunar Client";
        c.method_27535(this.field_22793, SwiftText.of(copy), this.field_22789 - this.field_22793.method_27525((class_5348)SwiftText.of(copy)) - 12, this.field_22790 - 12, -10592396);
        } finally {
        UiScale.end(c);
        }
        super.method_25394(c, ox, oy, delta);
    }
}

