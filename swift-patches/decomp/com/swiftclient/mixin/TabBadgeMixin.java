/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_355
 *  net.minecraft.class_5250
 *  net.minecraft.class_640
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.swiftclient.mixin;

import com.swiftclient.modules.ModuleManager;
import com.swiftclient.render.BadgeModule;
import net.minecraft.class_2561;
import net.minecraft.class_355;
import net.minecraft.class_5250;
import net.minecraft.class_640;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_355.class})
public abstract class TabBadgeMixin {
    @Inject(method={"method_1918(Lnet/minecraft/class_640;)Lnet/minecraft/class_2561;"}, at={@At(value="RETURN")}, cancellable=true, require=0)
    private void swift$tabBadge(class_640 class_6402, CallbackInfoReturnable<class_2561> callbackInfoReturnable) {
        try {
            class_2561 class_25612 = (class_2561)callbackInfoReturnable.getReturnValue();
            if (class_25612 == null) {
                return;
            }
            BadgeModule badgeModule = ModuleManager.get(BadgeModule.class);
            if (badgeModule == null || !badgeModule.enabled()) {
                return;
            }
            class_5250 class_52502 = class_2561.method_43470((String)"\u00a7a[T] ");
            callbackInfoReturnable.setReturnValue((Object)class_52502.method_10852(class_25612));
        }
        catch (Throwable throwable) {
            System.err.println("[swiftclient] tab badge failed: " + String.valueOf(throwable));
        }
    }
}

