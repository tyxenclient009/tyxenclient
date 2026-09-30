/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_3532
 *  net.minecraft.class_4185
 *  net.minecraft.class_4185$class_4241
 *  net.minecraft.class_4185$class_7841
 *  net.minecraft.class_5348
 */
package ru.vidtu.ias.screen;

import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4185;
import net.minecraft.class_5348;

final class PopupButton
extends class_4185 {
    private float red = 1.0f;
    private float green = 1.0f;
    private float blue = 1.0f;
    private float currentRed = 1.0f;
    private float currentGreen = 1.0f;
    private float currentBlue = 1.0f;
    private float multiplier = 1.0f;

    PopupButton(int x, int y, int width, int height, class_2561 text, class_4185.class_4241 press, class_4185.class_7841 narration) {
        super(x, y, width, height, text, press, narration);
    }

    void color(float red, float green, float blue, boolean instant) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        if (!instant) {
            return;
        }
        this.currentRed = red;
        this.currentGreen = green;
        this.currentBlue = blue;
    }

    protected void method_75752(class_332 graphics, int mouseX, int mouseY, float delta) {
        class_310 minecraft = class_310.method_1551();
        class_327 font = minecraft.field_1772;
        int x = this.method_46426();
        int y = this.method_46427();
        int width = this.method_25368();
        int height = this.method_25364();
        class_2561 message = this.method_25369();
        this.multiplier = this.method_25367() && this.method_37303() ? class_3532.method_15363((float)(this.multiplier - delta * 0.25f), (float)0.75f, (float)1.0f) : class_3532.method_15363((float)(this.multiplier + delta * 0.25f), (float)0.75f, (float)1.0f);
        this.currentRed += (this.red - this.currentRed) * delta;
        this.currentGreen += (this.green - this.currentGreen) * delta;
        this.currentBlue += (this.blue - this.currentBlue) * delta;
        int r = (int)(this.multiplier * 255.0f * this.currentRed);
        int g = (int)(this.multiplier * 255.0f * this.currentGreen);
        int b = (int)(this.multiplier * 255.0f * this.currentBlue);
        int color = 0xFF000000 | r << 16 | g << 8 | b;
        graphics.method_25294(x, y + 1, x + width, y + height - 1, color);
        graphics.method_25294(x + 1, y, x + width - 1, y + 1, color);
        graphics.method_25294(x + 1, y + height - 1, x + width - 1, y + height, color);
        graphics.method_51439(font, message, x + (width - font.method_27525((class_5348)message)) / 2, y + height / 2 - 4, -16777216, false);
    }

    public String toString() {
        return "PopupButton{red=" + this.red + ", green=" + this.green + ", blue=" + this.blue + ", currentRed=" + this.currentRed + ", currentGreen=" + this.currentGreen + ", currentBlue=" + this.currentBlue + ", multiplier=" + this.multiplier + "}";
    }
}

