package com.swiftclient.util;

import net.minecraft.class_332;

/**
 * TYX addition — adaptive UI scale shared by every Swift screen.
 * Screens are authored around ~640x400 reference units; on small windows
 * or huge GUI scales the fixed layouts would fill or overflow the screen.
 * This shrinks each screen's content toward its center (never enlarges,
 * never below half) and maps mouse coordinates back into layout space.
 */
public final class UiScale {
    private UiScale() {
    }

    public static float scale(int w, int h) {
        float s = Math.min(w / 640.0f, h / 400.0f);
        if (s > 1.0f) {
            return 1.0f;
        }
        if (s < 0.5f) {
            return 0.5f;
        }
        return s;
    }

    public static void begin(class_332 c, int w, int h) {
        float s = UiScale.scale(w, h);
        float cx = w / 2.0f;
        float cy = h / 2.0f;
        org.joml.Matrix3x2fStack m = c.method_51448();
        m.pushMatrix();
        m.translate(cx, cy);
        m.scale(s, s);
        m.translate(-cx, -cy);
    }

    public static void end(class_332 c) {
        c.method_51448().popMatrix();
    }

    public static int unX(int mx, int w, int h) {
        float s = UiScale.scale(w, h);
        float cx = w / 2.0f;
        return Math.round((mx - cx) / s + cx);
    }

    public static int unY(int my, int w, int h) {
        float s = UiScale.scale(w, h);
        float cy = h / 2.0f;
        return Math.round((my - cy) / s + cy);
    }
}
