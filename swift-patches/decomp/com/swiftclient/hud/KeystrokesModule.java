/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_3675
 */
package com.swiftclient.hud;

import com.swiftclient.config.ClientConfig;
import com.swiftclient.hud.HudUtil;
import com.swiftclient.modules.Category;
import com.swiftclient.modules.Module;
import com.swiftclient.util.SwiftText;
import com.swiftclient.util.Ui;
import net.minecraft.class_1041;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3675;

public final class KeystrokesModule
extends Module {
    public KeystrokesModule() {
        super("Keystrokes", Category.HUD, "WASD and mouse-button input display.", 0);
    }

    @Override
    public int defaultY() {
        return 58;
    }

    private boolean down(int key) {
        return class_3675.method_15987((class_1041)class_310.method_1551().method_22683(), (int)key);
    }

    @Override
    public void render(class_332 c, float d) {
        int x = ClientConfig.getX(this.name(), this.defaultX());
        int y = ClientConfig.getY(this.name(), this.defaultY());
        this.key(c, "W", x + 22, y, 87);
        this.key(c, "A", x, y + 22, 65);
        this.key(c, "S", x + 22, y + 22, 83);
        this.key(c, "D", x + 44, y + 22, 68);
        if (ClientConfig.getBool(this.name(), "mouse", true)) {
            this.key(c, "LMB", x, y + 44, 0);
            this.key(c, "RMB", x + 44, y + 44, 1);
        }
        HudUtil.BOUNDS.put(this.name(), new int[]{x, y, 84, 64});
    }

    @Override
    public int hudWidth(class_310 mc) {
        return 84;
    }

    @Override
    public int hudHeight(class_310 mc) {
        return 64;
    }

    private void key(class_332 c, String t, int x, int y, int k) {
        boolean p = this.down(k);
        Ui.rounded(c, x, y, 40, 20, 4, p ? -1514800 : -1290792936, 0);
        if (!p) {
            Ui.outline(c, x, y, 40, 20, 0x66FFFFFF);
        }
        c.method_27534(class_310.method_1551().field_1772, SwiftText.of(t), x + 20, y + 6, p ? -15461350 : -3157795);
    }
}

