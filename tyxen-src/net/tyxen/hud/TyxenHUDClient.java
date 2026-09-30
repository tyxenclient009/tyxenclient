/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
 *  net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
 *  net.fabricmc.loader.api.FabricLoader
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.tyxen.hud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.tyxen.hud.core.KeybindManager;
import net.tyxen.hud.core.ModuleManager;
import net.tyxen.hud.network.TyxenUserCache;
import net.tyxen.hud.render.BlockOverlayRenderer;
import net.tyxen.hud.render.RenderManager;
import net.tyxen.hud.render.WaypointWorldRenderer;
import net.tyxen.hud.render.WorldHealthBarRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(value=EnvType.CLIENT)
public class TyxenHUDClient
implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger((String)"Tyxen");
    public static final String MOD_ID = "tyxen";
    public static final String MC_VERSION = "1.21.11";
    public static final String RELEASE_ID = "ca786cd3";
    public static final String VERSION_LABEL = "Tyxen 1.21.11";
    private static TyxenHUDClient instance;
    private ModuleManager moduleManager;
    private KeybindManager keybindManager;
    private RenderManager renderManager;

    public static String getModVersion() {
        return FabricLoader.getInstance().getModContainer(MOD_ID).map(container -> container.getMetadata().getVersion().getFriendlyString()).orElse("1.0.0");
    }

    public void onInitializeClient() {
        instance = this;
        LOGGER.info("Tyxen initializing...");
        this.moduleManager = new ModuleManager();
        this.keybindManager = new KeybindManager();
        this.renderManager = new RenderManager();
        this.keybindManager.init();
        this.renderManager.init();
        this.moduleManager.registerModules();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.field_1724 != null) {
                this.moduleManager.onTick();
            }
        });
        WorldRenderEvents.AFTER_ENTITIES.register(WorldHealthBarRenderer::onAfterEntities);
        WorldRenderEvents.AFTER_ENTITIES.register(BlockOverlayRenderer::onAfterEntities);
        net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
                (entityType, entityRenderer, helper, context) -> helper.register(new net.tyxen.hud.cosmetics.WingsFeatureRenderer(entityRenderer)));
        WorldRenderEvents.BEFORE_ENTITIES.register(WaypointWorldRenderer::onBeforeEntities);
        TyxenUserCache.getInstance();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.field_1724 == null) {
                return;
            }
            String localUsername = client.field_1724.method_7334().name();
            TyxenUserCache cache = TyxenUserCache.getInstance();
            cache.pingServer(localUsername);
        });
        ClientPlayConnectionEvents.DISCONNECT.register((_handler, _client) -> TyxenUserCache.getInstance().onDisconnect());
        LOGGER.info("Tyxen initialized!");
    }

    public static TyxenHUDClient getInstance() {
        return instance;
    }

    public ModuleManager getModuleManager() {
        return this.moduleManager;
    }

    public RenderManager getRenderManager() {
        return this.renderManager;
    }
}

