/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1304
 *  net.minecraft.class_1799
 *  net.minecraft.class_332
 *  net.minecraft.class_3414
 *  net.minecraft.class_3417
 */
package net.tyxen.hud.modules.impl.utility;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.modules.Category;
import net.tyxen.hud.modules.Module;
import net.tyxen.hud.modules.settings.BooleanSetting;
import net.tyxen.hud.modules.settings.ColorSetting;
import net.tyxen.hud.modules.settings.ModeSetting;
import net.tyxen.hud.modules.settings.NumberSetting;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_332;
import net.minecraft.class_3414;
import net.minecraft.class_3417;

@Environment(value=EnvType.CLIENT)
public class ToolWarning
extends Module {
    private final ModeSetting thresholdMode = this.register(new ModeSetting("threshold_mode", "How to calculate low durability", "percentage", new String[]{"percentage", "absolute"}));
    private final NumberSetting percentThreshold = this.register(new NumberSetting("percent_threshold", "Durability percentage threshold", 10.0, 1.0, 50.0, 1.0));
    private final NumberSetting absoluteThreshold = this.register(new NumberSetting("absolute_threshold", "Absolute durability threshold", 10.0, 1.0, 100.0, 1.0));
    private final BooleanSetting showMainHand = this.register(new BooleanSetting("show_main_hand", "Warn for main hand item", true));
    private final BooleanSetting showOffHand = this.register(new BooleanSetting("show_off_hand", "Warn for off hand item", true));
    private final BooleanSetting showHelmet = this.register(new BooleanSetting("show_helmet", "Warn for helmet", true));
    private final BooleanSetting showChestplate = this.register(new BooleanSetting("show_chestplate", "Warn for chestplate", true));
    private final BooleanSetting showLeggings = this.register(new BooleanSetting("show_leggings", "Warn for leggings", true));
    private final BooleanSetting showBoots = this.register(new BooleanSetting("show_boots", "Warn for boots", true));
    private final BooleanSetting showIcon = this.register(new BooleanSetting("show_icon", "Show warning icon", true));
    private final BooleanSetting showItemName = this.register(new BooleanSetting("show_item_name", "Show item name", true));
    private final BooleanSetting showDurability = this.register(new BooleanSetting("show_durability", "Show remaining durability", true));
    private final BooleanSetting background = this.register(new BooleanSetting("background", "Show background", true));
    private final ColorSetting warningColor = this.register(new ColorSetting("warning_color", "Warning text color", 255, 85, 85));
    private final ColorSetting criticalColor = this.register(new ColorSetting("critical_color", "Critical text color (< 5%)", 255, 0, 0));
    private final BooleanSetting soundAlert = this.register(new BooleanSetting("sound_alert", "Play sound alert", true));
    private final NumberSetting soundCooldown = this.register(new NumberSetting("sound_cooldown", "Seconds between sounds", 3.0, 1.0, 10.0, 0.5));
    private long lastSoundTime = 0L;
    private List<WarningEntry> cachedWarnings = new ArrayList<WarningEntry>();

    public ToolWarning() {
        super("ToolWarning", "Warns when tools/armor are low durability", Category.HUD);
        this.percentThreshold.visibleWhen(() -> this.thresholdMode.is("percentage"));
        this.absoluteThreshold.visibleWhen(() -> this.thresholdMode.is("absolute"));
    }

    @Override
    public void onRender(class_332 graphics, float tickDelta) {
        long now;
        if (!this.isInGame() || ToolWarning.mc.field_1724 == null) {
            return;
        }
        this.updateWarnings();
        if (this.cachedWarnings.isEmpty()) {
            return;
        }
        int x = this.getHudX();
        int y = this.getHudY();
        float scale = this.getHudScale();
        graphics.method_51448().pushMatrix();
        graphics.method_51448().translate((float)x, (float)y);
        graphics.method_51448().scale(scale, scale);
        graphics.method_51448().translate((float)(-x), (float)(-y));
        int lineY = y;
        int maxWidth = this.getContentWidth();
        if (this.background.isEnabled()) {
            int height = this.cachedWarnings.size() * 12 + 4;
            graphics.method_25294(x - 2, y - 2, x + maxWidth + 4, y + height, -1442840576);
        }
        for (WarningEntry entry : this.cachedWarnings) {
            int color = entry.isCritical ? this.criticalColor.getRGB() : this.warningColor.getRGB();
            color |= 0xFF000000;
            StringBuilder text = new StringBuilder();
            if (this.showIcon.isEnabled()) {
                text.append("\u26a0 ");
            }
            if (this.showItemName.isEnabled()) {
                text.append(entry.name);
            }
            if (this.showDurability.isEnabled()) {
                if (this.showItemName.isEnabled()) {
                    text.append(": ");
                }
                text.append(entry.durability);
                if (this.thresholdMode.is("percentage")) {
                    text.append(" (").append(String.format("%.0f%%", Float.valueOf(entry.percent))).append(")");
                }
            }
            graphics.method_51433(ToolWarning.mc.field_1772, text.toString(), x, lineY, color, true);
            lineY += 12;
        }
        graphics.method_51448().popMatrix();
        if (this.soundAlert.isEnabled() && !this.cachedWarnings.isEmpty() && (double)((now = System.currentTimeMillis()) - this.lastSoundTime) > (Double)this.soundCooldown.getValue() * 1000.0) {
            ToolWarning.mc.field_1724.method_5783((class_3414)class_3417.field_14622.comp_349(), 0.5f, 0.5f);
            this.lastSoundTime = now;
        }
    }

    private void updateWarnings() {
        this.cachedWarnings.clear();
        if (ToolWarning.mc.field_1724 == null) {
            return;
        }
        if (this.showMainHand.isEnabled()) {
            this.checkItem(ToolWarning.mc.field_1724.method_6047());
        }
        if (this.showOffHand.isEnabled()) {
            this.checkItem(ToolWarning.mc.field_1724.method_6079());
        }
        if (this.showHelmet.isEnabled()) {
            this.checkItem(ToolWarning.mc.field_1724.method_6118(class_1304.field_6169));
        }
        if (this.showChestplate.isEnabled()) {
            this.checkItem(ToolWarning.mc.field_1724.method_6118(class_1304.field_6174));
        }
        if (this.showLeggings.isEnabled()) {
            this.checkItem(ToolWarning.mc.field_1724.method_6118(class_1304.field_6172));
        }
        if (this.showBoots.isEnabled()) {
            this.checkItem(ToolWarning.mc.field_1724.method_6118(class_1304.field_6166));
        }
    }

    private void checkItem(class_1799 stack) {
        boolean isLow;
        if (stack.method_7960() || !stack.method_7963()) {
            return;
        }
        int remaining = stack.method_7936() - stack.method_7919();
        float percent = (float)remaining / (float)stack.method_7936() * 100.0f;
        if (this.thresholdMode.is("percentage")) {
            isLow = (double)percent <= (Double)this.percentThreshold.getValue();
        } else {
            boolean bl = isLow = remaining <= this.absoluteThreshold.getIntValue();
        }
        if (isLow) {
            boolean isCritical = percent < 5.0f;
            this.cachedWarnings.add(new WarningEntry(stack.method_7964().getString(), remaining, percent, isCritical));
        }
    }

    private int getContentWidth() {
        int maxWidth = 0;
        for (WarningEntry entry : this.cachedWarnings) {
            StringBuilder text = new StringBuilder();
            if (this.showIcon.isEnabled()) {
                text.append("\u26a0 ");
            }
            if (this.showItemName.isEnabled()) {
                text.append(entry.name);
            }
            if (this.showDurability.isEnabled()) {
                if (this.showItemName.isEnabled()) {
                    text.append(": ");
                }
                text.append(entry.durability);
                if (this.thresholdMode.is("percentage")) {
                    text.append(" (").append(String.format("%.0f%%", Float.valueOf(entry.percent))).append(")");
                }
            }
            maxWidth = Math.max(maxWidth, ToolWarning.mc.field_1772.method_1727(text.toString()));
        }
        return maxWidth;
    }

    @Override
    public int getHudWidth() {
        int width = this.getContentWidth();
        return width > 0 ? width + 6 : 0;
    }

    @Override
    public int getHudHeight() {
        int count = this.cachedWarnings.size();
        return count > 0 ? count * 12 + 6 : 0;
    }

    @Environment(value=EnvType.CLIENT)
    private static class WarningEntry {
        final String name;
        final int durability;
        final float percent;
        final boolean isCritical;

        WarningEntry(String name, int durability, float percent, boolean isCritical) {
            this.name = name;
            this.durability = durability;
            this.percent = percent;
            this.isCritical = isCritical;
        }
    }
}

