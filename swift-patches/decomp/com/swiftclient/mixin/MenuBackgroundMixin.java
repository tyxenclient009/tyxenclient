/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_429
 *  net.minecraft.class_437
 *  net.minecraft.class_500
 *  net.minecraft.class_526
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.swiftclient.mixin;

import com.swiftclient.util.Ui;
import net.minecraft.class_332;
import net.minecraft.class_429;
import net.minecraft.class_437;
import net.minecraft.class_500;
import net.minecraft.class_526;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_437.class})
public abstract class MenuBackgroundMixin {
    @Shadow
    public int field_22789;
    @Shadow
    public int field_22790;

    @Inject(method={"method_25394(Lnet/minecraft/class_332;IIF)V"}, at={@At(value="HEAD")}, require=0)
    private void swift$menuBg(class_332 class_3322, int n, int n2, float f, CallbackInfo callbackInfo) {
        MenuBackgroundMixin menuBackgroundMixin = this;
        if (menuBackgroundMixin instanceof class_500 || menuBackgroundMixin instanceof class_526 || menuBackgroundMixin instanceof class_429) {
            Ui.menuBg(class_3322, this.field_22789, this.field_22790);
        }
    }
}

