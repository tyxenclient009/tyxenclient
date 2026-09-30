/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package com.swiftclient.hud;

import com.swiftclient.config.ClientConfig;
import com.swiftclient.hud.HudUtil;
import com.swiftclient.modules.Category;
import com.swiftclient.modules.Module;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class DayCounterModule
extends Module {
    public DayCounterModule() {
        super("Day Counter", Category.HUD, "World day counter.", 0);
    }

    @Override
    public int defaultY() {
        return 148;
    }

    @Override
    public void render(class_332 c, float d) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null) {
            return;
        }
        long day = mc.field_1687.method_8532() / 24000L + 1L;
        HudUtil.label(c, mc.field_1772, "Day " + day, ClientConfig.getX(this.name(), this.defaultX()), ClientConfig.getY(this.name(), this.defaultY()), this.name());
    }

    @Override
    public boolean hasBackground() {
        return true;
    }
}

