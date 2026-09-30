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

    public SwiftAccountsScreen(class_437 parent) {
        super((class_2561)class_2561.method_43470((String)"Accounts"));
        this.parent = parent;
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

    private int rowY(int i) {
        return this.py() + 92 + i * 22;
    }

    private boolean inRow(int i, int mx, int my) {
        int y = this.rowY(i);
        return mx >= this.px() + 12 && mx < this.px() + 300 - 12 && my >= y && my < y + 22;
    }

    private boolean inField(int mx, int my) {
        int y = this.py() + this.panelH() - 88;
        return mx >= this.px() + 12 && mx < this.px() + 12 + 170 && my >= y && my < y + 20;
    }

    private boolean inCreate(int mx, int my) {
        int y = this.py() + this.panelH() - 88;
        return mx >= this.px() + 190 && mx < this.px() + 300 - 12 && my >= y && my < y + 20;
    }

    private static UUID offlineUuid(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }

    private void apply(String name) {
        class_320 session = new class_320(name, SwiftAccountsScreen.offlineUuid(name), "0", Optional.empty(), Optional.empty());
        ((SwiftSessionAccessor)class_310.method_1551()).swift$setSession(session);
    }

    public boolean method_25402(class_11909 click, boolean doubleClick) {
        int mx = (int)click.comp_4798();
        int my = (int)click.comp_4799();
        if (click.method_74245() == 0 && this.inField(mx, my)) {
            this.typing = true;
            return true;
        }
        this.typing = false;
        if (click.method_74245() == 0 && this.inCreate(mx, my)) {
            String name = this.input.trim();
            if (!name.isEmpty() && name.length() <= 16 && name.matches("[a-zA-Z0-9_]+")) {
                ClientConfig.addAccount(name);
                this.apply(name);
                this.input = "";
            }
            return true;
        }
        List<String> accs = this.accounts();
        for (int i = 0; i < accs.size() && i < 5; ++i) {
            if (!this.inRow(i, mx, my)) continue;
            if (click.method_74245() == 1) {
                ClientConfig.removeAccount(accs.get(i));
                return true;
            }
            if (click.method_74245() != 0) continue;
            this.apply(accs.get(i));
            return true;
        }
        return click.method_74245() == 0 && mx >= this.px() && mx < this.px() + 300 && my >= this.py() && my < this.py() + this.panelH();
    }

    public boolean method_25400(class_11905 input) {
        if (!this.typing || !input.method_74227()) {
            return false;
        }
        String s = input.method_74226();
        if (s.length() != 1) {
            return true;
        }
        char ch = s.charAt(0);
        if ((ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9' || ch == '_') && this.input.length() < 16) {
            this.input = this.input + ch;
        }
        return true;
    }

    public boolean method_25404(class_11908 input) {
        if (this.typing && input.comp_4795() == 259 && !this.input.isEmpty()) {
            this.input = this.input.substring(0, this.input.length() - 1);
            return true;
        }
        if (this.typing && input.comp_4795() == 257) {
            this.typing = false;
            return true;
        }
        return super.method_25404(input);
    }

    public void method_25394(class_332 c, int mouseX, int mouseY, float delta) {
        boolean hCreate;
        this.anim = SwiftAccountsScreen.ease(Math.min(1.0f, (float)(System.nanoTime() - this.openedAt) / 1.5E8f));
        c.method_25294(0, 0, this.field_22789, this.field_22790, 1426458380);
        int x = this.px();
        int y = this.py();
        int w = 300;
        int h = this.panelH();
        Ui.sprite(c, Sprites.PANEL, x, y, w, h);
        Ui.crescent(c, x + 24, y + 20, 9, -723718);
        c.method_27535(this.field_22793, SwiftText.of("ACCOUNTS"), x + 40, y + 15, -855305);
        String user = class_310.method_1551().method_1548().method_1676();
        c.method_27535(this.field_22793, SwiftText.of("Signed in as " + user), x + 12, y + 38, -7697776);
        c.method_27535(this.field_22793, SwiftText.of("Click an account to switch  \u2022  Right-click to remove"), x + 12, y + 52, -7697776);
        c.method_25294(x, y + 68, x + w, y + 69, -13027015);
        c.method_27535(this.field_22793, SwiftText.of("SAVED OFFLINE ACCOUNTS"), x + 12, y + 78, -10592396);
        List<String> accs = this.accounts();
        int count = Math.min(5, accs.size());
        for (int i = 0; i < count; ++i) {
            int ry = this.rowY(i);
            boolean hover = this.inRow(i, mouseX, mouseY);
            boolean current = accs.get(i).equals(user);
            Ui.sprite(c, hover ? Sprites.BTN_HOVER : Sprites.BTN, x + 12, ry, w - 24, 20);
            c.method_27535(this.field_22793, SwiftText.of(accs.get(i)), x + 22, ry + 6, current ? Theme.accent() : -855305);
            if (!current) continue;
            c.method_27535(this.field_22793, SwiftText.of("ACTIVE"), x + w - 56, ry + 6, Theme.accent());
        }
        if (accs.isEmpty()) {
            c.method_27535(this.field_22793, SwiftText.of("No accounts yet \u2014 create one below."), x + 12, this.rowY(0) + 6, -7697776);
        }
        int fy = y + h - 88;
        Ui.sprite(c, Sprites.CHIP_DARK, x + 12, fy, 170, 20);
        String shown = this.input.isEmpty() ? (this.typing ? "" : "Username") : this.input;
        c.method_27535(this.field_22793, SwiftText.of(shown), x + 20, fy + 6, this.input.isEmpty() && !this.typing ? -10592396 : -855305);
        if (this.typing && System.currentTimeMillis() / 500L % 2L == 0L) {
            int cw = this.field_22793.method_27525((class_5348)SwiftText.of(shown));
            c.method_25294(x + 21 + cw, fy + 5, x + 22 + cw, fy + 15, -4605239);
        }
        Ui.sprite(c, (hCreate = this.inCreate(mouseX, mouseY)) ? Sprites.BTN_BLUE_HOVER : Sprites.BTN_BLUE, x + 190, fy, w - 202, 20);
        c.method_27534(this.field_22793, SwiftText.of("CREATE"), x + 190 + (w - 202) / 2, fy + 6, -1);
        String note = "Offline accounts work in singleplayer and offline-mode servers";
        c.method_27535(this.field_22793, SwiftText.of(note), x + 12, y + h - 52, -7697776);
        String esc = "ESC to go back";
        c.method_27535(this.field_22793, SwiftText.of(esc), x + w - this.field_22793.method_27525((class_5348)SwiftText.of(esc)) - 12, y + h - 18, -10592396);
        super.method_25394(c, mouseX, mouseY, delta);
    }
}

