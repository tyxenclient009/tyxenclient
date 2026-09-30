/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.swiftclient.mixin;

import com.swiftclient.render.FreelookModule;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_1297.class})
public abstract class FreelookPlayerMixin {
    @Inject(method={"method_5872"}, at={@At(value="HEAD")}, cancellable=true)
    private void swift$freelook(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if (!FreelookModule.looking) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || this != mc.field_1724) {
            return;
        }
        FreelookModule.camYaw += (float)(cursorDeltaX * 0.15);
        FreelookModule.camPitch = class_3532.method_15363((float)(FreelookModule.camPitch + (float)(cursorDeltaY * 0.15)), (float)-90.0f, (float)90.0f);
        ci.cancel();
    }
}

