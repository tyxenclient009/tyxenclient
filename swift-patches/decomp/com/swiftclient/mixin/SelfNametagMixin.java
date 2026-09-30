/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_310
 *  net.minecraft.class_922
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.swiftclient.mixin;

import com.swiftclient.modules.ModuleManager;
import com.swiftclient.player.SelfNametagModule;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_922.class})
public abstract class SelfNametagMixin {
    @Inject(method={"method_4055(Lnet/minecraft/class_1309;D)Z"}, at={@At(value="RETURN")}, cancellable=true, require=0)
    private void swift$selfLabel(class_1309 class_13092, double d, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        try {
            if (((Boolean)callbackInfoReturnable.getReturnValue()).booleanValue()) {
                return;
            }
            if (d >= 4096.0) {
                return;
            }
            SelfNametagModule selfNametagModule = ModuleManager.get(SelfNametagModule.class);
            if (selfNametagModule == null || !selfNametagModule.enabled()) {
                return;
            }
            class_310 class_3102 = class_310.method_1551();
            if (class_3102 == null || class_3102.field_1724 == null) {
                return;
            }
            if (class_13092 != class_3102.field_1724) {
                return;
            }
            if (class_3102.field_1690 == null || class_3102.field_1690.method_31044() == null || class_3102.field_1690.method_31044().method_31034()) {
                return;
            }
            if (!class_310.method_1498()) {
                return;
            }
            if (class_13092.method_5756((class_1657)class_3102.field_1724)) {
                return;
            }
            if (class_13092.method_5782()) {
                return;
            }
            callbackInfoReturnable.setReturnValue((Object)true);
        }
        catch (Throwable throwable) {
            System.err.println("[swiftclient] self nametag failed: " + String.valueOf(throwable));
        }
    }
}

