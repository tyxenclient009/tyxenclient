/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_315
 */
package com.swiftclient.player;

import com.swiftclient.config.ClientConfig;
import com.swiftclient.modules.Category;
import com.swiftclient.modules.Module;
import net.minecraft.class_310;
import net.minecraft.class_315;

public final class FovModule
extends Module {
    private int base = 90;

    public FovModule() {
        super("FOV", Category.PLAYER, "Independent FOV changer.", 0);
    }

    @Override
    public void onEnable() {
        class_315 o = class_310.method_1551().field_1690;
        if (o != null) {
            this.base = (Integer)o.method_41808().method_41753();
        }
    }

    @Override
    public void tick(class_310 mc) {
        class_315 o = mc.field_1690;
        if (o == null) {
            return;
        }
        int want = ClientConfig.getInt(this.name(), "fov", 90);
        if ((Integer)o.method_41808().method_41753() != want) {
            o.method_41808().method_41748((Object)want);
        }
    }

    @Override
    public void onDisable() {
        class_315 o = class_310.method_1551().field_1690;
        if (o != null) {
            o.method_41808().method_41748((Object)this.base);
        }
    }
}

