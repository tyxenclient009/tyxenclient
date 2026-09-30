/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_746
 */
package com.swiftclient.hud;

import com.swiftclient.config.ClientConfig;
import com.swiftclient.hud.HudUtil;
import com.swiftclient.modules.Category;
import com.swiftclient.modules.Module;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_746;

public final class CoordinatesModule
extends Module {
    public CoordinatesModule() {
        super("Coordinates", Category.HUD, "Player XYZ coordinates.", 0, true);
    }

    @Override
    public int defaultY() {
        return 20;
    }

    @Override
    public void render(class_332 c, float d) {
        class_746 p = class_310.method_1551().field_1724;
        if (p == null) {
            return;
        }
        boolean axis = ClientConfig.getBool(this.name(), "axis", true);
        String s = axis ? String.format("XYZ: %d / %d / %d", p.method_31477(), p.method_31478(), p.method_31479()) : String.format("%d / %d / %d", p.method_31477(), p.method_31478(), p.method_31479());
        HudUtil.label(c, class_310.method_1551().field_1772, s, ClientConfig.getX(this.name(), this.defaultX()), ClientConfig.getY(this.name(), this.defaultY()), this.name());
    }

    @Override
    public boolean hasBackground() {
        return true;
    }
}

