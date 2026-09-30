/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1074
 *  net.minecraft.class_310
 *  net.minecraft.class_320
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.vidtu.ias.mixins;

import net.minecraft.class_1074;
import net.minecraft.class_310;
import net.minecraft.class_320;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.vidtu.ias.config.IASConfig;

@Mixin(value={class_310.class})
public final class MinecraftMixin {
    @Shadow
    @Final
    private class_320 field_1726;

    private MinecraftMixin() {
        throw new AssertionError((Object)"No instances.");
    }

    @Inject(method={"method_24287"}, at={@At(value="RETURN")}, cancellable=true)
    private void ias$createTitle$return(CallbackInfoReturnable<String> cir) {
        if (!IASConfig.barNick || !class_1074.method_4663((String)"ias.bar") || this.field_1726 == null) {
            return;
        }
        String original = (String)cir.getReturnValue();
        cir.setReturnValue((Object)class_1074.method_4662((String)"ias.bar", (Object[])new Object[]{original, this.field_1726.method_1676()}));
    }
}

