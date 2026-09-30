/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_332
 */
package net.fastclient.hud.modules.impl.hud;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fastclient.hud.gui.FastClientUI;
import net.fastclient.hud.modules.Category;
import net.fastclient.hud.modules.Module;
import net.fastclient.hud.modules.settings.BooleanSetting;
import net.fastclient.hud.modules.settings.ColorSetting;
import net.minecraft.class_332;

@Environment(value=EnvType.CLIENT)
public class SaturationHUD
extends Module {
    private final BooleanSetting showBar = this.register(new BooleanSetting("show_bar", "Show progress bar", true));
    private final BooleanSetting background = this.register(new BooleanSetting("background", "Show background", true));
    private final ColorSetting saturationColor = this.register(new ColorSetting("saturation_color", "Saturation color", 255, 200, 50));

    public SaturationHUD() {
        super("Saturation", "Shows hidden hunger saturation", Category.HUD);
    }

    @Override
    public void onRender(class_332 graphics, float tickDelta) {
        if (!this.isInGame() || SaturationHUD.mc.field_1724 == null) {
            return;
        }
        int x = this.getHudX();
        int y = this.getHudY();
        float scale = this.getHudScale();
        graphics.method_51448().pushMatrix();
        graphics.method_51448().translate((float)x, (float)y);
        graphics.method_51448().scale(scale, scale);
        graphics.method_51448().translate((float)(-x), (float)(-y));
        float saturation = SaturationHUD.mc.field_1724.method_7344().method_7589();
        int food = SaturationHUD.mc.field_1724.method_7344().method_7586();
        int lineY = y;
        if (this.background.isEnabled()) {
            int height = 12;
            if (this.showBar.isEnabled()) {
                height += 6;
            }
            FastClientUI.hudPanel(graphics, x - 5, y - 4, 90, height + 6);
        }
        String satText = String.format("Sat: %.1f/%d", Float.valueOf(saturation), food);
        graphics.method_51433(SaturationHUD.mc.field_1772, satText, x, lineY, this.saturationColor.getRGB() | 0xFF000000, true);
        if (this.showBar.isEnabled()) {
            this.drawProgressBar(graphics, x, lineY += 10, 80, 4, saturation / 20.0f, this.saturationColor.getRGB() | 0xFF000000);
        }
        graphics.method_51448().popMatrix();
    }

    private void drawProgressBar(class_332 graphics, int x, int y, int width, int height, float progress, int color) {
        FastClientUI.roundedRect(graphics, x, y, width, height, 2, -1441128412);
        int filledWidth = (int)((float)width * Math.max(0.0f, Math.min(1.0f, progress)));
        if (filledWidth > 0) {
            FastClientUI.roundedRect(graphics, x, y, filledWidth, height, 2, color);
        }
        FastClientUI.outline(graphics, x, y, width, height, 1154997472);
    }

    @Override
    public int getHudWidth() {
        return 90;
    }

    @Override
    public int getHudHeight() {
        return this.showBar.isEnabled() ? 24 : 18;
    }
}

