/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_315
 *  net.minecraft.class_332
 */
package com.swiftclient.player;

import com.swiftclient.config.ClientConfig;
import com.swiftclient.hud.HudUtil;
import com.swiftclient.modules.Category;
import com.swiftclient.modules.Module;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_332;

public final class ToggleSprintModule
extends Module {
    public ToggleSprintModule() {
        super("Toggle Sprint", Category.PLAYER, "Toggle sprint state.", 0);
    }

    @Override
    public boolean hasPosition() {
        return true;
    }

    @Override
    public int defaultY() {
        return 226;
    }

    @Override
    public void render(class_332 c, float d) {
        HudUtil.label(c, class_310.method_1551().field_1772, "[Sprinting]", ClientConfig.getX(this.name(), this.defaultX()), ClientConfig.getY(this.name(), this.defaultY()), this.name());
    }

    @Override
    public void tick(class_310 mc) {
        if (mc.field_1724 != null) {
            mc.field_1690.field_1867.method_23481(true);
        }
    }

    @Override
    public void onDisable() {
        class_315 o = class_310.method_1551().field_1690;
        if (o != null) {
            o.field_1867.method_23481(false);
        }
    }
}

