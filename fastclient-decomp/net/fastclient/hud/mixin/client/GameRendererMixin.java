/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_4587
 *  net.minecraft.class_757
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.fastclient.hud.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fastclient.hud.core.ModuleManager;
import net.fastclient.hud.modules.impl.render.NoHurtCam;
import net.minecraft.class_4587;
import net.minecraft.class_757;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_757.class})
public class GameRendererMixin {
    @Inject(method={"method_3198"}, at={@At(value="HEAD")}, cancellable=true)
    private void onBobHurt(class_4587 poseStack, float f, CallbackInfo ci) {
        NoHurtCam noHurtCam;
        ModuleManager mm = ModuleManager.getInstance();
        if (mm != null && (noHurtCam = mm.getModule(NoHurtCam.class)) != null && noHurtCam.isEnabled()) {
            ci.cancel();
        }
    }
}

