package com.swiftclient.mixin;

import com.swiftclient.modules.ModuleManager;
import com.swiftclient.player.SelfNametagModule;
import net.minecraft.class_1309;
import net.minecraft.class_310;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * TYX addition — F5 self-nametag. Vanilla's LivingEntityRenderer.hasLabel
 * returns false for the camera entity (you) in every perspective, so your
 * own nameplate never renders. This flips the gate for the local player in
 * third-person only; every other vanilla hide-cause (HUD off, invisible,
 * riding, out of range) is mirrored so behavior stays vanilla otherwise.
 * The T badge beside it comes from NametagBadgeMixin once the label exists.
 */
@Mixin(value = {class_922.class})
public abstract class SelfNametagMixin {
    @Inject(method = {"method_4055(Lnet/minecraft/class_1309;D)Z"}, at = {
        @At(value = "RETURN")}, cancellable = true, require = 0)
    private void swift$selfLabel(class_1309 entity, double dist,
            CallbackInfoReturnable<Boolean> cir) {
        try {
            if (cir.getReturnValue()) {
                return;
            }
            if (dist >= 4096.0) {
                return;
            }
            SelfNametagModule mod = ModuleManager.get(SelfNametagModule.class);
            if (mod == null || !mod.enabled()) {
                return;
            }
            class_310 client = class_310.method_1551();
            if (client == null || client.field_1724 == null) {
                return;
            }
            if (entity != client.field_1724) {
                return;
            }
            if (client.field_1690 == null || client.field_1690.method_31044() == null
                    || client.field_1690.method_31044().method_31034()) {
                return;
            }
            if (!class_310.method_1498()) {
                return;
            }
            if (entity.method_5756(client.field_1724)) {
                return;
            }
            if (entity.method_5782()) {
                return;
            }
            cir.setReturnValue(true);
        } catch (Throwable t) {
            System.err.println("[swiftclient] self nametag failed: " + t);
        }
    }
}
