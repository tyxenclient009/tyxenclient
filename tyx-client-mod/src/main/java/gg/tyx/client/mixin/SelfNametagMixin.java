package gg.tyx.client.mixin;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * F5 self-nametag, living-override half (modern 1.21.9+ pipeline).
 *
 * Vanilla hides the camera entity's own nameplate on every version, so in
 * F5 you see your body but no nametag above it and no Tyx badge beside it.
 * This flips the gate back on for the local player — third-person only,
 * never first-person (see {@link SelfNametagBase} for the shared rules).
 *
 * Applies on 1.21.11 where the gate lives on this override (the player
 * path calls it via super). {@code require = 0}: on 1.21.9/1.21.10 this
 * override does not exist and the base half applies instead.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class SelfNametagMixin {

    @Inject(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z",
            at = @At("RETURN"), cancellable = true, require = 0)
    private void tyx$selfLabel(LivingEntity entity, double dist,
            CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && SelfNametagBase.allowSelf(entity, dist)) {
            cir.setReturnValue(true);
        }
    }
}
