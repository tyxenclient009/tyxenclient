/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.tyxen.hud.modules.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.modules.Category;
import net.tyxen.hud.modules.Module;

@Environment(value=EnvType.CLIENT)
public class SprintModule
extends Module {
    public SprintModule() {
        super("Sprint", "Auto sprint when moving forward", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (!this.isInGame()) {
            return;
        }
        boolean moving = SprintModule.mc.field_1724.field_3913.method_20622();
        boolean sneaking = SprintModule.mc.field_1724.method_18276();
        int food = 20;
        try {
            food = SprintModule.mc.field_1724.method_7344().method_7586();
        }
        catch (Throwable t) {
        }
        if (moving && !sneaking && food > 6) {
            SprintModule.mc.field_1724.method_5728(true);
        } else {
            SprintModule.mc.field_1724.method_5728(false);
        }
    }
}

