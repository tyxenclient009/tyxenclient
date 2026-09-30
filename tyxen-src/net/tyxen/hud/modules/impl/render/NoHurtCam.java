/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.tyxen.hud.modules.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.modules.Category;
import net.tyxen.hud.modules.Module;

@Environment(value=EnvType.CLIENT)
public class NoHurtCam
extends Module {
    public NoHurtCam() {
        super("NoHurtCam", "Disables camera shake when taking damage", Category.RENDER);
    }
}

