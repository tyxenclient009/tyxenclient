/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_266
 *  net.minecraft.class_269
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_5348
 *  net.minecraft.class_8646
 *  net.minecraft.class_9011
 */
package com.swiftclient.restyle;

import com.swiftclient.config.Theme;
import com.swiftclient.modules.Category;
import com.swiftclient.modules.Module;
import com.swiftclient.modules.ModuleManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_266;
import net.minecraft.class_269;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_5348;
import net.minecraft.class_8646;
import net.minecraft.class_9011;

public final class ScoreboardModule
extends Module {
    public ScoreboardModule() {
        super("Scoreboard", Category.HUD, "Restyled scoreboard.", 0);
    }

    public static void render(class_332 c) {
        try {
            ScoreboardModule.renderInner(c);
        } catch (Throwable t) {
            ModuleManager.fault(ModuleManager.get(ScoreboardModule.class), t, "render");
        }
    }

    private static void renderInner(class_332 c) {
        List<class_9011> rows;
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1724 == null) {
            return;
        }
        class_269 board = mc.field_1687.method_8428();
        class_266 obj = board.method_1189(class_8646.field_45157);
        if (obj == null || obj.method_1114() == null) {
            return;
        }
        ArrayList<class_9011> list = new ArrayList<class_9011>(board.method_1184(obj));
        list.removeIf(class_9011::method_55385);
        list.removeIf((class_9011 e) -> e.comp_2129() == null);
        list.sort((a, b) -> Integer.compare(b.comp_2128(), a.comp_2128()));
        rows = list.size() > 15 ? list.subList(0, 15) : list;
        if (rows.isEmpty()) {
            return;
        }
        class_327 tr = mc.field_1772;
        int w = tr.method_27525((class_5348)obj.method_1114());
        for (class_9011 e : rows) {
            int row = tr.method_27525((class_5348)e.comp_2129()) + 6 + tr.method_1727(String.valueOf(e.comp_2128()));
            if (row <= w) continue;
            w = row;
        }
        int sw = c.method_51421();
        int sh = c.method_51443();
        int x1 = sw - w - 14;
        int y = rows.size() * 10 + 12;
        int y0 = sh / 2 - y / 2;
        c.method_25294(x1 - 2, y0 - 2, x1 + w + 12, y0 + y + 2, -1290792936);
        c.method_25294(x1 - 2, y0 - 2, x1 + w + 12, y0 - 1, Theme.accent());
        c.method_27534(tr, obj.method_1114(), x1 + w / 2 + 5, y0 + 2, -1);
        int yy = y0 + 13;
        for (class_9011 e : rows) {
            c.method_27535(tr, e.comp_2129(), x1, yy, -855305);
            String v = String.valueOf(e.comp_2128());
            c.method_25303(tr, v, x1 + w + 10 - tr.method_1727(v), yy, -41892);
            yy += 10;
        }
    }
}

