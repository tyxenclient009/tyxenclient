/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package com.swiftclient.performance;

import com.swiftclient.config.ClientConfig;
import com.swiftclient.hud.HudUtil;
import com.swiftclient.modules.Category;
import com.swiftclient.modules.Module;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class MemoryModule
extends Module {
    public MemoryModule() {
        super("Memory", Category.PERFORMANCE, "Live heap usage display.", 0);
    }

    @Override
    public boolean hasPosition() {
        return true;
    }

    @Override
    public int defaultY() {
        return 202;
    }

    @Override
    public void render(class_332 c, float d) {
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / 0x100000L;
        long max = rt.maxMemory() / 0x100000L;
        HudUtil.label(c, class_310.method_1551().field_1772, "Mem  " + used + " / " + max + " MB", ClientConfig.getX(this.name(), this.defaultX()), ClientConfig.getY(this.name(), this.defaultY()), this.name());
    }

    @Override
    public boolean hasBackground() {
        return true;
    }
}

