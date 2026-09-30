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

    public SwiftHudEditor(class_437 class_4372) {
        super((class_2561)class_2561.method_43470((String)"HUD Layout"));
        this.parent = class_4372;
    }

    public boolean method_25421() {
        return false;
    }

    public void method_25419() {
        ClientConfig.save();
        this.field_22787.method_1507(this.parent);
    }

    private List<Module> modules() {
        ArrayList<Module> arrayList = new ArrayList<Module>();
        for (Module module : ModuleManager.all()) {
            if (!module.hasPosition()) continue;
            arrayList.add(module);
        }
        return arrayList;
    }

    private int[] rect(Module module) {
        int n = ClientConfig.getX(module.name(), module.defaultX());
        int n2 = ClientConfig.getY(module.name(), module.defaultY());
        class_310 class_3102 = class_310.method_1551();
        int n3 = module.hudWidth(class_3102);
        int n4 = module.hudHeight(class_3102);
        int[] nArray = HudUtil.BOUNDS.get(module.name());
        if (nArray != null && nArray.length >= 4) {
            n3 = nArray[2];
            n4 = nArray[3];
        }
        if (n3 < 16) {
            n3 = 16;
        }
        if (n4 < 10) {
            n4 = 10;
        }
        return new int[]{n, n2, n3, n4};
    }

    private void grid(class_332 class_3322) {
        int n = 24;
        int n2 = 0x14FFFFFF;
        for (int i = n / 2; i < this.field_22789; i += n) {
            for (int j = n / 2; j < this.field_22790; j += n) {
                class_3322.method_25294(i, j, i + 1, j + 1, n2);
            }
        }
    }

    private Module hit(int n, int n2) {
        List<Module> list = this.modules();
        for (int i = list.size() - 1; i >= 0; --i) {
            int[] nArray = this.rect(list.get(i));
            if (n < nArray[0] || n >= nArray[0] + nArray[2] || n2 < nArray[1] || n2 >= nArray[1] + nArray[3]) continue;
            return list.get(i);
        }
        return null;
    }

    public boolean method_25402(class_11909 class_119092, boolean bl) {
        int n = (int)class_119092.comp_4798();
        int n2 = (int)class_119092.comp_4799();
        if (class_119092.method_74245() == 1) {
            Module module = this.hit(n, n2);
            if (module != null) {
                this.field_22787.method_1507((class_437)new SwiftModuleSettings(module, this));
                return true;
            }
            return false;
        }
        if (class_119092.method_74245() != 0) {
            return false;
        }
        Module module = this.hit(n, n2);
        if (module != null) {
            this.selected = module;
            this.dragging = true;
            int[] nArray = this.rect(module);
            this.grabDX = n - nArray[0];
            this.grabDY = n2 - nArray[1];
            return true;
        }
        this.selected = null;
        return false;
    }

    public boolean method_25403(class_11909 class_119092, double d, double d2) {
        if (!this.dragging || this.selected == null) {
            return false;
        }
        int n = Math.max(0, Math.min(this.field_22789 - 20, (int)class_119092.comp_4798() - this.grabDX));
        int n2 = Math.max(0, Math.min(this.field_22790 - 20, (int)class_119092.comp_4799() - this.grabDY));
        int[] nArray = this.rect(this.selected);
        int n3 = this.field_22789 / 2 - nArray[2] / 2;
        int n4 = this.field_22790 / 2 - nArray[3] / 2;
        this.snapV = Math.abs(n - n3) <= 4;
        boolean bl = this.snapH = Math.abs(n2 - n4) <= 4;
        if (this.snapV) {
            n = n3;
        }
        if (this.snapH) {
            n2 = n4;
        }
        ClientConfig.setPosSilent(this.selected.name(), n, n2);
        return true;
    }

    public boolean method_25406(class_11909 class_119092) {
        this.dragging = false;
        this.snapV = false;
        this.snapH = false;
        ClientConfig.save();
        return false;
    }

    public void method_25394(class_332 class_3322, int n, int n2, float f) {
        this.grid(class_3322);
        class_3322.method_27534(this.field_22793, SwiftText.of("HUD LAYOUT"), this.field_22789 / 2, 10, -855305);
        class_3322.method_27534(this.field_22793, SwiftText.of("Drag boxes to move  \u2022  Snaps to center  \u2022  Right-click for settings  \u2022  ESC to go back"), this.field_22789 / 2, 23, -7697776);
        if (this.dragging && (this.snapV || this.snapH)) {
            int n3 = Theme.accent();
            if (this.snapV) {
                class_3322.method_25294(this.field_22789 / 2, 0, this.field_22789 / 2 + 1, this.field_22790, n3);
            }
            if (this.snapH) {
                class_3322.method_25294(0, this.field_22790 / 2, this.field_22789, this.field_22790 / 2 + 1, n3);
            }
        }
        Module module = this.hit(n, n2);
        for (Module module2 : this.modules()) {
            int[] nArray = this.rect(module2);
            boolean bl = module2 == this.selected;
            boolean bl2 = module2 == module;
            boolean bl3 = bl2;
            int n4 = bl ? -1 : (bl2 ? Theme.accent() : 0x66888899);
            Ui.outline(class_3322, nArray[0] - 1, nArray[1] - 1, nArray[2] + 2, nArray[3] + 2, n4);
            String string = module2.name() + (module2.enabled() ? "" : " (off)");
            int n5 = this.field_22793.method_27525((class_5348)SwiftText.of(string)) + 8;
            int n6 = Math.min(Math.max(nArray[0], 4), Math.max(4, this.field_22789 - n5 - 4));
            int n7 = Math.max(nArray[1] - 13, 2);
            class_3322.method_25294(n6, n7, n6 + n5, n7 + 11, -871494124);
            class_3322.method_27535(this.field_22793, SwiftText.of(string), n6 + 4, n7 + 2, bl ? -1 : -4605239);
        }
        if (this.selected != null) {
            Object object = this.rect(this.selected);
            class_3322.method_27534(this.field_22793, SwiftText.of(this.selected.name() + "  \u2022  X " + (int)object[0] + "  Y " + (int)object[1]), this.field_22789 / 2, this.field_22790 - 14, -855305);
        } else if (this.modules().isEmpty()) {
            class_3322.method_27534(this.field_22793, SwiftText.of("No positioned modules found."), this.field_22789 / 2, this.field_22790 / 2, -7697776);
        }
        super.method_25394(class_3322, n, n2, f);
    }
}

