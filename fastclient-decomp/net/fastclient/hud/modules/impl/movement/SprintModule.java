/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.fastclient.hud.modules.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fastclient.hud.modules.Category;
import net.fastclient.hud.modules.Module;

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
        if (SprintModule.mc.field_1724.field_3913.method_20622() && !SprintModule.mc.field_1724.method_18276()) {
            SprintModule.mc.field_1724.method_5728(true);
        }
    }
}

