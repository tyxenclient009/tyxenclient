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

public final class GuiScaleModule
extends Module {
    private int prev = -1;

    public GuiScaleModule() {
        super("GUI Scale", Category.RENDER, "Independent GUI scale override.", 0);
    }

    @Override
    public void tick(class_310 mc) {
        class_315 o = mc.field_1690;
        if (o == null) {
            return;
        }
        int want = ClientConfig.getInt(this.name(), "scale", 2);
        if (this.prev < 0) {
            this.prev = (Integer)o.method_42474().method_41753();
        }
        if ((Integer)o.method_42474().method_41753() != want) {
            o.method_42474().method_41748((Object)want);
        }
    }

    @Override
    public void onDisable() {
        class_315 o = class_310.method_1551().field_1690;
        if (o != null && this.prev >= 0) {
            o.method_42474().method_41748((Object)this.prev);
        }
        this.prev = -1;
    }
}

