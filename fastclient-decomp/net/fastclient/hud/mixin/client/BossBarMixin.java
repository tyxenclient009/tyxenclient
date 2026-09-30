/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_332
 *  net.minecraft.class_337
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.fastclient.hud.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fastclient.hud.core.ModuleManager;
import net.fastclient.hud.modules.impl.render.NoBossBar;
import net.minecraft.class_332;
import net.minecraft.class_337;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_337.class})
public class BossBarMixin {
    @Inject(method={"method_1796"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRender(class_332 graphics, CallbackInfo ci) {
        NoBossBar noBossBar;
        ModuleManager mm = ModuleManager.getInstance();
        if (mm != null && (noBossBar = mm.getModule(NoBossBar.class)) != null && noBossBar.isEnabled()) {
            ci.cancel();
        }
    }
}

