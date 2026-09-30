/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_304
 *  net.minecraft.class_332
 *  org.lwjgl.glfw.GLFW
 */
package net.fastclient.hud.modules.impl.hud;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fastclient.hud.gui.FastClientUI;
import net.fastclient.hud.modules.Category;
import net.fastclient.hud.modules.Module;
import net.fastclient.hud.modules.settings.BooleanSetting;
import net.fastclient.hud.modules.settings.ColorSetting;
import net.fastclient.hud.modules.settings.NumberSetting;
import net.minecraft.class_304;
import net.minecraft.class_332;
import org.lwjgl.glfw.GLFW;

@Environment(value=EnvType.CLIENT)
public class Keystrokes
extends Module {
    private final NumberSetting keySize = this.register(new NumberSetting("key_size", "Size of each key", 22.0, 16.0, 40.0, 2.0));
    private final NumberSetting spacing = this.register(new NumberSetting("spacing", "Gap between keys", 2.0, 0.0, 10.0, 1.0));
    private final BooleanSetting showMouse = this.register(new BooleanSetting("show_mouse", "Show mouse buttons", true));
    private final BooleanSetting showSpace = this.register(new BooleanSetting("show_space", "Show spacebar", true));
    private final ColorSetting pressedColor = this.register(new ColorSetting("pressed_color", "Pressed key color", 100, 180, 255));
    private final ColorSetting normalColor = this.register(new ColorSetting("normal_color", "Normal key color", 50, 50, 50));

    public Keystrokes() {
        super("Keystrokes", "Shows pressed keys visually", Category.HUD);
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
        int size = this.keySize.getIntValue();
        int gap = this.spacing.getIntValue();
        this.drawKey(graphics, x + size + gap, y, size, "W", Keystrokes.mc.field_1690.field_1894);
        this.drawKey(graphics, x, y + size + gap, size, "A", Keystrokes.mc.field_1690.field_1913);
        this.drawKey(graphics, x + size + gap, y + size + gap, size, "S", Keystrokes.mc.field_1690.field_1881);
        this.drawKey(graphics, x + (size + gap) * 2, y + size + gap, size, "D", Keystrokes.mc.field_1690.field_1849);
        int currentY = y + (size + gap) * 2;
        if (((Boolean)this.showMouse.getValue()).booleanValue()) {
            int mouseWidth = (size * 3 + gap * 2 - gap) / 2;
            long windowHandle = mc.method_22683().method_4490();
            boolean lmb = GLFW.glfwGetMouseButton((long)windowHandle, (int)0) == 1;
            boolean rmb = GLFW.glfwGetMouseButton((long)windowHandle, (int)1) == 1;
            this.drawKeyRect(graphics, x, currentY, mouseWidth, size, "LMB", lmb);
            this.drawKeyRect(graphics, x + mouseWidth + gap, currentY, mouseWidth, size, "RMB", rmb);
            currentY += size + gap;
        }
        if (((Boolean)this.showSpace.getValue()).booleanValue()) {
            int spaceWidth = size * 3 + gap * 2;
            this.drawKeyRect(graphics, x, currentY, spaceWidth, size / 2, "\u2014", Keystrokes.mc.field_1690.field_1903.method_1434());
        }
        graphics.method_51448().popMatrix();
    }

    private void drawKey(class_332 graphics, int x, int y, int size, String text, class_304 key) {
        boolean pressed = key.method_1434();
        int bgColor = 0xFF000000 | (pressed ? this.pressedColor.getRGB() : this.normalColor.getRGB());
        FastClientUI.roundedRect(graphics, x, y, size, size, 4, bgColor);
        FastClientUI.outline(graphics, x, y, size, size, pressed ? this.brighten(bgColor) : 1154997472);
        int textColor = pressed ? -1 : -3025448;
        int textX = x + (size - Keystrokes.mc.field_1772.method_1727(text)) / 2;
        int textY = y + (size - 8) / 2;
        graphics.method_51433(Keystrokes.mc.field_1772, text, textX, textY, textColor, true);
    }

    private void drawKeyRect(class_332 graphics, int x, int y, int width, int height, String text, boolean pressed) {
        int bgColor = 0xFF000000 | (pressed ? this.pressedColor.getRGB() : this.normalColor.getRGB());
        FastClientUI.roundedRect(graphics, x, y, width, height, 4, bgColor);
        FastClientUI.outline(graphics, x, y, width, height, pressed ? this.brighten(bgColor) : 1154997472);
        int textColor = pressed ? -1 : -3025448;
        int textX = x + (width - Keystrokes.mc.field_1772.method_1727(text)) / 2;
        int textY = y + (height - 8) / 2;
        graphics.method_51433(Keystrokes.mc.field_1772, text, textX, textY, textColor, true);
    }

    @Override
    public int getHudWidth() {
        int size = this.keySize.getIntValue();
        int gap = this.spacing.getIntValue();
        return size * 3 + gap * 2;
    }

    @Override
    public int getHudHeight() {
        int size = this.keySize.getIntValue();
        int gap = this.spacing.getIntValue();
        int height = (size + gap) * 2;
        if (((Boolean)this.showMouse.getValue()).booleanValue()) {
            height += size + gap;
        }
        if (((Boolean)this.showSpace.getValue()).booleanValue()) {
            height += size / 2;
        }
        return height;
    }

    private int brighten(int color) {
        int red = Math.min(255, (color >> 16 & 0xFF) + 28);
        int green = Math.min(255, (color >> 8 & 0xFF) + 28);
        int blue = Math.min(255, (color & 0xFF) + 28);
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }
}

