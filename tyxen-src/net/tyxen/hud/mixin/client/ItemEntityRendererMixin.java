package net.tyxen.hud.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.modules.impl.render.ItemPhysicsModule;
import net.minecraft.class_10039;
import net.minecraft.class_916;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Item physics look (ItemPhysic-trailer style): dropped items lie flat and
 * rest still, each at its own fixed angle (the state's spawn-random offset)
 * instead of the vanilla fast upright twirl. Gated on ItemPhysicsModule —
 * toggle it in the ClickGUI.
 *
 * ModifyExpressionValue (MixinExtras) only needs the call result, so there
 * is no fragile local-variable or enclosing-arg matching; require = 0 keeps
 * vanilla rendering on any future Mojang reshuffle instead of crashing boot.
 */
@Environment(value = EnvType.CLIENT)
@Mixin(value = {class_916.class})
public abstract class ItemEntityRendererMixin {
    @ModifyExpressionValue(
        method = {"method_3996"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/class_7833;rotation(F)Lorg/joml/Quaternionf;"
        ),
        require = 0
    )
    private Quaternionf tyxen$flatSpin(Quaternionf original, @Local(argsOnly = true) class_10039 state) {
        if (!ItemPhysicsModule.isActive()) {
            return original;
        }
        // Lay flat, then rest at this item's own fixed angle (spawn-random
        // 0..2PI) — still, like real ItemPhysics. No world-time motion.
        return new Quaternionf().rotateY(state.field_53435).rotateX((float) Math.PI / 2.0f);
    }

    /**
     * Freeze the up-down bob: vanilla adds a sine wobble (second float
     * stored in render) on top of the base lift. Zero it so the item
     * rests touching the ground, perfectly still. require = 0 keeps
     * vanilla motion on any future Mojang reshuffle.
     */
    @ModifyVariable(
        method = {"method_3996"},
        at = @At(value = "STORE", ordinal = 1),
        require = 0
    )
    private float tyxen$stillBob(float original) {
        if (!ItemPhysicsModule.isActive()) {
            return original;
        }
        return 0.0f;
    }
}
