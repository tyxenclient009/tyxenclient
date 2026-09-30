/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_315
 */
package com.swiftclient.performance;

import com.swiftclient.config.ClientConfig;
import com.swiftclient.modules.Category;
import com.swiftclient.modules.Module;
import net.minecraft.class_310;
import net.minecraft.class_315;

public final class DynamicRenderDistanceModule
extends Module {
    private int ticks;
    private int prevVd = -1;

    public DynamicRenderDistanceModule() {
        super("Dynamic Render Distance", Category.PERFORMANCE, "FPS-aware render distance.", 0);
    }

    @Override
    public void onEnable() {
        class_315 o = class_310.method_1551().field_1690;
        if (o != null) {
            this.prevVd = (Integer)o.method_42503().method_41753();
        }
        this.ticks = 0;
    }

    @Override
    public void tick(class_310 mc) {
        class_315 o = mc.field_1690;
        if (o == null || ++this.ticks % 20 != 0) {
            return;
        }
        int fps = mc.method_47599();
        int target = ClientConfig.getInt(this.name(), "target", 60);
        int min = Math.max(2, ClientConfig.getInt(this.name(), "min", 2));
        int max = Math.min(32, Math.max(min, ClientConfig.getInt(this.name(), "max", 32)));
        int vd = (Integer)o.method_42503().method_41753();
        if (fps < target - 5 && vd > min) {
            o.method_42503().method_41748((Object)(vd - 1));
        } else if (fps > target + 10 && vd < max) {
            o.method_42503().method_41748((Object)(vd + 1));
        }
    }

    @Override
    public void onDisable() {
        class_315 o = class_310.method_1551().field_1690;
        if (o != null && this.prevVd > 0) {
            o.method_42503().method_41748((Object)this.prevVd);
        }
        this.prevVd = -1;
    }
}

