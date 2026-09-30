/*
 * Decompiled with CFR 0.152.
 */
package com.swiftclient.util;

public final class AnimationUtil {
    private AnimationUtil() {
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}

