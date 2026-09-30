/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  org.joml.Matrix3x2fStack
 */
package com.swiftclient.util;

import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;

public final class UiScale {
    private UiScale() {
    }

    public static float scale(int n, int n2) {
        float f = Math.min((float)n / 640.0f, (float)n2 / 400.0f);
        if (f > 1.0f) {
            return 1.0f;
        }
        if (f < 0.5f) {
            return 0.5f;
        }
        return f;
    }

    public static void begin(class_332 class_3322, int n, int n2) {
        float f = UiScale.scale(n, n2);
        float f2 = (float)n / 2.0f;
        float f3 = (float)n2 / 2.0f;
        Matrix3x2fStack matrix3x2fStack = class_3322.method_51448();
        matrix3x2fStack.pushMatrix();
        matrix3x2fStack.translate(f2, f3);
        matrix3x2fStack.scale(f, f);
        matrix3x2fStack.translate(-f2, -f3);
    }

    public static void end(class_332 class_3322) {
        class_3322.method_51448().popMatrix();
    }

    public static int unX(int n, int n2, int n3) {
        float f = UiScale.scale(n2, n3);
        float f2 = (float)n2 / 2.0f;
        return Math.round(((float)n - f2) / f + f2);
    }

    public static int unY(int n, int n2, int n3) {
        float f = UiScale.scale(n2, n3);
        float f2 = (float)n3 / 2.0f;
        return Math.round(((float)n - f2) / f + f2);
    }
}

