/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10799
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package com.swiftclient.gui;

import com.swiftclient.gui.Sprites;
import com.swiftclient.gui.SwiftAccountsScreen;
import com.swiftclient.gui.SwiftHudEditor;
import com.swiftclient.gui.SwiftModsMenu;
import com.swiftclient.util.SwiftText;
import com.swiftclient.util.Ui;
import net.minecraft.class_10799;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_437;

public final class SwiftClickGui
extends class_437 {
    private static final int BTN_W = 120;
    private static final int BTN_H = 22;
    private static final int TILE = 22;
    private static final class_2960 TILE_SETTINGS = class_2960.method_60655((String)"swiftclient", (String)"textures/gui/icons/tile_settings_12.png");
    private long openedAt;
    private float anim;

    public SwiftClickGui() {
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

    private int groupY() {
        return (int)((double)this.field_22790 * 0.44) + (int)((1.0f - this.anim) * 14.0f);
    }

    /**
     * TYX patch — adaptive UI scale: the menu is authored at ~640x400, so on
     * small windows / huge GUI scales it fills the whole screen. Shrink the
     * whole group toward screen center (never enlarge, never below half).
     */
    private float uiScale() {
        float s = Math.min(this.field_22789 / 640.0f, this.field_22790 / 400.0f);
        if (s > 1.0f) {
            s = 1.0f;
        }
        if (s < 0.5f) {
            s = 0.5f;
        }
        return s;
    }

    private int unX(int mx) {
        float s = this.uiScale();
        float cx = this.field_22789 / 2.0f;
        return Math.round((mx - cx) / s + cx);
    }

    private int unY(int my) {
        float s = this.uiScale();
        float cy = this.field_22790 / 2.0f;
        return Math.round((my - cy) / s + cy);
    }

    private int btnX() {
        return this.field_22789 / 2 - 60;
    }

    private int btnY() {
        return this.groupY() + 58;
    }

    private int subY(int i) {
        return this.btnY() + 22 + 8 + i * 28;
    }

    private int tileX(boolean left) {
        return left ? this.btnX() - 8 - 22 : this.btnX() + 120 + 8;
    }

    private boolean inMods(int mx, int my) {
        return mx >= this.btnX() && mx < this.btnX() + 120 && my >= this.btnY() && my < this.btnY() + 22;
    }

    private boolean inSub(int i, int mx, int my) {
        int y = this.subY(i);
        return mx >= this.btnX() && mx < this.btnX() + 120 && my >= y && my < y + 22;
    }

    private int inTile(int mx, int my) {
        if (my < this.btnY() || my >= this.btnY() + 22) {
            return -1;
        }
        if (mx >= this.tileX(true) && mx < this.tileX(true) + 22) {
            return 0;
        }
        if (mx >= this.tileX(false) && mx < this.tileX(false) + 22) {
            return 1;
        }
        return -1;
    }

    public boolean method_25402(class_11909 click, boolean doubleClick) {
        int my;
        if (click.method_74245() != 0) {
            return false;
        }
        int mx = this.unX((int)click.comp_4798());
        if (this.inMods(mx, my = this.unY((int)click.comp_4799()))) {
            this.field_22787.method_1507((class_437)new SwiftModsMenu("MODS"));
            return true;
        }
        if (this.inSub(0, mx, my)) {
            this.field_22787.method_1507((class_437)new SwiftHudEditor(this));
            return true;
        }
        if (this.inSub(1, mx, my)) {
            this.field_22787.method_1507((class_437)new SwiftAccountsScreen(this));
            return true;
        }
        int t = this.inTile(mx, my);
        if (t == 0) {
            this.field_22787.method_1507((class_437)new SwiftModsMenu("SETTINGS"));
            return true;
        }
        if (t == 1) {
            this.method_25419();
            return true;
        }
        return false;
    }

    public void method_25394(class_332 c, int mouseX, int mouseY, float delta) {
        this.anim = SwiftClickGui.ease(Math.min(1.0f, (float)(System.nanoTime() - this.openedAt) / 1.5E8f));
        int ox = mouseX;
        int oy = mouseY;
        float s = this.uiScale();
        float fcx = this.field_22789 / 2.0f;
        float fcy = this.field_22790 / 2.0f;
        org.joml.Matrix3x2fStack matrices = c.method_51448();
        matrices.pushMatrix();
        matrices.translate(fcx, fcy);
        matrices.scale(s, s);
        matrices.translate(-fcx, -fcy);
        try {
        mouseX = this.unX(mouseX);
        mouseY = this.unY(mouseY);
        int cx = this.field_22789 / 2;
        int gy = this.groupY();
        Ui.crescent(c, cx, gy, 17, -723718);
        Ui.spacedText(c, this.field_22793, "TYXEN X SWIFT", cx, gy + 26, -855305, 2);
        boolean hov = this.inMods(mouseX, mouseY);
        Ui.sprite(c, hov ? Sprites.BTN_HOVER : Sprites.BTN, this.btnX(), this.btnY(), 120, 22);
        c.method_27534(this.field_22793, SwiftText.of("MODS"), cx, this.btnY() + 7, -855305);
        String[] subs = new String[]{"EDIT HUD", "ACCOUNTS"};
        for (int i = 0; i < subs.length; ++i) {
            int y = this.subY(i);
            boolean h = this.inSub(i, mouseX, mouseY);
            Ui.sprite(c, h ? Sprites.BTN_HOVER : Sprites.BTN, this.btnX(), y, 120, 22);
            c.method_27534(this.field_22793, SwiftText.of(subs[i]), cx, y + 7, -4605239);
        }
        int hovTile = this.inTile(mouseX, mouseY);
        this.drawTile(c, this.tileX(true), this.btnY(), hovTile == 0, true);
        this.drawTile(c, this.tileX(false), this.btnY(), hovTile == 1, false);
        String hint = "ESC or Right Shift to close";
        c.method_27534(this.field_22793, SwiftText.of(hint), cx, this.subY(1) + 22 + 10, -10592396);
        } finally {
        matrices.popMatrix();
        }
        super.method_25394(c, ox, oy, delta);
    }

    private void drawTile(class_332 c, int x, int y, boolean hover, boolean gear) {
        int col;
        Ui.sprite(c, hover ? Sprites.TILE22_HOVER : Sprites.TILE22, x, y, 22, 22);
        int n = col = hover ? -1 : -4605239;
        if (gear) {
            c.method_25290(class_10799.field_56883, TILE_SETTINGS, x + 5, y + 5, 0.0f, 0.0f, 12, 12, 12, 12);
        } else {
            c.method_27534(this.field_22793, SwiftText.of("x"), x + 11, y + 7, col);
        }
    }
}

