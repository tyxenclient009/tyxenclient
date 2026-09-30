/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_634
 *  net.minecraft.class_640
 */
package com.swiftclient.hud;

import com.swiftclient.config.ClientConfig;
import com.swiftclient.hud.HudUtil;
import com.swiftclient.modules.Category;
import com.swiftclient.modules.Module;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_634;
import net.minecraft.class_640;

public final class PingModule
extends Module {
    public PingModule() {
        super("Ping", Category.HUD, "Network latency display.", 0);
    }

    @Override
    public int defaultY() {
        return 124;
    }

    @Override
    public void render(class_332 c, float d) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) {
            return;
        }
        class_634 h = mc.method_1562();
        class_640 e = h == null ? null : h.method_2871(mc.field_1724.method_5667());
        int ping = e == null ? 0 : e.method_2959();
        HudUtil.label(c, mc.field_1772, "Ping: " + ping + " ms", ClientConfig.getX(this.name(), this.defaultX()), ClientConfig.getY(this.name(), this.defaultY()), this.name());
    }

    @Override
    public boolean hasBackground() {
        return true;
    }
}

