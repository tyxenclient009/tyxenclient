package gg.tyx.client.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * VARIANT 1218 — "[TYXEN]" prefix for the F5 self-nametag
 * (MC 1.21.1–1.21.8, old dispatcher pipeline).
 *
 * The old pipeline feeds nameplates straight from Entity.getDisplayName,
 * so the prefix hooks here — self player in third-person only (same rules
 * as the label gate in {@link SelfNametagBase}). Render-cosmetic only:
 * death messages and server logic read other accessors.
 */
@Mixin(Entity.class)
public abstract class SelfPrefixMixin {

    @Inject(method = "getDisplayName()Lnet/minecraft/text/Text;",
            at = @At("RETURN"), cancellable = true)
    private void tyx$prefix(CallbackInfoReturnable<Text> cir) {
        Text original = cir.getReturnValue();
        if (original == null) return;
        Entity self = (Entity) (Object) this;
        if (!SelfNametagBase.allowSelf(self)) return;
        cir.setReturnValue(Text.literal("§b[TYXEN] §r").append(original));
    }
}
