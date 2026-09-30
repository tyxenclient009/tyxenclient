/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.tyxen.hud.modules.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.modules.Category;
import net.tyxen.hud.modules.Module;
import net.tyxen.hud.modules.settings.BooleanSetting;

@Environment(value=EnvType.CLIENT)
public class PingOverlay
extends Module {
    private final BooleanSetting colorCoded = this.register(new BooleanSetting("color_coded", "Color code by ping quality", true));
    private final BooleanSetting showMs = this.register(new BooleanSetting("show_ms", "Show 'ms' suffix", true));

    public PingOverlay() {
        super("PingOverlay", "Show numeric ping on the player list", Category.RENDER);
    }

    public String formatPing(int latency) {
        if (latency <= 0) {
            return "?";
        }
        return this.showMs.isEnabled() ? latency + "ms" : String.valueOf(latency);
    }

    public int getPingColor(int latency) {
        if (!this.colorCoded.isEnabled()) {
            return -1;
        }
        if (latency <= 0) {
            return -5592406;
        }
        if (latency < 75) {
            return -16711936;
        }
        if (latency < 150) {
            return -8323328;
        }
        if (latency < 300) {
            return -256;
        }
        if (latency < 600) {
            return Short.MIN_VALUE;
        }
        return -49088;
    }
}

