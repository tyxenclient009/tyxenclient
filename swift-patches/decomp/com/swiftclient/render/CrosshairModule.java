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

    public static void render(class_332 class_3322) {
        try {
            CrosshairModule.renderInner(class_3322);
        }
        catch (Throwable throwable) {
            ModuleManager.fault(ModuleManager.get(CrosshairModule.class), throwable, "render");
        }
    }

    private static void renderInner(class_332 class_3322) {
        class_310 class_3102 = class_310.method_1551();
        if (class_3102.field_1690 == null || class_3102.field_1690.field_1842 || class_3102.field_1724 == null) {
            return;
        }
        int n = class_3322.method_51421() / 2;
        int n2 = class_3322.method_51443() / 2;
        if (ClientConfig.getBool("Crosshair", "dot", false)) {
            class_3322.method_25294(n - 1, n2 - 1, n + 2, n2 + 2, -1);
        } else {
            int n3 = -1513232;
            class_3322.method_25294(n - 4, n2, n + 5, n2 + 1, n3);
            class_3322.method_25294(n, n2 - 4, n + 1, n2 + 5, n3);
        }
    }
}

