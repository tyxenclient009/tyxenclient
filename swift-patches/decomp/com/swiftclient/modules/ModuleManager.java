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
import com.swiftclient.performance.DynamicRenderDistanceModule;
import com.swiftclient.performance.MemoryModule;
import com.swiftclient.performance.ParticleDensityModule;
import com.swiftclient.player.FovModule;
import com.swiftclient.player.SelfNametagModule;
import com.swiftclient.player.ToggleSneakModule;
import com.swiftclient.player.ToggleSprintModule;
import com.swiftclient.render.BadgeModule;
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
    private static final Map<String, Integer> FAULTS;

    public static void fault(Module module, Throwable throwable, String string) {
        if (module == null) {
            return;
        }
        int n = FAULTS.getOrDefault(module.name(), 0) + 1;
        FAULTS.put(module.name(), n);
        System.err.println("[swiftclient] module '" + module.name() + "' failed in " + string + " (" + n + "x): " + String.valueOf(throwable));
        if (n >= 5) {
            try {
                module.setEnabled(false);
            }
            catch (Throwable throwable2) {
                // empty catch block
            }
            System.err.println("[swiftclient] module '" + module.name() + "' auto-disabled after 5 consecutive failures.");
            FAULTS.remove(module.name());
        }
    }

    public static void guard(Module module, String string, Runnable runnable) {
        try {
            runnable.run();
            FAULTS.remove(module.name());
        }
        catch (Throwable throwable) {
            ModuleManager.fault(module, throwable, string);
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

    public static void register(Module module) {
        MODULES.add(module);
    }

    public static List<Module> all() {
        return Collections.unmodifiableList(MODULES);
    }

    public static <T extends Module> T get(Class<T> clazz) {
        for (Module module : MODULES) {
            if (!clazz.isInstance(module)) continue;
            return (T)((Module)clazz.cast(module));
        }
        return null;
    }

    public static void tick(class_310 class_3102) {
        for (Module module : MODULES) {
            if (!module.enabled()) continue;
            ModuleManager.guard(module, "tick", () -> module.tick(class_3102));
        }
    }

    public static void renderHud(class_332 class_3322, float f) {
        for (Module module : MODULES) {
            if (!module.enabled()) continue;
            ModuleManager.guard(module, "render", () -> module.render(class_3322, f));
        }
    }

    static {
        FAULTS = new HashMap<String, Integer>();
    }
}

