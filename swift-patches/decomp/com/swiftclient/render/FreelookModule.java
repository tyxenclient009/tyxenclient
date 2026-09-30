/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_746
 */
package com.swiftclient.render;

import com.swiftclient.modules.Category;
import com.swiftclient.modules.Module;
import net.minecraft.class_310;
import net.minecraft.class_746;

public final class FreelookModule
extends Module {
    public static float camYaw;
    public static float camPitch;
    public static boolean looking;

    public FreelookModule() {
        super("Freelook", Category.RENDER, "Look around freely while enabled.", 0);
    }

    @Override
    public void onEnable() {
        class_746 p = class_310.method_1551().field_1724;
        if (p != null) {
            camYaw = p.method_36454();
            camPitch = p.method_36455();
        }
        looking = true;
    }

    @Override
    public void onDisable() {
        looking = false;
        class_746 p = class_310.method_1551().field_1724;
        if (p != null) {
            p.method_36456(camYaw);
            p.method_36457(camPitch);
        }
    }
}

