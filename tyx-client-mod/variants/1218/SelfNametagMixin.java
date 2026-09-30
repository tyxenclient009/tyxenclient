package gg.tyx.client.mixin;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * VARIANT 1218 — F5 self-nametag, living-override half
 * (MC 1.21.1–1.21.8, old dispatcher pipeline).
 *
 * Vanilla hides the camera entity's own nameplate on every version, so in
 * F5 you see your body but no nametag above it. This flips the gate back
 * on for the local player — third-person only, never first-person (see
 * {@link SelfNametagBase} for the shared rules).
 *
 * Applies on 1.21.1 where the gate lives on this override.
 * {@code require = 0}: on 1.21.2–1.21.8 this override does not exist and
 * the base half applies instead.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class SelfNametagMixin {

    @Inject(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At("RETURN"), cancellable = true, require = 0)
    private void tyx$selfLabel(LivingEntity entity,
            CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && SelfNametagBase.allowSelf(entity)) {
            cir.setReturnValue(true);
        }
    }
}
