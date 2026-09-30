package gg.tyx.client.hud;

import gg.tyx.client.TyxClient;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

/**
 * Armor status (items + durability) top-left under the FPS line, and active
 * potion effects with mm:ss timers beneath. Text-based timers dodge sprite
 * API churn across versions — always renders, never crashes.
 */
public final class GearHud implements HudRenderCallback {
    @Override
    public void onHudRender(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;
        int y = 4;
        if (TyxClient.CONFIG.fpsHud) y += 14;
        if (TyxClient.CONFIG.coordsHud) y += 14;

        if (TyxClient.CONFIG.armorHud) {
            EquipmentSlot[] slots = {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET,
            };
            boolean horiz = TyxClient.CONFIG.armorMode.equals("horizontal");
            boolean value = TyxClient.CONFIG.armorValue;
            boolean bg = TyxClient.CONFIG.armorBackground;
            int gap = TyxClient.CONFIG.armorGap;
            int ax = 4, ay = y;
            for (EquipmentSlot slot : slots) {
                ItemStack stack = client.player.getEquippedStack(slot);
                if (stack.isEmpty()) continue;
                int left = stack.isDamageable()
                        ? Math.max(0, stack.getMaxDamage() - stack.getDamage())
                        : -1;
                int pct = stack.isDamageable()
                        ? Math.max(0, left * 100 / stack.getMaxDamage())
                        : -1;
                String t = !stack.isDamageable() ? "" : (value ? String.valueOf(left) : pct + "%");
                int col = pct < 0 || pct > 30 ? 0xE8EEEA : 0xFF5555;
                if (horiz) {
                    if (bg) ctx.fill(ax - 2, ay - 2, ax + 34, ay + 30, 0x80000000);
                    ctx.drawItem(stack, ax, ay);
                    if (!t.isEmpty()) ctx.drawText(client.textRenderer, t, ax + 2, ay + 18, col, true);
                    ax += 36 + gap;
                } else {
                    if (bg) ctx.fill(ax - 2, ay - 2, ax + 62, ay + 20, 0x80000000);
                    ctx.drawItem(stack, ax, ay);
                    if (!t.isEmpty()) ctx.drawText(client.textRenderer, t, ax + 20, ay + 5, col, true);
                    ay += 22 + gap;
                }
            }
            y = (horiz ? y + 32 : ay) + 2;
        }

        if (TyxClient.CONFIG.potionHud) {
            for (StatusEffectInstance e : client.player.getStatusEffects()) {
                int ticks = e.getDuration();
                String line = e.getEffectType().value().getName().getString()
                        + " " + (e.getAmplifier() + 1)
                        + "  " + String.format("%d:%02d", ticks / 1200, (ticks / 20) % 60);
                int w = client.textRenderer.getWidth(line);
                ctx.fill(2, y - 2, 6 + w, y + 10, 0x80000000);
                ctx.drawText(client.textRenderer, line, 4, y, 0x7DD3FC, true);
                y += 14;
            }
        }
    }
}
