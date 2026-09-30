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
package net.tyxen.hud.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.appearance.PlayerAppearanceService;
import net.tyxen.hud.modules.impl.render.TyxenSkinsModule;
import com.mojang.authlib.GameProfile;
import net.minecraft.class_638;
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
    private void tyxen$resolveAppearance(CallbackInfoReturnable<class_8685> callback) {
        if (!TyxenSkinsModule.isActive()) {
            return;
        }
        class_742 player = (class_742)(Object)this;
        callback.setReturnValue(PlayerAppearanceService.getInstance().resolve(callback.getReturnValue(), player.method_7334().name()));
    }
}

