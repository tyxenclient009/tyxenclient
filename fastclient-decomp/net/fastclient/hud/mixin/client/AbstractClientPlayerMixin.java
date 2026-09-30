/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_742
 *  net.minecraft.class_8685
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.fastclient.hud.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fastclient.hud.appearance.PlayerAppearanceService;
import net.fastclient.hud.modules.impl.render.FastClientSkinsModule;
import net.minecraft.class_742;
import net.minecraft.class_8685;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_742.class})
public abstract class AbstractClientPlayerMixin {
    @Inject(method={"method_52814"}, at={@At(value="RETURN")}, cancellable=true)
    private void fastclient$resolveAppearance(CallbackInfoReturnable<class_8685> callback) {
        if (!FastClientSkinsModule.isActive()) {
            return;
        }
        class_742 player = (class_742)this;
        callback.setReturnValue((Object)PlayerAppearanceService.getInstance().resolve((class_8685)callback.getReturnValue(), player.method_7334().name()));
    }
}

