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
 *  org.joml.Matrix3x2fStack
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
import org.joml.Matrix3x2fStack;

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

    private static float ease(float f) {
        float f2 = 1.0f - f;
        return 1.0f - f2 * f2 * f2;
    }

    public boolean method_25421() {
        return false;
    }

    private int groupY() {
        return (int)((double)this.field_22790 * 0.44) + (int)((1.0f - this.anim) * 14.0f);
    }

    private float uiScale() {
        float f = Math.min((float)this.field_22789 / 640.0f, (float)this.field_22790 / 400.0f);
        if (f > 1.0f) {
            f = 1.0f;
        }
        if (f < 0.5f) {
            f = 0.5f;
        }
        return f;
    }

    private int unX(int n) {
        float f = this.uiScale();
        float f2 = (float)this.field_22789 / 2.0f;
        return Math.round(((float)n - f2) / f + f2);
    }

    private int unY(int n) {
        float f = this.uiScale();
        float f2 = (float)this.field_22790 / 2.0f;
        return Math.round(((float)n - f2) / f + f2);
    }

    private int btnX() {
        return this.field_22789 / 2 - 60;
    }

    private int btnY() {
        return this.groupY() + 58;
    }

    private int subY(int n) {
        return this.btnY() + 22 + 8 + n * 28;
    }

    private int tileX(boolean bl) {
        return bl ? this.btnX() - 8 - 22 : this.btnX() + 120 + 8;
    }

    private boolean inMods(int n, int n2) {
        return n >= this.btnX() && n < this.btnX() + 120 && n2 >= this.btnY() && n2 < this.btnY() + 22;
    }

    private boolean inSub(int n, int n2, int n3) {
        int n4 = this.subY(n);
        return n2 >= this.btnX() && n2 < this.btnX() + 120 && n3 >= n4 && n3 < n4 + 22;
    }

    private int inTile(int n, int n2) {
        if (n2 < this.btnY() || n2 >= this.btnY() + 22) {
            return -1;
        }
        if (n >= this.tileX(true) && n < this.tileX(true) + 22) {
            return 0;
        }
        if (n >= this.tileX(false) && n < this.tileX(false) + 22) {
            return 1;
        }
        return -1;
    }

    public boolean method_25402(class_11909 class_119092, boolean bl) {
        int n;
        if (class_119092.method_74245() != 0) {
            return false;
        }
        int n2 = this.unX((int)class_119092.comp_4798());
        if (this.inMods(n2, n = this.unY((int)class_119092.comp_4799()))) {
            this.field_22787.method_1507((class_437)new SwiftModsMenu("MODS"));
            return true;
        }
        if (this.inSub(0, n2, n)) {
            this.field_22787.method_1507((class_437)new SwiftHudEditor(this));
            return true;
        }
        if (this.inSub(1, n2, n)) {
            this.field_22787.method_1507((class_437)new SwiftAccountsScreen(this));
            return true;
        }
        int n3 = this.inTile(n2, n);
        if (n3 == 0) {
            this.field_22787.method_1507((class_437)new SwiftModsMenu("SETTINGS"));
            return true;
        }
        if (n3 == 1) {
            this.method_25419();
            return true;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void method_25394(class_332 class_3322, int n, int n2, float f) {
        this.anim = SwiftClickGui.ease(Math.min(1.0f, (float)(System.nanoTime() - this.openedAt) / 1.5E8f));
        int n3 = n;
        int n4 = n2;
        float f2 = this.uiScale();
        float f3 = (float)this.field_22789 / 2.0f;
        float f4 = (float)this.field_22790 / 2.0f;
        Matrix3x2fStack matrix3x2fStack = class_3322.method_51448();
        matrix3x2fStack.pushMatrix();
        matrix3x2fStack.translate(f3, f4);
        matrix3x2fStack.scale(f2, f2);
        matrix3x2fStack.translate(-f3, -f4);
        try {
            int n5;
            n = this.unX(n);
            n2 = this.unY(n2);
            int n6 = this.field_22789 / 2;
            int n7 = this.groupY();
            Ui.crescent(class_3322, n6, n7, 17, -723718);
            Ui.spacedText(class_3322, this.field_22793, "TYXEN X SWIFT", n6, n7 + 26, -855305, 2);
            boolean bl = this.inMods(n, n2);
            Ui.sprite(class_3322, bl ? Sprites.BTN_HOVER : Sprites.BTN, this.btnX(), this.btnY(), 120, 22);
            class_3322.method_27534(this.field_22793, SwiftText.of("MODS"), n6, this.btnY() + 7, -855305);
            String[] stringArray = new String[]{"EDIT HUD", "ACCOUNTS"};
            for (n5 = 0; n5 < stringArray.length; ++n5) {
                int n8 = this.subY(n5);
                boolean bl2 = this.inSub(n5, n, n2);
                Ui.sprite(class_3322, bl2 ? Sprites.BTN_HOVER : Sprites.BTN, this.btnX(), n8, 120, 22);
                class_3322.method_27534(this.field_22793, SwiftText.of(stringArray[n5]), n6, n8 + 7, -4605239);
            }
            n5 = this.inTile(n, n2);
            this.drawTile(class_3322, this.tileX(true), this.btnY(), n5 == 0, true);
            this.drawTile(class_3322, this.tileX(false), this.btnY(), n5 == 1, false);
            String string = "ESC or Right Shift to close";
            class_3322.method_27534(this.field_22793, SwiftText.of(string), n6, this.subY(1) + 22 + 10, -10592396);
        }
        finally {
            matrix3x2fStack.popMatrix();
        }
        super.method_25394(class_3322, n3, n4, f);
    }

    private void drawTile(class_332 class_3322, int n, int n2, boolean bl, boolean bl2) {
        Ui.sprite(class_3322, bl ? Sprites.TILE22_HOVER : Sprites.TILE22, n, n2, 22, 22);
        int n3 = bl ? -1 : -4605239;
        int n4 = n3;
        if (bl2) {
            class_3322.method_25290(class_10799.field_56883, TILE_SETTINGS, n + 5, n2 + 5, 0.0f, 0.0f, 12, 12, 12, 12);
        } else {
            class_3322.method_27534(this.field_22793, SwiftText.of("x"), n + 11, n2 + 7, n3);
        }
    }
}

