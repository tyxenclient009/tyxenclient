/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2960
 */
package com.swiftclient.gui;

import com.swiftclient.modules.Module;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_2960;

public final class ModuleIcons {
    private static final Map<String, Integer> COLORS = Map.ofEntries(Map.entry("fps", -11549705), Map.entry("coordinates", -11677536), Map.entry("keystrokes", -10929), Map.entry("potion_effects", -4560696), Map.entry("armor_status", -7297874), Map.entry("cps", -30107), Map.entry("ping", -11684180), Map.entry("clock", -8812853), Map.entry("day_counter", -18611), Map.entry("actionbar", -1023342), Map.entry("bossbar", -1739917), Map.entry("scoreboard", -5319295), Map.entry("zoom", -10177034), Map.entry("freelook", -8331542), Map.entry("crosshair", -1092784), Map.entry("gui_scale", -3238952), Map.entry("block_outline", -3722), Map.entry("chunk_borders", -8336444), Map.entry("light_level", -8062), Map.entry("hitboxes", -1074534), Map.entry("fullbright", -2659), Map.entry("toggle_sneak", -5908825), Map.entry("toggle_sprint", -7288071), Map.entry("fov", -5005861), Map.entry("particle_density", -5054501), Map.entry("dynamic_render_distance", -6313766), Map.entry("memory", -4412764));

    private ModuleIcons() {
    }

    public static String key(Module m) {
        return m.name().toLowerCase(Locale.ROOT).replace(' ', '_').replaceAll("[^a-z0-9_]", "");
    }

    public static class_2960 id(Module m) {
        return class_2960.method_60655((String)"swiftclient", (String)("textures/gui/icons/icon_" + ModuleIcons.key(m) + ".png"));
    }

    public static int color(Module m) {
        return COLORS.getOrDefault(ModuleIcons.key(m), -855305);
    }
}

