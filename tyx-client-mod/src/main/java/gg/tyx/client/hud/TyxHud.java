package gg.tyx.client.hud;

import gg.tyx.client.TyxClient;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/** Minimal Lunar-style HUD: FPS + coordinates, toggleable from the menu. */
public final class TyxHud implements HudRenderCallback {
    @Override
    public void onHudRender(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;
        int y = 4;
        if (TyxClient.CONFIG.fpsHud) {
            String fps = client.getCurrentFps() + " FPS";
            int col = switch (TyxClient.CONFIG.fpsColor) {
                case "white" -> 0xFFFFFF;
                case "amber" -> 0xFFBF00;
                default -> 0x4FD68A;
            };
            int w = client.textRenderer.getWidth(fps);
            ctx.fill(2, y - 2, 6 + w, y + 10, 0x80000000);
            ctx.drawText(client.textRenderer, fps, 4, y, col, true);
            y += 14;
        }
        if (TyxClient.CONFIG.coordsHud && client.player != null) {
            String pos = String.format("%d / %d / %d",
                    client.player.getBlockX(), client.player.getBlockY(), client.player.getBlockZ());
            int w = client.textRenderer.getWidth(pos);
            ctx.fill(2, y - 2, 6 + w, y + 10, 0x80000000);
            ctx.drawText(client.textRenderer, pos, 4, y, 0xE8EEEA, true);
        }
    }
}
