/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1937
 *  net.minecraft.class_310
 *  net.minecraft.class_4184
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.swiftclient.mixin;

import com.swiftclient.render.FreelookModule;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_4184.class})
public abstract class FreelookCameraMixin {
    @Shadow
    private float field_18718;
    @Shadow
    private float field_18717;

    @Inject(method={"method_19321"}, at={@At(value="TAIL")})
    private void swift$freelook(class_1937 world, class_1297 focusedEntity, boolean thirdPerson, boolean inverseView, float tickProgress, CallbackInfo ci) {
        if (!FreelookModule.looking) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || mc.field_1687 == null) {
            return;
        }
        this.field_18718 = FreelookModule.camYaw;
        this.field_18717 = FreelookModule.camPitch;
    }
}

