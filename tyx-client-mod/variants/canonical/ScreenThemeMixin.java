package gg.tyx.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lunar-style list theming, centralized on Screen.
 *
 * Why here and not per-screen mixins: on modern versions (1.21.11+)
 * MultiplayerScreen / SelectWorldScreen no longer override {@code render}
 * — Mixin cannot target an inherited-but-not-declared method, so the old
 * per-screen mixins crashed at apply time. {@code Screen} itself always
 * declares it, on every version, so one guarded inject covers every list
 * screen with zero per-version variants.
 *
 * (A mixin may not extend its own target, hence the shadows instead of
 * inherited width/textRenderer access.)
 *
 * TitleScreen (own video mixin) and GameMenuScreen (own button mixin, no
 * glass) are deliberately skipped — everything else list-like gets the
 * dark glass, green hairline and footer. Widgets draw on top untouched.
 */
@Mixin(Screen.class)
public abstract class ScreenThemeMixin {

    @Shadow
    public int width;

    @Shadow
    public int height;

    @Shadow
    protected TextRenderer textRenderer;

    @Inject(method = "render", at = @At("HEAD"))
    private void tyxTheme(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Object self = this;
        if (!(self instanceof MultiplayerScreen) && !(self instanceof SelectWorldScreen)) return;
        ctx.fillGradient(0, 0, this.width, this.height, 0xE00B0F0D, 0xE0141815);
        ctx.fill(0, 0, this.width, 2, 0xFF1EA86A);
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("TYX CLIENT"),
                6, this.height - 14, 0x5B6660);
        String name = MinecraftClient.getInstance().getSession().getUsername();
        int w = this.textRenderer.getWidth(name);
        ctx.drawTextWithShadow(this.textRenderer, Text.literal(name),
                this.width - w - 6, this.height - 14, 0x8B978F);
    }
}
