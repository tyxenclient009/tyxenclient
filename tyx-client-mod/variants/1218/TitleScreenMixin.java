package gg.tyx.client.mixin;

import gg.tyx.client.gui.TyxMenuScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * VARIANT 1218 — Lunar-style main menu on the layer-function GUI pipeline
 * (MC 1.21.2–1.21.5): looping video background (120 frames bundled as
 * menu assets, 12 fps), dark readability overlay, Tyx branding, clean
 * button stack (no Realms row), version footer. Same look as canonical,
 * only the texture draw call differs (RenderLayer function instead of a
 * render pipeline).
 */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    /** Bundled menu video: 960x540 JPGs, 12 fps, 10 s loop. */
    private static final int MENU_FRAMES = 120;
    private static final int MENU_FPS = 12;
    private static final int FRAME_W = 960;
    private static final int FRAME_H = 540;

    private static Identifier tyxMenuFrame() {
        int idx = (int) ((System.currentTimeMillis() * MENU_FPS / 1000) % MENU_FRAMES);
        return Identifier.of("tyxclient",
                String.format("textures/gui/menu/bg_%03d.jpg", idx));
    }

    private void tyxButton(String label, ButtonWidget.PressAction action, int y) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal(label), action)
                .dimensions(this.width / 2 - 100, y, 200, 20)
                .build());
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void tyxMenu(CallbackInfo ci) {
        this.clearChildren();
        int y = this.height / 4 + 56;
        tyxButton("Singleplayer", b -> client.setScreen(new SelectWorldScreen(this)), y); y += 24;
        tyxButton("Multiplayer", b -> client.setScreen(new MultiplayerScreen(this)), y); y += 24;
        tyxButton("Tyx Mods", b -> client.setScreen(new TyxMenuScreen(this)), y); y += 24;
        tyxButton("Options", b -> client.setScreen(new OptionsScreen(this, client.options)), y); y += 24;
        tyxButton("Quit Game", b -> client.scheduleStop(), y);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void tyxRender(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ci.cancel();
        // Looping video frame, stretched fullscreen…
        ctx.drawTexture(RenderLayer::getGuiTextured, tyxMenuFrame(),
                0, 0, 0.0f, 0.0f, this.width, this.height, FRAME_W, FRAME_H);
        // …under a dark gradient so buttons and branding stay readable.
        ctx.fillGradient(0, 0, this.width, this.height, 0xCC0B0F0D, 0x99141B17);
        ctx.fill(0, 0, this.width, 2, 0xFF1EA86A);
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§a§lTYX §f§lCLIENT"), this.width / 2, this.height / 4 + 8, 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Modded Minecraft, minus the bloat"),
                this.width / 2, this.height / 4 + 30, 0x8B978F);
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal("Tyx Client 1.2.0 · 1.21.1 Fabric"), 6, this.height - 14, 0x5B6660);
        String name = client.getSession().getUsername();
        int w = this.textRenderer.getWidth(name);
        ctx.drawTextWithShadow(this.textRenderer, Text.literal(name),
                this.width - w - 6, this.height - 14, 0x8B978F);
    }
}
