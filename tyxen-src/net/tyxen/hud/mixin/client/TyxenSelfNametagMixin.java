package net.tyxen.hud.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
 * own nameplate (with the Tyxen badge from EntityRendererMixin) never
 * renders. This flips the gate for the local player in third-person only;
 * every other vanilla hide-cause is mirrored so behavior stays vanilla
 * otherwise. Always on in third person (no toggle).
 */
@Environment(value = EnvType.CLIENT)
@Mixin(value = {class_922.class})
public abstract class TyxenSelfNametagMixin {
    @Inject(method = {"method_4055(Lnet/minecraft/class_1309;D)Z"}, at = {
        @At(value = "RETURN")}, cancellable = true, require = 0)
    private void tyxen$selfLabel(class_1309 entity, double dist,
            CallbackInfoReturnable<Boolean> cir) {
        try {
            if (cir.getReturnValue()) {
                return;
            }
            if (dist >= 4096.0) {
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
            System.err.println("[tyxen] self nametag failed: " + t);
        }
    }
}
