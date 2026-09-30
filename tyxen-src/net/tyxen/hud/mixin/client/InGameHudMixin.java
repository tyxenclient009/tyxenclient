/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_310
 *  net.minecraft.class_329
 *  net.minecraft.class_332
 *  net.minecraft.class_9779
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.tyxen.hud.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.TyxenHUDClient;
import net.tyxen.hud.core.ModuleManager;
import net.tyxen.hud.modules.impl.render.Crosshair;
import net.minecraft.class_310;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_329.class})
public class InGameHudMixin {
    @Shadow
    @Final
    private class_310 field_2035;

    @Inject(method={"method_1736"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderCrosshair(class_332 graphics, class_9779 deltaTracker, CallbackInfo ci) {
        Crosshair crosshairModule;
        ModuleManager mm = ModuleManager.getInstance();
        if (mm != null && (crosshairModule = mm.getModule(Crosshair.class)) != null && crosshairModule.isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method={"method_1753"}, at={@At(value="TAIL")})
    private void onRenderHud(class_332 graphics, class_9779 deltaTracker, CallbackInfo ci) {
        if (this.field_2035.field_1690.field_1842) {
            return;
        }
        TyxenHUDClient instance = TyxenHUDClient.getInstance();
        if (instance != null && instance.getRenderManager() != null) {
            instance.getRenderManager().onRenderHudDirect(graphics, deltaTracker);
        }
    }
}

