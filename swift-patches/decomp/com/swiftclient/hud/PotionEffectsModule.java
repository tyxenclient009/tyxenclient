/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1291
 *  net.minecraft.class_1293
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_746
 */
package com.swiftclient.hud;

import com.swiftclient.config.ClientConfig;
import com.swiftclient.hud.HudUtil;
import com.swiftclient.modules.Category;
import com.swiftclient.modules.Module;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_746;

public final class PotionEffectsModule
extends Module {
    public PotionEffectsModule() {
        super("Potion Effects", Category.HUD, "Active status effects and durations.", 0);
    }

    @Override
    public int defaultY() {
        return 44;
    }

    @Override
    public void render(class_332 c, float d) {
        class_746 p = class_310.method_1551().field_1724;
        if (p == null) {
            return;
        }
        class_327 tr = class_310.method_1551().field_1772;
        boolean dur = ClientConfig.getBool(this.name(), "duration", true);
        int x = ClientConfig.getX(this.name(), this.defaultX());
        int y = ClientConfig.getY(this.name(), this.defaultY());
        int rows = 0;
        for (class_1293 e : p.method_6026()) {
            Object s = ((class_1291)e.method_5579().comp_349()).method_5560().getString();
            if (dur) {
                s = (String)s + "  " + this.format(e.method_5584());
            }
            HudUtil.label(c, tr, (String)s, x, y, this.name());
            y += 12;
            ++rows;
        }
        HudUtil.BOUNDS.put(this.name(), new int[]{x, y - 12 * rows - 2, 130, Math.max(1, rows) * 12});
    }

    @Override
    public boolean hasBackground() {
        return true;
    }

    private String format(int t) {
        int sec = t / 20;
        return sec / 60 + ":" + String.format("%02d", sec % 60);
    }
}

