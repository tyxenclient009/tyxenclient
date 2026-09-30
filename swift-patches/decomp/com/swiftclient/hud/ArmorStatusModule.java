/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1304
 *  net.minecraft.class_1799
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package com.swiftclient.hud;

import com.swiftclient.config.ClientConfig;
import com.swiftclient.hud.HudUtil;
import com.swiftclient.modules.Category;
import com.swiftclient.modules.Module;
import com.swiftclient.util.SwiftText;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class ArmorStatusModule
extends Module {
    public ArmorStatusModule() {
        super("Armor Status", Category.HUD, "Armor durability and item icons.", 0);
    }

    @Override
    public int defaultY() {
        return 160;
    }

    @Override
    public void render(class_332 c, float d) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) {
            return;
        }
        boolean dura = ClientConfig.getBool(this.name(), "durability", true);
        int x = ClientConfig.getX(this.name(), this.defaultX());
        int y = ClientConfig.getY(this.name(), this.defaultY());
        class_1304[] slots = new class_1304[]{class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166};
        for (int i = 0; i < 4; ++i) {
            class_1799 stack = mc.field_1724.method_6118(slots[i]);
            int ix = x + i * 20;
            if (!stack.method_7960()) {
                c.method_51427(stack, ix, y);
            }
            if (!dura || !stack.method_7963()) continue;
            int rem = stack.method_7936() - stack.method_7919();
            c.method_27535(mc.field_1772, SwiftText.of(String.valueOf(rem)), ix + 2, y + 17, rem > 0 ? -855305 : -41892);
        }
        HudUtil.BOUNDS.put(this.name(), new int[]{x, y, 80, 28});
    }

    @Override
    public int hudWidth(class_310 mc) {
        return 80;
    }

    @Override
    public int hudHeight(class_310 mc) {
        return 28;
    }
}

