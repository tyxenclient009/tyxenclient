/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10017
 *  net.minecraft.class_1297
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 */
package net.fastclient.hud.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fastclient.hud.accessor.EntityRenderStateAccessor;
import net.minecraft.class_10017;
import net.minecraft.class_1297;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_10017.class})
public class EntityRenderStateMixin
implements EntityRenderStateAccessor {
    @Unique
    private class_1297 fastclient$entity;

    @Override
    public void fastclient$setEntity(class_1297 entity) {
        this.fastclient$entity = entity;
    }

    @Override
    public class_1297 fastclient$getEntity() {
        return this.fastclient$entity;
    }
}

