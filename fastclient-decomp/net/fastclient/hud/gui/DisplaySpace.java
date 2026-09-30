/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1041
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package net.fastclient.hud.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1041;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;

@Environment(value=EnvType.CLIENT)
public final class DisplaySpace {
    private DisplaySpace() {
    }

    public static int width() {
        class_1041 window = class_310.method_1551().method_22683();
        return Math.max(1, Math.round((float)window.method_4489() / DisplaySpace.renderScale()));
    }

    public static int height() {
        class_1041 window = class_310.method_1551().method_22683();
        return Math.max(1, Math.round((float)window.method_4506() / DisplaySpace.renderScale()));
    }

    public static int mouseX(double guiMouseX) {
        return (int)Math.round(guiMouseX * (double)DisplaySpace.guiScale() / (double)DisplaySpace.renderScale());
    }

    public static int mouseY(double guiMouseY) {
        return (int)Math.round(guiMouseY * (double)DisplaySpace.guiScale() / (double)DisplaySpace.renderScale());
    }

    public static int mouseDelta(double guiDelta) {
        return (int)Math.round(guiDelta * (double)DisplaySpace.guiScale() / (double)DisplaySpace.renderScale());
    }

    public static void push(class_332 graphics) {
        float invScale = DisplaySpace.renderScale() / DisplaySpace.guiScale();
        graphics.method_51448().pushMatrix();
        graphics.method_51448().scale(invScale, invScale);
    }

    public static void pop(class_332 graphics) {
        graphics.method_51448().popMatrix();
    }

    public static float nativeTextCompensationScale() {
        return DisplaySpace.guiScale() / DisplaySpace.renderScale();
    }

    public static void enableScissor(class_332 graphics, int x1, int y1, int x2, int y2) {
        graphics.method_44379(x1, y1, x2, y2);
    }

    public static void disableScissor(class_332 graphics) {
        graphics.method_44380();
    }

    public static class_2960 texture(class_2960 highDensityTexture) {
        if (DisplaySpace.renderScale() > 1.0f) {
            return highDensityTexture;
        }
        String path = highDensityTexture.method_12832();
        int extension = path.lastIndexOf(46);
        String standardDensityPath = extension >= 0 ? path.substring(0, extension) + "_1x" + path.substring(extension) : path + "_1x";
        return class_2960.method_60655((String)highDensityTexture.method_12836(), (String)standardDensityPath);
    }

    private static float guiScale() {
        class_1041 window = class_310.method_1551().method_22683();
        return Math.max(1.0f, (float)window.method_4495());
    }

    private static float renderScale() {
        class_1041 window = class_310.method_1551().method_22683();
        float scaleX = (float)Math.max(1, window.method_4489()) / 1920.0f;
        float scaleY = (float)Math.max(1, window.method_4506()) / 1080.0f;
        float continuousScale = Math.min(scaleX, scaleY);
        return Math.max(1.0f, (float)Math.round(continuousScale));
    }
}

