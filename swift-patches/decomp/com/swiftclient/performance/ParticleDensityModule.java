/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_315
 *  net.minecraft.class_4066
 */
package com.swiftclient.performance;

import com.swiftclient.config.ClientConfig;
import com.swiftclient.modules.Category;
import com.swiftclient.modules.Module;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_4066;

public final class ParticleDensityModule
extends Module {
    private class_4066 prev;

    public ParticleDensityModule() {
        super("Particle Density", Category.PERFORMANCE, "Particle reduction slider.", 0);
    }

    @Override
    public void tick(class_310 mc) {
        class_4066 want;
        class_315 o = mc.field_1690;
        if (o == null) {
            return;
        }
        int v = ClientConfig.getInt(this.name(), "amount", 1);
        class_4066 class_40662 = v <= 0 ? class_4066.field_18197 : (want = v == 1 ? class_4066.field_18198 : class_4066.field_18199);
        if (this.prev == null) {
            this.prev = (class_4066)o.method_42475().method_41753();
        }
        if (o.method_42475().method_41753() != want) {
            o.method_42475().method_41748((Object)want);
        }
    }

    @Override
    public void onDisable() {
        class_315 o = class_310.method_1551().field_1690;
        if (o != null && this.prev != null) {
            o.method_42475().method_41748((Object)this.prev);
        }
        this.prev = null;
    }
}

