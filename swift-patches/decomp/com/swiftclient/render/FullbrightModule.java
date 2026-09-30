/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_315
 */
package com.swiftclient.render;

import com.swiftclient.config.ClientConfig;
import com.swiftclient.modules.Category;
import com.swiftclient.modules.Module;
import net.minecraft.class_310;
import net.minecraft.class_315;

public final class FullbrightModule
extends Module {
    private double prev = -1.0;

    public FullbrightModule() {
        super("Fullbright", Category.RENDER, "Maximum brightness vision.", 0);
    }

    @Override
    public void tick(class_310 mc) {
        class_315 o = mc.field_1690;
        if (o == null) {
            return;
        }
        double want = ClientConfig.getInt(this.name(), "strength", 10);
        if (this.prev < 0.0) {
            this.prev = (Double)o.method_42473().method_41753();
        }
        if ((Double)o.method_42473().method_41753() != want) {
            o.method_42473().method_41748((Object)want);
        }
    }

    @Override
    public void onDisable() {
        class_315 o = class_310.method_1551().field_1690;
        if (o != null && this.prev >= 0.0) {
            o.method_42473().method_41748((Object)this.prev);
        }
        this.prev = -1.0;
    }
}

