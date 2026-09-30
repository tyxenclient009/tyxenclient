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

    public static void render(class_332 class_3322) {
        try {
            ScoreboardModule.renderInner(class_3322);
        }
        catch (Throwable throwable) {
            ModuleManager.fault(ModuleManager.get(ScoreboardModule.class), throwable, "render");
        }
    }

    private static void renderInner(class_332 class_3322) {
        int n;
        ArrayList<class_9011> arrayList;
        class_310 class_3102 = class_310.method_1551();
        if (class_3102.field_1687 == null || class_3102.field_1724 == null) {
            return;
        }
        class_269 class_2692 = class_3102.field_1687.method_8428();
        class_266 class_2662 = class_2692.method_1189(class_8646.field_45157);
        if (class_2662 == null || class_2662.method_1114() == null) {
            return;
        }
        ArrayList<class_9011> arrayList2 = new ArrayList<class_9011>(class_2692.method_1184(class_2662));
        arrayList2.removeIf(class_9011::method_55385);
        arrayList2.removeIf(class_90112 -> class_90112.comp_2129() == null);
        arrayList2.sort((class_90112, class_90113) -> Integer.compare(class_90113.comp_2128(), class_90112.comp_2128()));
        List<Object> list = arrayList = arrayList2.size() > 15 ? arrayList2.subList(0, 15) : arrayList2;
        if (arrayList.isEmpty()) {
            return;
        }
        class_327 class_3272 = class_3102.field_1772;
        int n2 = class_3272.method_27525((class_5348)class_2662.method_1114());
        for (class_9011 class_90114 : arrayList) {
            n = class_3272.method_27525((class_5348)class_90114.comp_2129()) + 6 + class_3272.method_1727(String.valueOf(class_90114.comp_2128()));
            if (n <= n2) continue;
            n2 = n;
        }
        int n3 = class_3322.method_51421();
        int n4 = class_3322.method_51443();
        n = n3 - n2 - 14;
        int n5 = arrayList.size() * 10 + 12;
        int n6 = n4 / 2 - n5 / 2;
        class_3322.method_25294(n - 2, n6 - 2, n + n2 + 12, n6 + n5 + 2, -1290792936);
        class_3322.method_25294(n - 2, n6 - 2, n + n2 + 12, n6 - 1, Theme.accent());
        class_3322.method_27534(class_3272, class_2662.method_1114(), n + n2 / 2 + 5, n6 + 2, -1);
        int n7 = n6 + 13;
        for (class_9011 class_90115 : arrayList) {
            class_3322.method_27535(class_3272, class_90115.comp_2129(), n, n7, -855305);
            String string = String.valueOf(class_90115.comp_2128());
            class_3322.method_25303(class_3272, string, n + n2 + 10 - class_3272.method_1727(string), n7, -41892);
            n7 += 10;
        }
    }
}

