/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
 *  net.minecraft.class_304
 *  net.minecraft.class_304$class_11900
 *  net.minecraft.class_310
 *  net.minecraft.class_3675$class_307
 *  net.minecraft.class_437
 *  org.lwjgl.glfw.GLFW
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.tyxen.hud.core;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.tyxen.hud.core.ModuleManager;
import net.tyxen.hud.gui.screens.HudOverlayScreen;
import net.tyxen.hud.gui.screens.WaypointsScreen;
import net.tyxen.hud.modules.Module;
import net.tyxen.hud.modules.impl.render.WaypointsModule;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_437;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(value=EnvType.CLIENT)
public class KeybindManager {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"Tyxen");
    private static KeybindManager instance;
    private class_304 zoomKey;
    private class_304 fpsToggleKey;
    private class_304 fullbrightToggleKey;
    private class_304 clickGuiKey;
    private boolean fpsKeyWasDown = false;
    private boolean fullbrightKeyWasDown = false;
    private boolean clickGuiKeyWasDown = false;
    private boolean zoomKeyWasDown = false;
    private final Map<String, Boolean> customKeyStates = new HashMap<String, Boolean>();

    public KeybindManager() {
        instance = this;
    }

    public static KeybindManager getInstance() {
        return instance;
    }

    public void init() {
        this.zoomKey = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.fast-client-hud.zoom", class_3675.class_307.field_1668, -1, class_304.class_11900.field_62556));
        this.fpsToggleKey = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.fast-client-hud.fps_toggle", class_3675.class_307.field_1668, 295, class_304.class_11900.field_62556));
        this.fullbrightToggleKey = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.fast-client-hud.fullbright_toggle", class_3675.class_307.field_1668, 296, class_304.class_11900.field_62556));
        this.clickGuiKey = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.fast-client-hud.click_gui", class_3675.class_307.field_1668, 344, class_304.class_11900.field_62556));
        ClientTickEvents.END_CLIENT_TICK.register(client -> this.handleKeybinds());
        LOGGER.info("[KeybindManager] Initialized");
    }

    private void handleKeybinds() {
        boolean clickGuiKeyDown;
        Module fullbrightModule;
        Module fpsModule;
        class_310 mc = class_310.method_1551();
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) {
            return;
        }
        long windowHandle = mc.method_22683().method_4490();
        Module zoomModule = mm.getModule("zoom");
        if (zoomModule != null) {
            boolean zoomKeyDown;
            boolean bl = zoomKeyDown = this.zoomKey.method_1434() || this.isCustomKeyDownWithModifiers(zoomModule, windowHandle);
            if (zoomModule.isKeyHeld()) {
                if (zoomKeyDown && !zoomModule.isEnabled()) {
                    zoomModule.setEnabled(true);
                } else if (!zoomKeyDown && zoomModule.isEnabled()) {
                    zoomModule.setEnabled(false);
                }
            } else if (zoomKeyDown && !this.zoomKeyWasDown) {
                mm.toggleModule(zoomModule);
            }
            this.zoomKeyWasDown = zoomKeyDown;
        }
        if ((fpsModule = mm.getModule("fps")) != null) {
            boolean fpsKeyDown;
            boolean bl = fpsKeyDown = this.fpsToggleKey.method_1434() || this.isCustomKeyDownWithModifiers(fpsModule, windowHandle);
            if (fpsKeyDown && !this.fpsKeyWasDown) {
                mm.toggleModule(fpsModule);
            }
            this.fpsKeyWasDown = fpsKeyDown;
        }
        if ((fullbrightModule = mm.getModule("fullbright")) != null) {
            boolean fullbrightKeyDown;
            boolean bl = fullbrightKeyDown = this.fullbrightToggleKey.method_1434() || this.isCustomKeyDownWithModifiers(fullbrightModule, windowHandle);
            if (fullbrightKeyDown && !this.fullbrightKeyWasDown) {
                mm.toggleModule(fullbrightModule);
            }
            this.fullbrightKeyWasDown = fullbrightKeyDown;
        }
        if ((clickGuiKeyDown = this.clickGuiKey.method_1434()) && !this.clickGuiKeyWasDown && mc.field_1755 == null) {
            mc.method_1507((class_437)new HudOverlayScreen());
        }
        this.clickGuiKeyWasDown = clickGuiKeyDown;
        this.handleCustomModuleKeybinds(mm, mc, windowHandle);
    }

    private boolean isCustomKeyDownWithModifiers(Module module, long windowHandle) {
        int keyCode = module.getKeyBinding();
        if (keyCode == 0) {
            return false;
        }
        if (GLFW.glfwGetKey((long)windowHandle, (int)keyCode) != 1) {
            return false;
        }
        int requiredMods = module.getKeyModifiers();
        return this.areModifiersMatching(windowHandle, requiredMods);
    }

    private boolean areModifiersMatching(long windowHandle, int requiredMods) {
        boolean shiftRequired = (requiredMods & 1) != 0;
        boolean ctrlRequired = (requiredMods & 2) != 0;
        boolean altRequired = (requiredMods & 4) != 0;
        boolean shiftDown = GLFW.glfwGetKey((long)windowHandle, (int)340) == 1 || GLFW.glfwGetKey((long)windowHandle, (int)344) == 1;
        boolean ctrlDown = GLFW.glfwGetKey((long)windowHandle, (int)341) == 1 || GLFW.glfwGetKey((long)windowHandle, (int)345) == 1;
        boolean altDown = GLFW.glfwGetKey((long)windowHandle, (int)342) == 1 || GLFW.glfwGetKey((long)windowHandle, (int)346) == 1;
        return shiftDown == shiftRequired && ctrlDown == ctrlRequired && altDown == altRequired;
    }

    private void handleCustomModuleKeybinds(ModuleManager mm, class_310 mc, long windowHandle) {
        boolean screenOpen = mc.field_1755 != null;
        for (Module module : mm.getModules()) {
            String moduleName;
            int keyCode = module.getKeyBinding();
            if (keyCode == 0 || (moduleName = module.getName().toLowerCase(Locale.ROOT)).equals("zoom") || moduleName.equals("fps") || moduleName.equals("fullbright")) continue;
            boolean keyDown = GLFW.glfwGetKey((long)windowHandle, (int)keyCode) == 1;
            boolean modifiersMatch = this.areModifiersMatching(windowHandle, module.getKeyModifiers());
            boolean fullMatch = keyDown && modifiersMatch;
            boolean wasDown = this.customKeyStates.getOrDefault(moduleName, false);
            if (module instanceof WaypointsModule) {
                WaypointsModule waypoints = (WaypointsModule)module;
                if (!screenOpen && waypoints.isEnabled() && fullMatch && !wasDown) {
                    mc.method_1507((class_437)new WaypointsScreen(waypoints));
                }
                this.customKeyStates.put(moduleName, fullMatch);
                continue;
            }
            if (module.isKeyHeld()) {
                if (fullMatch && !module.isEnabled()) {
                    module.setEnabled(true);
                } else if (!fullMatch && module.isEnabled()) {
                    module.setEnabled(false);
                }
            } else if (!screenOpen && fullMatch && !wasDown) {
                mm.toggleModule(module);
            }
            this.customKeyStates.put(moduleName, fullMatch);
        }
    }

    public class_304 getZoomKey() {
        return this.zoomKey;
    }
}

