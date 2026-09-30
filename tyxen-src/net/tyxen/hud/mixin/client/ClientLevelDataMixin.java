/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_638$class_5271
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.tyxen.hud.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.core.ModuleManager;
import net.tyxen.hud.modules.impl.render.TimeChanger;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_638.class_5271.class})
public class ClientLevelDataMixin {
    @Inject(method={"method_217"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetDayTime(CallbackInfoReturnable<Long> cir) {
        TimeChanger timeChanger;
        ModuleManager mm = ModuleManager.getInstance();
        if (mm != null && (timeChanger = mm.getModule(TimeChanger.class)) != null && timeChanger.isEnabled()) {
            cir.setReturnValue(timeChanger.getCustomTime());
        }
    }
}

