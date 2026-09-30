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

    public static void render(class_332 c, Map<UUID, class_345> bars) {
        try {
            BossbarModule.renderInner(c, bars);
        } catch (Throwable t) {
            ModuleManager.fault(ModuleManager.get(BossbarModule.class), t, "render");
        }
    }

    private static void renderInner(class_332 c, Map<UUID, class_345> bars) {
        class_310 mc = class_310.method_1551();
        int w = c.method_51421();
        int y = 12;
        int i = 0;
        for (class_345 b : bars.values()) {
            if (i >= 5) break;
            int cx = w / 2;
            c.method_27534(mc.field_1772, b.method_5414(), cx, y, -855305);
            int bw = 182;
            int x0 = cx - bw / 2;
            int y0 = y + 11;
            c.method_25294(x0 - 1, y0 - 1, x0 + bw + 1, y0 + 6, -16119280);
            c.method_25294(x0, y0, x0 + bw, y0 + 5, -15000536);
            int fw = (int)((float)bw * class_3532.method_15363((float)b.method_5412(), (float)0.0f, (float)1.0f));
            if (fw > 0) {
                c.method_25294(x0, y0, x0 + fw, y0 + 5, BossbarModule.color(b.method_5420()));
            }
            y += 26;
            ++i;
        }
    }

    private static int color(class_1259.class_1260 color) {
        if (color == class_1259.class_1260.field_5788) {
            return -1023342;
        }
        if (color == class_1259.class_1260.field_5780) {
            return -12877066;
        }
        if (color == class_1259.class_1260.field_5784) {
            return -1754827;
        }
        if (color == class_1259.class_1260.field_5785) {
            return -12345273;
        }
        if (color == class_1259.class_1260.field_5782) {
            return -141259;
        }
        if (color == class_1259.class_1260.field_5783) {
            return -7461718;
        }
        return -2039584;
    }
}

