/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_5348
 */
package com.swiftclient.hud;

import com.swiftclient.config.ClientConfig;
import com.swiftclient.util.SwiftText;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_5348;

public final class HudUtil {
    public static final Map<String, int[]> BOUNDS = new HashMap<String, int[]>();

    private HudUtil() {
    }

    public static void label(class_332 c, class_327 tr, String text, int x, int y, String module) {
        boolean bg = ClientConfig.getBool(module, "background", true);
        int w = tr.method_27525((class_5348)SwiftText.of(text));
        if (bg) {
            int a = Math.max(20, Math.min(255, ClientConfig.getInt(module, "opacity", 255)));
            c.method_25294(x - 3, y - 2, x + w + 3, y + 12, a << 24 | 0x50507);
        }
        c.method_27535(tr, SwiftText.of(text), x, y + 1, -855305);
        BOUNDS.put(module, new int[]{x - 3, y - 2, w + 6, 14});
    }
}

