package gg.tyx.client.mixin;

import gg.tyx.client.gui.TyxMenuScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Pause menu: a compact Tyx Mods shortcut, top-right, never in the way. */
@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void tyxShortcut(CallbackInfo ci) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Tyx Mods"),
                b -> client.setScreen(new TyxMenuScreen((GameMenuScreen) (Object) this)))
                .dimensions(this.width - 112, 8, 104, 20)
                .build());
    }
}
