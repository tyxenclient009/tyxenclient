package com.swiftclient.mixin;

import com.swiftclient.util.Ui;
import net.minecraft.class_332;
import net.minecraft.class_429;
import net.minecraft.class_437;
import net.minecraft.class_500;
import net.minecraft.class_526;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * TYX addition — one shared menu backdrop for every list/settings/pause
 * screen (singleplayer, multiplayer, options, pause), same artwork as the
 * main menu. Targets Screen.render itself because the individual screens
 * don't all override it; vanilla widgets and text keep drawing on top
 * untouched. Title has its own menu mixin and is skipped.
 */
@Mixin(value = {class_437.class})
public abstract class MenuBackgroundMixin {
    @Shadow
    public int field_22789;

    @Shadow
    public int field_22790;

    @Inject(method = {"method_25394(Lnet/minecraft/class_332;IIF)V"}, at = {@At(value = "HEAD")}, require = 0)
    private void swift$menuBg(class_332 ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Title-area menus only (singleplayer / multiplayer / options).
        // In-game screens (pause menu, ClickGUI and friends) intentionally
        // keep the vanilla look over the live world.
        Object self = this;
        if (self instanceof class_500 || self instanceof class_526 || self instanceof class_429) {
            Ui.menuBg(ctx, this.field_22789, this.field_22790);
        }
    }
}
