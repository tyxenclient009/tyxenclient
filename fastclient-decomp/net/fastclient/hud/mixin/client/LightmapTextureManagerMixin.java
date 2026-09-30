/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_765
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package net.fastclient.hud.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fastclient.hud.core.ModuleManager;
import net.fastclient.hud.modules.impl.render.FullbrightModule;
import net.minecraft.class_765;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_765.class})
public abstract class LightmapTextureManagerMixin {
    @WrapOperation(method={"method_3313"}, at={@At(value="INVOKE", target="Ljava/lang/Double;floatValue()F", ordinal=1)})
    private float changeGamma(Double instance, Operation<Float> original) {
        if (LightmapTextureManagerMixin.isFullbrightEnabled()) {
            return LightmapTextureManagerMixin.getFullbrightStrength();
        }
        return ((Float)original.call(new Object[]{instance})).floatValue();
    }

    private static boolean isFullbrightEnabled() {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm != null) {
            FullbrightModule fullbright = mm.getModule(FullbrightModule.class);
            return fullbright != null && fullbright.isEnabled();
        }
        return false;
    }

    private static float getFullbrightStrength() {
        FullbrightModule fullbright;
        ModuleManager mm = ModuleManager.getInstance();
        if (mm != null && (fullbright = mm.getModule(FullbrightModule.class)) != null) {
            return fullbright.getStrength();
        }
        return 10.0f;
    }
}

