package com.swiftclient.mixin;

import com.swiftclient.modules.ModuleManager;
import com.swiftclient.render.BadgeModule;
import net.minecraft.class_2561;
import net.minecraft.class_355;
import net.minecraft.class_640;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * TYX addition — T marker before every name in the Tab player list
 * (including your own), tied to the Nametag Badge module toggle.
 * Hooks getPlayerName itself, so vanilla measures columns with the prefix
 * included and the layout stays pixel-perfect on every version.
 */
@Mixin(value = {class_355.class})
public abstract class TabBadgeMixin {
    @Inject(method = {
        "method_1918(Lnet/minecraft/class_640;)Lnet/minecraft/class_2561;"}, at = {
            @At(value = "RETURN")}, cancellable = true, require = 0)
    private void swift$tabBadge(class_640 entry, CallbackInfoReturnable<class_2561> cir) {
        try {
            class_2561 original = cir.getReturnValue();
            if (original == null) {
                return;
            }
            BadgeModule mod = ModuleManager.get(BadgeModule.class);
            if (mod == null || !mod.enabled()) {
                return;
            }
            net.minecraft.class_5250 prefix = class_2561.method_43470("§a[T] ");
            cir.setReturnValue(prefix.method_10852(original));
        } catch (Throwable t) {
            System.err.println("[swiftclient] tab badge failed: " + t);
        }
    }
}
