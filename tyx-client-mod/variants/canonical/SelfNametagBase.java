package gg.tyx.client.mixin;

import gg.tyx.client.TyxClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * F5 self-nametag, base-class half + shared rules (modern 1.21.9+ pipeline).
 *
 * Applies where the gate lives on the {@code EntityRenderer} base
 * (1.21.9/1.21.10). {@code require = 0}: on 1.21.11 the living override
 * ({@link SelfNametagMixin}) handles players and this half simply never
 * fires for them — every build applies exactly the inject it needs.
 */
@Mixin(EntityRenderer.class)
public abstract class SelfNametagBase {

    @Inject(method = "hasLabel(Lnet/minecraft/entity/Entity;D)Z",
            at = @At("RETURN"), cancellable = true, require = 0)
    private void tyx$selfLabel(Entity entity, double dist,
            CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && allowSelf(entity, dist)) {
            cir.setReturnValue(true);
        }
    }

    /**
     * "[TYXEN]" prefix for the F5 self-nametag. Hooks the renderer's label
     * text (the exact value vanilla assigns to the nameplate) — render-only,
     * never networked. Null means vanilla is out of label range, so it also
     * doubles as the range gate and dist 0.0 keeps every other rule intact.
     */
    @Inject(method = "getDisplayName(Lnet/minecraft/entity/Entity;)Lnet/minecraft/text/Text;",
            at = @At("RETURN"), cancellable = true)
    private void tyx$prefix(Entity entity, CallbackInfoReturnable<Text> cir) {
        Text original = cir.getReturnValue();
        if (original == null) return;
        if (!allowSelf(entity, 0.0)) return;
        cir.setReturnValue(Text.literal("§b[TYXEN] §r").append(original));
    }

    /**
     * True only for the local player in third-person with HUD on, visible
     * and not riding — every other vanilla false-cause (F1 HUD off,
     * invisible, passengers, sneaking-far) still hides the tag.
     */
    static boolean allowSelf(Entity entity, double dist) {
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
        double max = entity.isSneaky() ? 1024.0 : 4096.0;
        if (dist >= max) return false;
        return true;
    }
}
