/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package com.swiftclient.render;

import com.swiftclient.config.ClientConfig;
import com.swiftclient.modules.Category;
import com.swiftclient.modules.Module;
import com.swiftclient.modules.ModuleManager;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class CrosshairModule
extends Module {
    public CrosshairModule() {
        super("Crosshair", Category.RENDER, "Custom crosshair styles.", 0);
    }

    public static void render(class_332 c) {
        try {
            CrosshairModule.renderInner(c);
        } catch (Throwable t) {
            ModuleManager.fault(ModuleManager.get(CrosshairModule.class), t, "render");
        }
    }

    private static void renderInner(class_332 c) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1690 == null || mc.field_1690.field_1842 || mc.field_1724 == null) {
            return;
        }
        int cx = c.method_51421() / 2;
        int cy = c.method_51443() / 2;
        if (ClientConfig.getBool("Crosshair", "dot", false)) {
            c.method_25294(cx - 1, cy - 1, cx + 2, cy + 2, -1);
        } else {
            int col = -1513232;
            c.method_25294(cx - 4, cy, cx + 5, cy + 1, col);
            c.method_25294(cx, cy - 4, cx + 1, cy + 5, col);
        }
    }
}

