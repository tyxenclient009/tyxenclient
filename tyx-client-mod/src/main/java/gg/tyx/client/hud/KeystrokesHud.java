package gg.tyx.client.hud;

import gg.tyx.client.TyxClient;
import java.util.ArrayDeque;
import java.util.Deque;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/**
 * Lunar-style keystrokes: chunky bordered boxes, big high-contrast key
 * names (always with shadow so they read on any background), mouse
 * buttons with live CPS underneath. Bottom-right, out of the way.
 */
public final class KeystrokesHud implements HudRenderCallback {
    private static final Deque<Long> LMB = new ArrayDeque<>();
    private static final Deque<Long> RMB = new ArrayDeque<>();
    private static final long WINDOW_MS = 1000;

    private static final int BOX = 0xCC0B0F0D;
    private static final int BOX_DOWN = 0xCC1EA86A;
    private static final int EDGE = 0xFF2A3530;
    private static final int EDGE_DOWN = 0xFF4FD68A;
    private static final int NAME = 0xFFFFFF;
    private static final int CPS = 0x4FD68A;

    public static void click(boolean left) {
        long now = System.currentTimeMillis();
        (left ? LMB : RMB).addLast(now);
    }

    private static int cps(Deque<Long> q) {
        long cutoff = System.currentTimeMillis() - WINDOW_MS;
        while (!q.isEmpty() && q.peekFirst() < cutoff) q.pollFirst();
        return q.size();
    }

    @Override
    public void onHudRender(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden || !TyxClient.CONFIG.keystrokes) return;
        var o = client.options;
        int s = 26, gap = 3;
        int x = client.getWindow().getScaledWidth() - 4;
        int y = client.getWindow().getScaledHeight() - 4;
        int unit = s + gap;

        key(ctx, x - s, y - unit * 3, s, s, o.forwardKey.isPressed(), "W");
        key(ctx, x - s * 3 - gap * 2, y - unit * 2, s, s, o.leftKey.isPressed(), "A");
        key(ctx, x - s * 2 - gap, y - unit * 2, s, s, o.backKey.isPressed(), "S");
        key(ctx, x - s, y - unit * 2, s, s, o.rightKey.isPressed(), "D");
        key(ctx, x - s * 3 - gap * 2, y - unit - 13, s * 3 + gap * 2, 13, o.jumpKey.isPressed(), "SPACE");

        int mw = (s * 3 + gap) / 2;
        mouse(ctx, x - s * 3 - gap * 2, y - s, mw, s, o.attackKey.isPressed(), "LMB",
                TyxClient.CONFIG.keyShowCps ? cps(LMB) : -1);
        mouse(ctx, x - mw, y - s, mw, s, o.useKey.isPressed(), "RMB",
                TyxClient.CONFIG.keyShowCps ? cps(RMB) : -1);
    }

    /** Single key box with border + centered shadowed name. */
    private static void key(DrawContext ctx, int x, int y, int w, int h, boolean down, String label) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        if (TyxClient.CONFIG.keyBackground) {
            ctx.fill(x, y, x + w, y + h, down ? BOX_DOWN : BOX);
            int edge = down ? EDGE_DOWN : EDGE;
            ctx.fill(x, y, x + w, y + 1, edge);
            ctx.fill(x, y + h - 1, x + w, y + h, edge);
            ctx.fill(x, y, x + 1, y + h, edge);
            ctx.fill(x + w - 1, y, x + w, y + h, edge);
        }
        int tw = tr.getWidth(label);
        ctx.drawText(tr, label, x + (w - tw) / 2, y + (h - 9) / 2, NAME, true);
    }

    /** Mouse button: name on top, live CPS below in accent. */
    private static void mouse(DrawContext ctx, int x, int y, int w, int h,
            boolean down, String label, int cps) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        if (TyxClient.CONFIG.keyBackground) {
            ctx.fill(x, y, x + w, y + h, down ? BOX_DOWN : BOX);
            int edge = down ? EDGE_DOWN : EDGE;
            ctx.fill(x, y, x + w, y + 1, edge);
            ctx.fill(x, y + h - 1, x + w, y + h, edge);
            ctx.fill(x, y, x + 1, y + h, edge);
            ctx.fill(x + w - 1, y, x + w, y + h, edge);
        }
        int tw = tr.getWidth(label);
        ctx.drawText(tr, label, x + (w - tw) / 2, y + 3, NAME, true);
        if (cps < 0) return;
        String c = String.valueOf(cps);
        int cw = tr.getWidth(c);
        ctx.drawText(tr, c, x + (w - cw) / 2, y + 13, CPS, true);
    }
}
