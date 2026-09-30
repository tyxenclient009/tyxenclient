/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_332
 */
package net.fastclient.hud.modules.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fastclient.hud.gui.DisplaySpace;
import net.fastclient.hud.modules.Category;
import net.fastclient.hud.modules.Module;
import net.fastclient.hud.modules.settings.ColorSetting;
import net.fastclient.hud.modules.settings.ModeSetting;
import net.fastclient.hud.modules.settings.NumberSetting;
import net.minecraft.class_332;

@Environment(value=EnvType.CLIENT)
public class Crosshair
extends Module {
    private final ModeSetting style = this.register(new ModeSetting("style", "Crosshair style", "cross", new String[]{"cross", "dot", "circle", "square"}));
    private final NumberSetting size = this.register(new NumberSetting("size", "Crosshair size", 6.0, 1.0, 30.0, 1.0));
    private final NumberSetting thickness = this.register(new NumberSetting("thickness", "Line thickness", 1.0, 0.5, 10.0, 0.5));
    private final NumberSetting gap = this.register(new NumberSetting("gap", "Center gap", 2.0, 0.0, 15.0, 1.0));
    private final ColorSetting color = this.register(new ColorSetting("color", "Crosshair color", 255, 255, 255));

    public Crosshair() {
        super("Crosshair", "Custom crosshair replacement", Category.RENDER);
        this.gap.visibleWhen(() -> this.style.is("cross"));
        this.thickness.visibleWhen(() -> !this.style.is("circle"));
    }

    @Override
    public void onRender(class_332 graphics, float tickDelta) {
        if (!this.isInGame()) {
            return;
        }
        if (!Crosshair.mc.field_1690.method_31044().method_31034()) {
            return;
        }
        int centerX = DisplaySpace.width() / 2;
        int centerY = DisplaySpace.height() / 2;
        int crosshairColor = this.color.getRGB() | 0xFF000000;
        int sizeVal = this.size.getIntValue();
        float thicknessVal = ((Double)this.thickness.getValue()).floatValue();
        int gapVal = this.gap.getIntValue();
        switch ((String)this.style.getValue()) {
            case "cross": {
                int halfThick = Math.max(1, (int)Math.ceil(thicknessVal / 2.0f));
                int thickOffset = thicknessVal < 1.0f ? 0 : halfThick - 1;
                graphics.method_25294(centerX - thickOffset, centerY - sizeVal - gapVal, centerX + halfThick, centerY - gapVal, crosshairColor);
                graphics.method_25294(centerX - thickOffset, centerY + gapVal + 1, centerX + halfThick, centerY + sizeVal + gapVal + 1, crosshairColor);
                graphics.method_25294(centerX - sizeVal - gapVal, centerY - thickOffset, centerX - gapVal, centerY + halfThick, crosshairColor);
                graphics.method_25294(centerX + gapVal + 1, centerY - thickOffset, centerX + sizeVal + gapVal + 1, centerY + halfThick, crosshairColor);
                break;
            }
            case "dot": {
                int dotSize = Math.max(1, (int)thicknessVal);
                int dotHalf = dotSize / 2;
                graphics.method_25294(centerX - dotHalf, centerY - dotHalf, centerX + dotSize - dotHalf, centerY + dotSize - dotHalf, crosshairColor);
                break;
            }
            case "circle": {
                this.drawCircleOutline(graphics, centerX, centerY, sizeVal, crosshairColor);
                break;
            }
            case "square": {
                int squareThick = Math.max(1, (int)thicknessVal);
                graphics.method_25294(centerX - sizeVal, centerY - sizeVal, centerX + sizeVal, centerY - sizeVal + squareThick, crosshairColor);
                graphics.method_25294(centerX - sizeVal, centerY + sizeVal - squareThick, centerX + sizeVal, centerY + sizeVal, crosshairColor);
                graphics.method_25294(centerX - sizeVal, centerY - sizeVal, centerX - sizeVal + squareThick, centerY + sizeVal, crosshairColor);
                graphics.method_25294(centerX + sizeVal - squareThick, centerY - sizeVal, centerX + sizeVal, centerY + sizeVal, crosshairColor);
            }
        }
    }

    private void drawCircleOutline(class_332 graphics, int centerX, int centerY, int radius, int color) {
        int segments = Math.max(16, radius * 4);
        double angleStep = Math.PI * 2 / (double)segments;
        for (int i = 0; i < segments; ++i) {
            double angle = (double)i * angleStep;
            int x = centerX + (int)(Math.cos(angle) * (double)radius);
            int y = centerY + (int)(Math.sin(angle) * (double)radius);
            graphics.method_25294(x, y, x + 1, y + 1, color);
        }
    }
}

