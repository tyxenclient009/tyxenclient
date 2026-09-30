/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package com.swiftclient.modules;

import com.swiftclient.hud.ArmorStatusModule;
import com.swiftclient.hud.ClockModule;
import com.swiftclient.hud.CoordinatesModule;
import com.swiftclient.hud.CpsModule;
import com.swiftclient.hud.DayCounterModule;
import com.swiftclient.hud.FPSModule;
import com.swiftclient.hud.KeystrokesModule;
import com.swiftclient.hud.PingModule;
import com.swiftclient.hud.PotionEffectsModule;
import com.swiftclient.modules.Module;
import com.swiftclient.player.SelfNametagModule;
import com.swiftclient.render.BadgeModule;
import com.swiftclient.performance.DynamicRenderDistanceModule;
import com.swiftclient.performance.MemoryModule;
import com.swiftclient.performance.ParticleDensityModule;
import com.swiftclient.player.FovModule;
import com.swiftclient.player.ToggleSneakModule;
import com.swiftclient.player.ToggleSprintModule;
import com.swiftclient.render.BlockOutlineModule;
import com.swiftclient.render.ChunkBordersModule;
import com.swiftclient.render.CrosshairModule;
import com.swiftclient.render.FreelookModule;
import com.swiftclient.render.FullbrightModule;
import com.swiftclient.render.GuiScaleModule;
import com.swiftclient.render.HitboxesModule;
import com.swiftclient.render.LightLevelModule;
import com.swiftclient.render.ZoomModule;
import com.swiftclient.restyle.ActionBarModule;
import com.swiftclient.restyle.BossbarModule;
import com.swiftclient.restyle.ScoreboardModule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<Module>();
    private static boolean initialized;
    private static final Map<String, Integer> FAULTS = new HashMap<String, Integer>();

    /**
     * TYX safety net — one crashing module must never take down the game
     * (or a whole HUD frame) again. Consecutive failures auto-disable the
     * module after 5 strikes; a clean frame clears its count.
     */
    public static void fault(Module m, Throwable t, String phase) {
        if (m == null) {
            return;
        }
        int n = FAULTS.getOrDefault(m.name(), 0) + 1;
        FAULTS.put(m.name(), n);
        System.err.println("[swiftclient] module '" + m.name() + "' failed in " + phase + " (" + n + "x): " + t);
        if (n >= 5) {
            try {
                m.setEnabled(false);
            } catch (Throwable ignored) {
            }
            System.err.println("[swiftclient] module '" + m.name() + "' auto-disabled after 5 consecutive failures.");
            FAULTS.remove(m.name());
        }
    }

    public static void guard(Module m, String phase, Runnable run) {
        try {
            run.run();
            FAULTS.remove(m.name());
        } catch (Throwable t) {
            ModuleManager.fault(m, t, phase);
        }
    }

    private ModuleManager() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        ModuleManager.register(new FPSModule());
        ModuleManager.register(new CoordinatesModule());
        ModuleManager.register(new KeystrokesModule());
        ModuleManager.register(new PotionEffectsModule());
        ModuleManager.register(new CpsModule());
        ModuleManager.register(new PingModule());
        ModuleManager.register(new ClockModule());
        ModuleManager.register(new DayCounterModule());
        ModuleManager.register(new ArmorStatusModule());
        ModuleManager.register(new ActionBarModule());
        ModuleManager.register(new BossbarModule());
        ModuleManager.register(new ScoreboardModule());
        ModuleManager.register(new ZoomModule());
        ModuleManager.register(new FreelookModule());
        ModuleManager.register(new CrosshairModule());
        ModuleManager.register(new GuiScaleModule());
        ModuleManager.register(new BlockOutlineModule());
        ModuleManager.register(new ChunkBordersModule());
        ModuleManager.register(new LightLevelModule());
        ModuleManager.register(new HitboxesModule());
        ModuleManager.register(new FullbrightModule());
        ModuleManager.register(new ToggleSneakModule());
        ModuleManager.register(new ToggleSprintModule());
        ModuleManager.register(new FovModule());
        ModuleManager.register(new ParticleDensityModule());
        ModuleManager.register(new DynamicRenderDistanceModule());
        ModuleManager.register(new MemoryModule());
        ModuleManager.register(new BadgeModule());
        ModuleManager.register(new SelfNametagModule());
        MODULES.forEach(Module::init);
    }

    public static void register(Module m) {
        MODULES.add(m);
    }

    public static List<Module> all() {
        return Collections.unmodifiableList(MODULES);
    }

    public static <T extends Module> T get(Class<T> type) {
        for (Module m : MODULES) {
            if (!type.isInstance(m)) continue;
            return (T)((Module)type.cast(m));
        }
        return null;
    }

    public static void tick(class_310 c) {
        for (Module m : MODULES) {
            if (!m.enabled()) continue;
            ModuleManager.guard(m, "tick", () -> m.tick(c));
        }
    }

    public static void renderHud(class_332 ctx, float delta) {
        for (Module m : MODULES) {
            if (!m.enabled()) continue;
            ModuleManager.guard(m, "render", () -> m.render(ctx, delta));
        }
    }
}

