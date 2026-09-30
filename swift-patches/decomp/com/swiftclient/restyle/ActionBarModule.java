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

    public static void render(class_332 class_3322, class_2561 class_25612, int n) {
        try {
            ActionBarModule.renderInner(class_3322, class_25612, n);
        }
        catch (Throwable throwable) {
            ModuleManager.fault(ModuleManager.get(ActionBarModule.class), throwable, "render");
        }
    }

    private static void renderInner(class_332 class_3322, class_2561 class_25612, int n) {
        class_310 class_3102 = class_310.method_1551();
        float f = Math.min(1.0f, (float)n / 10.0f);
        int n2 = (int)(f * 255.0f) << 24;
        int n3 = class_3322.method_51421();
        int n4 = n3 / 2;
        int n5 = class_3322.method_51443() - 68;
        int n6 = class_3102.field_1772.method_27525((class_5348)class_25612);
        class_3322.method_25294(n4 - n6 / 2 - 6, n5 - 3, n4 + n6 / 2 + 6, n5 + 11, -1290792936);
        class_3322.method_27535(class_3102.field_1772, class_25612, n4 - n6 / 2, n5, n2 | 0xF2F2F7);
        class_3322.method_25294(n4 - n6 / 2 - 6, n5 + 11, n4 + n6 / 2 + 6, n5 + 12, n2 & 0xFF000000 | Theme.accent() & 0xFFFFFF);
    }
}

