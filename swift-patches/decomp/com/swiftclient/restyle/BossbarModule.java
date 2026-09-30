/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1259$class_1260
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_345
 *  net.minecraft.class_3532
 */
package com.swiftclient.restyle;

import com.swiftclient.modules.Category;
import com.swiftclient.modules.Module;
import com.swiftclient.modules.ModuleManager;
import java.util.Map;
import java.util.UUID;
import net.minecraft.class_1259;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_345;
import net.minecraft.class_3532;

public final class BossbarModule
extends Module {
    public BossbarModule() {
        super("Bossbar", Category.HUD, "Restyled boss bars.", 0);
    }

    public static void render(class_332 class_3322, Map<UUID, class_345> map) {
        try {
            BossbarModule.renderInner(class_3322, map);
        }
        catch (Throwable throwable) {
            ModuleManager.fault(ModuleManager.get(BossbarModule.class), throwable, "render");
        }
    }

    private static void renderInner(class_332 class_3322, Map<UUID, class_345> map) {
        class_310 class_3102 = class_310.method_1551();
        int n = class_3322.method_51421();
        int n2 = 12;
        int n3 = 0;
        for (class_345 class_3452 : map.values()) {
            if (n3 >= 5) break;
            int n4 = n / 2;
            class_3322.method_27534(class_3102.field_1772, class_3452.method_5414(), n4, n2, -855305);
            int n5 = 182;
            int n6 = n4 - n5 / 2;
            int n7 = n2 + 11;
            class_3322.method_25294(n6 - 1, n7 - 1, n6 + n5 + 1, n7 + 6, -16119280);
            class_3322.method_25294(n6, n7, n6 + n5, n7 + 5, -15000536);
            int n8 = (int)((float)n5 * class_3532.method_15363((float)class_3452.method_5412(), (float)0.0f, (float)1.0f));
            if (n8 > 0) {
                class_3322.method_25294(n6, n7, n6 + n8, n7 + 5, BossbarModule.color(class_3452.method_5420()));
            }
            n2 += 26;
            ++n3;
        }
    }

    private static int color(class_1259.class_1260 class_12602) {
        if (class_12602 == class_1259.class_1260.field_5788) {
            return -1023342;
        }
        if (class_12602 == class_1259.class_1260.field_5780) {
            return -12877066;
        }
        if (class_12602 == class_1259.class_1260.field_5784) {
            return -1754827;
        }
        if (class_12602 == class_1259.class_1260.field_5785) {
            return -12345273;
        }
        if (class_12602 == class_1259.class_1260.field_5782) {
            return -141259;
        }
        if (class_12602 == class_1259.class_1260.field_5783) {
            return -7461718;
        }
        return -2039584;
    }
}

