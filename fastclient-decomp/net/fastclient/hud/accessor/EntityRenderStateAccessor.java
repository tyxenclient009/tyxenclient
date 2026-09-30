/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1297
 */
package net.fastclient.hud.accessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;

@Environment(value=EnvType.CLIENT)
public interface EntityRenderStateAccessor {
    public void fastclient$setEntity(class_1297 var1);

    public class_1297 fastclient$getEntity();
}

