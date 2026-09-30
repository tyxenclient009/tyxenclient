package gg.tyx.client.mixin;

import gg.tyx.client.TyxClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * VARIANT 1218 — F5 self-nametag, base-class half + shared rules
 * (MC 1.21.1–1.21.8, old dispatcher pipeline).
 *
 * Applies where the gate lives on the {@code EntityRenderer} base
 * (1.21.2–1.21.8). {@code require = 0}: on 1.21.1 the living override
 * ({@link SelfNametagMixin}) handles players and this half simply never
 * fires for them — every build applies exactly the inject it needs.
 */
@Mixin(EntityRenderer.class)
public abstract class SelfNametagBase {

    @Inject(method = "hasLabel(Lnet/minecraft/entity/Entity;D)Z",
            at = @At("RETURN"), cancellable = true, require = 0)
    private void tyx$selfLabel(Entity entity, double dist,
            CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && allowSelf(entity)) {
            cir.setReturnValue(true);
        }
    }

    /**
     * True only for the local player in third-person with HUD on, visible
     * and not riding — every other vanilla false-cause (F1 HUD off,
     * invisible, passengers) still hides the tag. (Self is always metres
     * from its own camera, so no distance gate is needed here.)
     */
    static boolean allowSelf(Entity entity) {
        if (!TyxClient.CONFIG.selfNametag) return false;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return false;
        if (entity != client.player) return false;
        // Third-person only: in first person your own head would fill the screen.
        if (client.gameRenderer == null || client.gameRenderer.getCamera() == null) return false;
        if (!client.gameRenderer.getCamera().isThirdPerson()) return false;
        if (!MinecraftClient.isHudEnabled()) return false;
        if (entity.isInvisibleTo(client.player)) return false;
        if (entity.hasPassengers()) return false;
        return true;
    }
}
