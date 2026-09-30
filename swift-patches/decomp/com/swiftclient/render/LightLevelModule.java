/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1944
 *  net.minecraft.class_2338
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package com.swiftclient.render;

import com.swiftclient.config.ClientConfig;
import com.swiftclient.hud.HudUtil;
import com.swiftclient.modules.Category;
import com.swiftclient.modules.Module;
import net.minecraft.class_1944;
import net.minecraft.class_2338;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class LightLevelModule
extends Module {
    public LightLevelModule() {
        super("Light Level", Category.RENDER, "Light-level spawn safety readout.", 0);
    }

    @Override
    public boolean hasPosition() {
        return true;
    }

    @Override
    public int defaultY() {
        return 190;
    }

    @Override
    public void render(class_332 c, float d) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || mc.field_1687 == null) {
            return;
        }
        class_2338 pos = mc.field_1724.method_24515();
        int b = mc.field_1687.method_8314(class_1944.field_9282, pos);
        int s = mc.field_1687.method_8314(class_1944.field_9284, pos);
        boolean dark = b == 0;
        HudUtil.label(c, mc.field_1772, "Light " + b + " / " + s + "  " + (dark ? "DARK" : "SAFE"), ClientConfig.getX(this.name(), this.defaultX()), ClientConfig.getY(this.name(), this.defaultY()), this.name());
    }

    @Override
    public boolean hasBackground() {
        return true;
    }
}

