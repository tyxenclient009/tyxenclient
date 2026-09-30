package gg.tyx.client;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;

/**
 * Says "gg" a moment after a match ends. Keyword-based so it works on any
 * server whose end message contains one of these (Hypixel, etc.).
 * Delayed 30 ticks to look human, never spams (10s cooldown).
 */
public final class AutoGg {
    private static final String[] TRIGGERS = {
        "winner", " wins", "1st place", "victory", "game over", "match over",
    };
    private static int delay = -1;
    private static long lastSent;

    public static void init() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay || !TyxClient.CONFIG.autoGG) return;
            String s = message.getString().toLowerCase();
            for (String t : TRIGGERS) {
                if (s.contains(t)) {
                    delay = Math.max(10, Math.min(100, TyxClient.CONFIG.ggDelay));
                    break;
                }
            }
        });
    }

    public static void tick(MinecraftClient client) {
        if (delay < 0 || !TyxClient.CONFIG.autoGG) return;
        if (--delay == 0 && client.player != null
                && System.currentTimeMillis() - lastSent > 10_000) {
            try {
                client.player.networkHandler.sendChatMessage("gg");
                lastSent = System.currentTimeMillis();
            } catch (Exception ignored) {
                // Chat closed / disconnected — never crash the tick.
            }
        }
    }
}
