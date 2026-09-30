/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  net.minecraft.class_5348
 */
package com.swiftclient.gui;

import com.swiftclient.config.ClientConfig;
import com.swiftclient.config.Theme;
import com.swiftclient.gui.SwiftModuleSettings;
import com.swiftclient.hud.HudUtil;
import com.swiftclient.modules.Module;
import com.swiftclient.modules.ModuleManager;
import com.swiftclient.util.SwiftText;
import com.swiftclient.util.Ui;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_5348;

public final class SwiftHudEditor
extends class_437 {
    private final class_437 parent;
    private Module selected;
    private boolean dragging;
    private int grabDX;
    private int grabDY;
    private boolean snapV;
    private boolean snapH;

    public SwiftHudEditor(class_437 parent) {
        super((class_2561)class_2561.method_43470((String)"HUD Layout"));
        this.parent = parent;
    }

    public boolean method_25421() {
        return false;
    }

    public void method_25419() {
        ClientConfig.save();
        this.field_22787.method_1507(this.parent);
    }

    private List<Module> modules() {
        ArrayList<Module> out = new ArrayList<Module>();
        for (Module m : ModuleManager.all()) {
            if (!m.hasPosition()) continue;
            out.add(m);
        }
        return out;
    }

    private int[] rect(Module m) {
        int x = ClientConfig.getX(m.name(), m.defaultX());
        int y = ClientConfig.getY(m.name(), m.defaultY());
        class_310 mc = class_310.method_1551();
        int w = m.hudWidth(mc);
        int h = m.hudHeight(mc);
        int[] live = HudUtil.BOUNDS.get(m.name());
        if (live != null && live.length >= 4) {
            w = live[2];
            h = live[3];
        }
        if (w < 16) {
            w = 16;
        }
        if (h < 10) {
            h = 10;
        }
        return new int[]{x, y, w, h};
    }

    private void grid(class_332 c) {
        int step = 24;
        int col = 0x14FFFFFF;
        for (int gx = step / 2; gx < this.field_22789; gx += step) {
            for (int gy = step / 2; gy < this.field_22790; gy += step) {
                c.method_25294(gx, gy, gx + 1, gy + 1, col);
            }
        }
    }

    private Module hit(int mx, int my) {
        List<Module> mods = this.modules();
        for (int i = mods.size() - 1; i >= 0; --i) {
            int[] r = this.rect(mods.get(i));
            if (mx < r[0] || mx >= r[0] + r[2] || my < r[1] || my >= r[1] + r[3]) continue;
            return mods.get(i);
        }
        return null;
    }

    public boolean method_25402(class_11909 click, boolean doubleClick) {
        int mx = (int)click.comp_4798();
        int my = (int)click.comp_4799();
        if (click.method_74245() == 1) {
            Module h = this.hit(mx, my);
            if (h != null) {
                this.field_22787.method_1507((class_437)new SwiftModuleSettings(h, this));
                return true;
            }
            return false;
        }
        if (click.method_74245() != 0) {
            return false;
        }
        Module h = this.hit(mx, my);
        if (h != null) {
            this.selected = h;
            this.dragging = true;
            int[] r = this.rect(h);
            this.grabDX = mx - r[0];
            this.grabDY = my - r[1];
            return true;
        }
        this.selected = null;
        return false;
    }

    public boolean method_25403(class_11909 click, double dx, double dy) {
        if (!this.dragging || this.selected == null) {
            return false;
        }
        int nx = Math.max(0, Math.min(this.field_22789 - 20, (int)click.comp_4798() - this.grabDX));
        int ny = Math.max(0, Math.min(this.field_22790 - 20, (int)click.comp_4799() - this.grabDY));
        int[] r = this.rect(this.selected);
        int snapX = this.field_22789 / 2 - r[2] / 2;
        int snapY = this.field_22790 / 2 - r[3] / 2;
        this.snapV = Math.abs(nx - snapX) <= 4;
        this.snapH = Math.abs(ny - snapY) <= 4;
        if (this.snapV) {
            nx = snapX;
        }
        if (this.snapH) {
            ny = snapY;
        }
        ClientConfig.setPosSilent(this.selected.name(), nx, ny);
        return true;
    }

    public boolean method_25406(class_11909 click) {
        this.dragging = false;
        this.snapV = false;
        this.snapH = false;
        ClientConfig.save();
        return false;
    }

    public void method_25394(class_332 c, int mouseX, int mouseY, float delta) {
        this.grid(c);
        c.method_27534(this.field_22793, SwiftText.of("HUD LAYOUT"), this.field_22789 / 2, 10, -855305);
        c.method_27534(this.field_22793, SwiftText.of("Drag boxes to move  \u2022  Snaps to center  \u2022  Right-click for settings  \u2022  ESC to go back"), this.field_22789 / 2, 23, -7697776);
        if (this.dragging && (this.snapV || this.snapH)) {
            int acc = Theme.accent();
            if (this.snapV) {
                c.method_25294(this.field_22789 / 2, 0, this.field_22789 / 2 + 1, this.field_22790, acc);
            }
            if (this.snapH) {
                c.method_25294(0, this.field_22790 / 2, this.field_22789, this.field_22790 / 2 + 1, acc);
            }
        }
        Module hov = this.hit(mouseX, mouseY);
        for (Module m : this.modules()) {
            boolean h;
            int[] r = this.rect(m);
            boolean sel = m == this.selected;
            boolean bl = h = m == hov;
            int col = sel ? -1 : (h ? Theme.accent() : 0x66888899);
            Ui.outline(c, r[0] - 1, r[1] - 1, r[2] + 2, r[3] + 2, col);
            String tag = m.name() + (m.enabled() ? "" : " (off)");
            int tw = this.field_22793.method_27525((class_5348)SwiftText.of(tag)) + 8;
            int tx = Math.min(Math.max(r[0], 4), Math.max(4, this.field_22789 - tw - 4));
            int ty = Math.max(r[1] - 13, 2);
            c.method_25294(tx, ty, tx + tw, ty + 11, -871494124);
            c.method_27535(this.field_22793, SwiftText.of(tag), tx + 4, ty + 2, sel ? -1 : -4605239);
        }
        if (this.selected != null) {
            int[] r = this.rect(this.selected);
            c.method_27534(this.field_22793, SwiftText.of(this.selected.name() + "  \u2022  X " + r[0] + "  Y " + r[1]), this.field_22789 / 2, this.field_22790 - 14, -855305);
        } else if (this.modules().isEmpty()) {
            c.method_27534(this.field_22793, SwiftText.of("No positioned modules found."), this.field_22789 / 2, this.field_22790 / 2, -7697776);
        }
        super.method_25394(c, mouseX, mouseY, delta);
    }
}

