/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.fastclient.hud.modules.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fastclient.hud.modules.Category;
import net.fastclient.hud.modules.Module;

@Environment(value=EnvType.CLIENT)
public class NoHurtCam
extends Module {
    public NoHurtCam() {
        super("NoHurtCam", "Disables camera shake when taking damage", Category.RENDER);
    }
}

