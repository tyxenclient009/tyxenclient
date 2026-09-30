/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_320
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  net.minecraft.class_5348
 */
package com.swiftclient.gui;

import com.swiftclient.config.ClientConfig;
import com.swiftclient.config.Theme;
import com.swiftclient.gui.Sprites;
import com.swiftclient.mixin.SwiftSessionAccessor;
import com.swiftclient.util.SwiftText;
import com.swiftclient.util.Ui;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_320;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_5348;

public final class SwiftAccountsScreen
extends class_437 {
    private final class_437 parent;
    private String input = "";
    private boolean typing;
    private static final int PANEL_W = 300;
    private static final int ROW_H = 22;
    private static final int MAX_ROWS = 5;
    private long openedAt;
    private float anim;

    public SwiftAccountsScreen(class_437 class_4372) {
        super((class_2561)class_2561.method_43470((String)"Accounts"));
        this.parent = class_4372;
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

    public void method_25419() {
        this.field_22787.method_1507(this.parent);
    }

    private int panelH() {
        return 176 + Math.min(5, Math.max(1, ClientConfig.accounts().size())) * 22;
    }

    private int px() {
        return (this.field_22789 - 300) / 2;
    }

    private int py() {
        return (this.field_22790 - this.panelH()) / 2 + (int)((1.0f - this.anim) * 18.0f);
    }

    private List<String> accounts() {
        return new ArrayList<String>(ClientConfig.accounts());
    }

    private int rowY(int n) {
        return this.py() + 92 + n * 22;
    }

    private boolean inRow(int n, int n2, int n3) {
        int n4 = this.rowY(n);
        return n2 >= this.px() + 12 && n2 < this.px() + 300 - 12 && n3 >= n4 && n3 < n4 + 22;
    }

    private boolean inField(int n, int n2) {
        int n3 = this.py() + this.panelH() - 88;
        return n >= this.px() + 12 && n < this.px() + 12 + 170 && n2 >= n3 && n2 < n3 + 20;
    }

    private boolean inCreate(int n, int n2) {
        int n3 = this.py() + this.panelH() - 88;
        return n >= this.px() + 190 && n < this.px() + 300 - 12 && n2 >= n3 && n2 < n3 + 20;
    }

    private static UUID offlineUuid(String string) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + string).getBytes(StandardCharsets.UTF_8));
    }

    private void apply(String string) {
        class_320 class_3202 = new class_320(string, SwiftAccountsScreen.offlineUuid(string), "0", Optional.empty(), Optional.empty());
        ((SwiftSessionAccessor)class_310.method_1551()).swift$setSession(class_3202);
    }

    public boolean method_25402(class_11909 class_119092, boolean bl) {
        int n = (int)class_119092.comp_4798();
        int n2 = (int)class_119092.comp_4799();
        if (class_119092.method_74245() == 0 && this.inField(n, n2)) {
            this.typing = true;
            return true;
        }
        this.typing = false;
        if (class_119092.method_74245() == 0 && this.inCreate(n, n2)) {
            String string = this.input.trim();
            if (!string.isEmpty() && string.length() <= 16 && string.matches("[a-zA-Z0-9_]+")) {
                ClientConfig.addAccount(string);
                this.apply(string);
                this.input = "";
            }
            return true;
        }
        List<String> list = this.accounts();
        for (int i = 0; i < list.size() && i < 5; ++i) {
            if (!this.inRow(i, n, n2)) continue;
            if (class_119092.method_74245() == 1) {
                ClientConfig.removeAccount(list.get(i));
                return true;
            }
            if (class_119092.method_74245() != 0) continue;
            this.apply(list.get(i));
            return true;
        }
        return class_119092.method_74245() == 0 && n >= this.px() && n < this.px() + 300 && n2 >= this.py() && n2 < this.py() + this.panelH();
    }

    public boolean method_25400(class_11905 class_119052) {
        if (!this.typing || !class_119052.method_74227()) {
            return false;
        }
        String string = class_119052.method_74226();
        if (string.length() != 1) {
            return true;
        }
        char c = string.charAt(0);
        if ((c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_') && this.input.length() < 16) {
            this.input = this.input + c;
        }
        return true;
    }

    public boolean method_25404(class_11908 class_119082) {
        if (this.typing && class_119082.comp_4795() == 259 && !this.input.isEmpty()) {
            this.input = this.input.substring(0, this.input.length() - 1);
            return true;
        }
        if (this.typing && class_119082.comp_4795() == 257) {
            this.typing = false;
            return true;
        }
        return super.method_25404(class_119082);
    }

    public void method_25394(class_332 class_3322, int n, int n2, float f) {
        boolean bl;
        int n3;
        int n4;
        this.anim = SwiftAccountsScreen.ease(Math.min(1.0f, (float)(System.nanoTime() - this.openedAt) / 1.5E8f));
        class_3322.method_25294(0, 0, this.field_22789, this.field_22790, 1426458380);
        int n5 = this.px();
        int n6 = this.py();
        int n7 = 300;
        int n8 = this.panelH();
        Ui.sprite(class_3322, Sprites.PANEL, n5, n6, n7, n8);
        Ui.crescent(class_3322, n5 + 24, n6 + 20, 9, -723718);
        class_3322.method_27535(this.field_22793, SwiftText.of("ACCOUNTS"), n5 + 40, n6 + 15, -855305);
        String string = class_310.method_1551().method_1548().method_1676();
        class_3322.method_27535(this.field_22793, SwiftText.of("Signed in as " + string), n5 + 12, n6 + 38, -7697776);
        class_3322.method_27535(this.field_22793, SwiftText.of("Click an account to switch  \u2022  Right-click to remove"), n5 + 12, n6 + 52, -7697776);
        class_3322.method_25294(n5, n6 + 68, n5 + n7, n6 + 69, -13027015);
        class_3322.method_27535(this.field_22793, SwiftText.of("SAVED OFFLINE ACCOUNTS"), n5 + 12, n6 + 78, -10592396);
        List<String> list = this.accounts();
        int n9 = Math.min(5, list.size());
        for (n4 = 0; n4 < n9; ++n4) {
            int n10 = this.rowY(n4);
            n3 = this.inRow(n4, n, n2);
            boolean bl2 = list.get(n4).equals(string);
            Ui.sprite(class_3322, n3 != 0 ? Sprites.BTN_HOVER : Sprites.BTN, n5 + 12, n10, n7 - 24, 20);
            class_3322.method_27535(this.field_22793, SwiftText.of(list.get(n4)), n5 + 22, n10 + 6, bl2 ? Theme.accent() : -855305);
            if (!bl2) continue;
            class_3322.method_27535(this.field_22793, SwiftText.of("ACTIVE"), n5 + n7 - 56, n10 + 6, Theme.accent());
        }
        if (list.isEmpty()) {
            class_3322.method_27535(this.field_22793, SwiftText.of("No accounts yet \u2014 create one below."), n5 + 12, this.rowY(0) + 6, -7697776);
        }
        n4 = n6 + n8 - 88;
        Ui.sprite(class_3322, Sprites.CHIP_DARK, n5 + 12, n4, 170, 20);
        String string2 = this.input.isEmpty() ? (this.typing ? "" : "Username") : this.input;
        class_3322.method_27535(this.field_22793, SwiftText.of(string2), n5 + 20, n4 + 6, this.input.isEmpty() && !this.typing ? -10592396 : -855305);
        if (this.typing && System.currentTimeMillis() / 500L % 2L == 0L) {
            n3 = this.field_22793.method_27525((class_5348)SwiftText.of(string2));
            class_3322.method_25294(n5 + 21 + n3, n4 + 5, n5 + 22 + n3, n4 + 15, -4605239);
        }
        Ui.sprite(class_3322, (bl = this.inCreate(n, n2)) ? Sprites.BTN_BLUE_HOVER : Sprites.BTN_BLUE, n5 + 190, n4, n7 - 202, 20);
        class_3322.method_27534(this.field_22793, SwiftText.of("CREATE"), n5 + 190 + (n7 - 202) / 2, n4 + 6, -1);
        String string3 = "Offline accounts work in singleplayer and offline-mode servers";
        class_3322.method_27535(this.field_22793, SwiftText.of(string3), n5 + 12, n6 + n8 - 52, -7697776);
        String string4 = "ESC to go back";
        class_3322.method_27535(this.field_22793, SwiftText.of(string4), n5 + n7 - this.field_22793.method_27525((class_5348)SwiftText.of(string4)) - 12, n6 + n8 - 18, -10592396);
        super.method_25394(class_3322, n, n2, f);
    }
}

