/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
 *  net.minecraft.class_332
 *  net.minecraft.class_9779
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.tyxen.hud.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.tyxen.hud.core.ModuleManager;
import net.tyxen.hud.gui.DisplaySpace;
import net.tyxen.hud.modules.Category;
import net.tyxen.hud.modules.Module;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(value=EnvType.CLIENT)
public class RenderManager {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"Tyxen");
    private static RenderManager instance;

    public RenderManager() {
        instance = this;
    }

    public static RenderManager getInstance() {
        return instance;
    }

    public void init() {
        HudRenderCallback.EVENT.register(this::onHudRender);
        LOGGER.info("[RenderManager] Initialized");
    }

    private void onHudRender(class_332 graphics, class_9779 deltaTracker) {
        this.renderAllModules(graphics, deltaTracker);
    }

    public void onRenderHudDirect(class_332 graphics, class_9779 deltaTracker) {
        this.renderAllModules(graphics, deltaTracker);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void renderAllModules(class_332 graphics, class_9779 deltaTracker) {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) {
            return;
        }
        float tickDelta = deltaTracker.method_60637(true);
        DisplaySpace.push(graphics);
        try {
            for (Module module : mm.getModulesByCategory(Category.HUD)) {
                if (!module.isEnabled()) continue;
                try {
                    module.onRender(graphics, tickDelta);
                }
                catch (Exception e) {
                    LOGGER.error("[{}] Error in render: {}", (Object)module.getName(), (Object)e.getMessage());
                }
            }
            for (Module module : mm.getModulesByCategory(Category.RENDER)) {
                if (!module.isEnabled()) continue;
                try {
                    module.onRender(graphics, tickDelta);
                }
                catch (Exception e) {
                    LOGGER.error("[{}] Error in render: {}", (Object)module.getName(), (Object)e.getMessage());
                }
            }
            for (Module module : mm.getModulesByCategory(Category.UTILITY)) {
                if (!module.isEnabled()) continue;
                try {
                    module.onRender(graphics, tickDelta);
                }
                catch (Exception e) {
                    LOGGER.error("[{}] Error in render: {}", (Object)module.getName(), (Object)e.getMessage());
                }
            }
        }
        finally {
            DisplaySpace.pop(graphics);
        }
    }
}

