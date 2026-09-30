/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_12077
 *  net.minecraft.class_243
 *  net.minecraft.class_4597
 *  net.minecraft.class_9976
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.tyxen.hud.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.core.ModuleManager;
import net.tyxen.hud.modules.impl.render.Particles;
import net.minecraft.class_12077;
import net.minecraft.class_243;
import net.minecraft.class_4597;
import net.minecraft.class_9976;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_9976.class})
public class WeatherRendererMixin {
    @Inject(method={"method_62320"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderWeather(class_4597 bufferSource, class_243 cameraPos, class_12077 state, CallbackInfo ci) {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) {
            return;
        }
        Particles module = mm.getModule(Particles.class);
        if (module == null || !module.isEnabled()) {
            return;
        }
        if (!module.shouldShowWeather()) {
            ci.cancel();
        }
    }
}

