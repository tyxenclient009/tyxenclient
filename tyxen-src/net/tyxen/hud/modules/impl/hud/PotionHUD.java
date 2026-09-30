/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10799
 *  net.minecraft.class_1291
 *  net.minecraft.class_1293
 *  net.minecraft.class_2960
 *  net.minecraft.class_329
 *  net.minecraft.class_332
 *  net.minecraft.class_6880
 */
package net.tyxen.hud.modules.impl.hud;

import java.util.Collection;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.gui.TyxenUI;
import net.tyxen.hud.modules.Category;
import net.tyxen.hud.modules.Module;
import net.tyxen.hud.modules.settings.BooleanSetting;
import net.tyxen.hud.modules.settings.NumberSetting;
import net.minecraft.class_10799;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_2960;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_6880;

@Environment(value=EnvType.CLIENT)
public class PotionHUD
extends Module {
    private final BooleanSetting showIcon = this.register(new BooleanSetting("show_icon", "Show effect icon", true));
    private final BooleanSetting showDuration = this.register(new BooleanSetting("show_duration", "Show remaining duration", true));
    private final BooleanSetting showAmplifier = this.register(new BooleanSetting("show_amplifier", "Show effect level", true));
    private final BooleanSetting showName = this.register(new BooleanSetting("show_name", "Show effect name", true));
    private final BooleanSetting background = this.register(new BooleanSetting("background", "Show background", true));
    private final NumberSetting bgOpacity = this.register(new NumberSetting("opacity", "Background opacity", 80.0, 0.0, 255.0, 5.0));
    private static final int ICON_SIZE = 18;

    public PotionHUD() {
        super("PotionHUD", "Shows active potion effects", Category.HUD);
        this.bgOpacity.visibleWhen(this.background::isEnabled);
    }

    @Override
    public void onRender(class_332 graphics, float tickDelta) {
        if (!this.isInGame()) {
            return;
        }
        int x = this.getHudX();
        int y = this.getHudY();
        float scale = this.getHudScale();
        graphics.method_51448().pushMatrix();
        graphics.method_51448().translate((float)x, (float)y);
        graphics.method_51448().scale(scale, scale);
        graphics.method_51448().translate((float)(-x), (float)(-y));
        int offsetY = 0;
        int iconOffset = (Boolean)this.showIcon.getValue() != false ? 20 : 0;
        int lineHeight = (Boolean)this.showIcon.getValue() != false ? 20 : 12;
        Collection<class_1293> effects = PotionHUD.mc.field_1724.method_6026();
        if (effects.isEmpty()) {
            graphics.method_51448().popMatrix();
            return;
        }
        int maxWidth = 0;
        for (class_1293 effect : effects) {
            String text = this.buildEffectText(effect);
            int width = iconOffset + PotionHUD.mc.field_1772.method_1727(text);
            if (width <= maxWidth) continue;
            maxWidth = width;
        }
        if (((Boolean)this.background.getValue()).booleanValue()) {
            TyxenUI.hudPanel(graphics, x - 5, y - 4, maxWidth + 10, effects.size() * lineHeight + 6, this.bgOpacity.getIntValue());
        }
        for (class_1293 effect : effects) {
            int n;
            class_6880 effectType = effect.method_5579();
            class_1291 effectValue = (class_1291)effectType.comp_349();
            if (((Boolean)this.showIcon.getValue()).booleanValue()) {
                class_2960 spriteId = class_329.method_71644((class_6880)effectType);
                graphics.method_52706(class_10799.field_56883, spriteId, x, y + offsetY, 18, 18);
            }
            String text = this.buildEffectText(effect);
            int color = effectValue.method_5556();
            if (color == 0 || color == -1) {
                color = 0xFFFFFF;
            }
            color |= 0xFF000000;
            if (((Boolean)this.showIcon.getValue()).booleanValue()) {
                Objects.requireNonNull(PotionHUD.mc.field_1772);
                n = (18 - 9) / 2;
            } else {
                n = 0;
            }
            int textY = y + offsetY + n;
            graphics.method_51433(PotionHUD.mc.field_1772, text, x + iconOffset, textY, color, true);
            offsetY += lineHeight;
        }
        graphics.method_51448().popMatrix();
    }

    private String buildEffectText(class_1293 effect) {
        StringBuilder text = new StringBuilder();
        if (((Boolean)this.showName.getValue()).booleanValue()) {
            text.append(((class_1291)effect.method_5579().comp_349()).method_5560().getString());
        }
        if (((Boolean)this.showAmplifier.getValue()).booleanValue() && effect.method_5578() > 0) {
            if (text.length() > 0) {
                text.append(" ");
            }
            text.append(this.toRoman(effect.method_5578() + 1));
        }
        if (((Boolean)this.showDuration.getValue()).booleanValue()) {
            int duration = effect.method_5584() / 20;
            int minutes = duration / 60;
            int seconds = duration % 60;
            if (text.length() > 0) {
                text.append(" ");
            }
            text.append("\u00a77").append(String.format("%d:%02d", minutes, seconds));
        }
        return text.toString();
    }

    private String toRoman(int number) {
        String[] romanNumerals = new String[]{"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        if (number >= 1 && number <= 10) {
            return romanNumerals[number - 1];
        }
        return String.valueOf(number);
    }

    @Override
    public int getHudWidth() {
        if (PotionHUD.mc.field_1724 == null) {
            return 100;
        }
        Collection<class_1293> effects = PotionHUD.mc.field_1724.method_6026();
        if (effects.isEmpty()) {
            return 0;
        }
        int iconOffset = (Boolean)this.showIcon.getValue() != false ? 20 : 0;
        int maxWidth = 0;
        for (class_1293 effect : effects) {
            String text = this.buildEffectText(effect);
            int width = iconOffset + PotionHUD.mc.field_1772.method_1727(text.replaceAll("\u00a7.", ""));
            if (width <= maxWidth) continue;
            maxWidth = width;
        }
        return maxWidth + 10;
    }

    @Override
    public int getHudHeight() {
        if (PotionHUD.mc.field_1724 == null) {
            return 20;
        }
        Collection<class_1293> effects = PotionHUD.mc.field_1724.method_6026();
        if (effects.isEmpty()) {
            return 0;
        }
        int lineHeight = (Boolean)this.showIcon.getValue() != false ? 20 : 12;
        return effects.size() * lineHeight + 6;
    }
}

