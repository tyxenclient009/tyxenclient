/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 *  net.minecraft.class_310
 *  net.minecraft.class_315
 *  net.minecraft.class_3675
 */
package com.swiftclient.render;

import com.swiftclient.config.ClientConfig;
import com.swiftclient.modules.Category;
import com.swiftclient.modules.Module;
import com.swiftclient.modules.ModuleManager;
import com.swiftclient.player.FovModule;
import net.minecraft.class_1041;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_3675;

public final class ZoomModule
extends Module {
    private int baseFov = 70;
    private float cur = 70.0f;

    public ZoomModule() {
        super("Zoom", Category.RENDER, "Hold C for smooth zoom.", 67);
    }

    @Override
    public void onEnable() {
        class_315 o = class_310.method_1551().field_1690;
        if (o != null) {
            this.baseFov = (Integer)o.method_41808().method_41753();
            this.cur = this.baseFov;
        }
    }

    @Override
    public void tick(class_310 mc) {
        class_315 o = mc.field_1690;
        if (o == null) {
            return;
        }
        FovModule fov = ModuleManager.get(FovModule.class);
        if (fov != null && fov.enabled()) {
            return;
        }
        boolean down = mc.field_1724 != null && class_3675.method_15987((class_1041)mc.method_22683(), (int)this.key());
        int zoomFov = ClientConfig.getInt(this.name(), "strength", 30);
        float target = down ? (float)zoomFov : (float)this.baseFov;
        this.cur += (target - this.cur) * 0.35f;
        if (Math.abs(target - this.cur) < 0.5f) {
            this.cur = target;
        }
        int v = Math.round(this.cur);
        if ((Integer)o.method_41808().method_41753() != v) {
            o.method_41808().method_41748((Object)v);
        }
    }

    @Override
    public void onDisable() {
        class_315 o = class_310.method_1551().field_1690;
        if (o != null) {
            o.method_41808().method_41748((Object)this.baseFov);
        }
    }
}

