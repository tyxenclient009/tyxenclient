/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_5348
 */
package com.swiftclient.restyle;

import com.swiftclient.config.Theme;
import com.swiftclient.modules.Category;
import com.swiftclient.modules.Module;
import com.swiftclient.modules.ModuleManager;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_5348;

public final class ActionBarModule
extends Module {
    public ActionBarModule() {
        super("ActionBar", Category.HUD, "Restyled action bar.", 0);
    }

    public static void render(class_332 c, class_2561 text, int remaining) {
        try {
            ActionBarModule.renderInner(c, text, remaining);
        } catch (Throwable t) {
            ModuleManager.fault(ModuleManager.get(ActionBarModule.class), t, "render");
        }
    }

    private static void renderInner(class_332 c, class_2561 text, int remaining) {
        class_310 mc = class_310.method_1551();
        float a = Math.min(1.0f, (float)remaining / 10.0f);
        int alpha = (int)(a * 255.0f) << 24;
        int w = c.method_51421();
        int x = w / 2;
        int y = c.method_51443() - 68;
        int tw = mc.field_1772.method_27525((class_5348)text);
        c.method_25294(x - tw / 2 - 6, y - 3, x + tw / 2 + 6, y + 11, -1290792936);
        c.method_27535(mc.field_1772, text, x - tw / 2, y, alpha | 0xF2F2F7);
        c.method_25294(x - tw / 2 - 6, y + 11, x + tw / 2 + 6, y + 12, alpha & 0xFF000000 | Theme.accent() & 0xFFFFFF);
    }
}

