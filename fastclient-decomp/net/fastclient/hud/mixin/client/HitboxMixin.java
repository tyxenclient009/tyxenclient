/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_12155
 *  net.minecraft.class_12180
 *  net.minecraft.class_1297
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.fastclient.hud.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fastclient.hud.core.ModuleManager;
import net.fastclient.hud.modules.impl.render.HitboxModule;
import net.minecraft.class_12155;
import net.minecraft.class_12180;
import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_12155.class})
public class HitboxMixin {
    @Shadow
    @Final
    private class_310 field_63589;

    @Inject(method={"method_75432"}, at={@At(value="HEAD")}, cancellable=true)
    private void onShowHitboxes(class_1297 entity, float tickDelta, boolean showVehicle, CallbackInfo ci) {
        HitboxModule hitboxModule;
        ModuleManager mm = ModuleManager.getInstance();
        if (mm != null && (hitboxModule = mm.getModule(HitboxModule.class)) != null && hitboxModule.isEnabled()) {
            this.renderCustomHitboxGizmo(entity, tickDelta, hitboxModule);
            if (showVehicle && entity.method_5854() != null) {
                this.renderCustomHitboxGizmo(entity.method_5854(), tickDelta, hitboxModule);
            }
            ci.cancel();
        }
    }

    private void renderCustomHitboxGizmo(class_1297 entity, float tickDelta, HitboxModule module) {
        class_238 box = entity.method_5829();
        int boxColor = module.getBoxColorARGB();
        int eyeColor = module.getEyeHeightColorARGB();
        int lookColor = module.getLookDirColorARGB();
        float lineWidth = 1.0f;
        class_12180.method_75546((class_243)new class_243(box.field_1323, box.field_1322, box.field_1321), (class_243)new class_243(box.field_1320, box.field_1322, box.field_1321), (int)boxColor, (float)lineWidth);
        class_12180.method_75546((class_243)new class_243(box.field_1323, box.field_1322, box.field_1321), (class_243)new class_243(box.field_1323, box.field_1322, box.field_1324), (int)boxColor, (float)lineWidth);
        class_12180.method_75546((class_243)new class_243(box.field_1320, box.field_1322, box.field_1321), (class_243)new class_243(box.field_1320, box.field_1322, box.field_1324), (int)boxColor, (float)lineWidth);
        class_12180.method_75546((class_243)new class_243(box.field_1323, box.field_1322, box.field_1324), (class_243)new class_243(box.field_1320, box.field_1322, box.field_1324), (int)boxColor, (float)lineWidth);
        class_12180.method_75546((class_243)new class_243(box.field_1323, box.field_1325, box.field_1321), (class_243)new class_243(box.field_1320, box.field_1325, box.field_1321), (int)boxColor, (float)lineWidth);
        class_12180.method_75546((class_243)new class_243(box.field_1323, box.field_1325, box.field_1321), (class_243)new class_243(box.field_1323, box.field_1325, box.field_1324), (int)boxColor, (float)lineWidth);
        class_12180.method_75546((class_243)new class_243(box.field_1320, box.field_1325, box.field_1321), (class_243)new class_243(box.field_1320, box.field_1325, box.field_1324), (int)boxColor, (float)lineWidth);
        class_12180.method_75546((class_243)new class_243(box.field_1323, box.field_1325, box.field_1324), (class_243)new class_243(box.field_1320, box.field_1325, box.field_1324), (int)boxColor, (float)lineWidth);
        class_12180.method_75546((class_243)new class_243(box.field_1323, box.field_1322, box.field_1321), (class_243)new class_243(box.field_1323, box.field_1325, box.field_1321), (int)boxColor, (float)lineWidth);
        class_12180.method_75546((class_243)new class_243(box.field_1320, box.field_1322, box.field_1321), (class_243)new class_243(box.field_1320, box.field_1325, box.field_1321), (int)boxColor, (float)lineWidth);
        class_12180.method_75546((class_243)new class_243(box.field_1323, box.field_1322, box.field_1324), (class_243)new class_243(box.field_1323, box.field_1325, box.field_1324), (int)boxColor, (float)lineWidth);
        class_12180.method_75546((class_243)new class_243(box.field_1320, box.field_1322, box.field_1324), (class_243)new class_243(box.field_1320, box.field_1325, box.field_1324), (int)boxColor, (float)lineWidth);
        if (module.shouldShowEyeHeight()) {
            double eyeY = entity.method_23318() + (double)entity.method_5751();
            double halfWidth = (double)entity.method_17681() / 2.0;
            class_243 pos = entity.method_73189();
            class_12180.method_75546((class_243)new class_243(pos.field_1352 - halfWidth, eyeY, pos.field_1350 - halfWidth), (class_243)new class_243(pos.field_1352 + halfWidth, eyeY, pos.field_1350 + halfWidth), (int)eyeColor, (float)lineWidth);
            class_12180.method_75546((class_243)new class_243(pos.field_1352 - halfWidth, eyeY, pos.field_1350 + halfWidth), (class_243)new class_243(pos.field_1352 + halfWidth, eyeY, pos.field_1350 - halfWidth), (int)eyeColor, (float)lineWidth);
        }
        if (module.shouldShowLookVector()) {
            class_243 eyePos = entity.method_5836(tickDelta);
            class_243 look = entity.method_5828(tickDelta);
            class_243 lookEnd = eyePos.method_1019(look.method_1021(2.0));
            class_12180.method_75546((class_243)eyePos, (class_243)lookEnd, (int)lookColor, (float)lineWidth);
        }
    }
}

