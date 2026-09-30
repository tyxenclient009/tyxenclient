/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
 *  net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
 *  net.minecraft.class_304
 *  net.minecraft.class_304$class_11900
 *  net.minecraft.class_310
 *  net.minecraft.class_3675$class_307
 *  net.minecraft.class_437
 */
package com.swiftclient.core;

import com.swiftclient.config.ClientConfig;
import com.swiftclient.config.Theme;
import com.swiftclient.gui.SwiftAccountsScreen;
import com.swiftclient.gui.SwiftClickGui;
import com.swiftclient.gui.SwiftHudEditor;
import com.swiftclient.gui.SwiftModsMenu;
import com.swiftclient.gui.SwiftModuleSettings;
import com.swiftclient.modules.ModuleManager;
import com.swiftclient.render.WorldOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_437;

public final class SwiftClient
implements ClientModInitializer {
    public static final String MOD_ID = "swiftclient";
    public static final String VERSION = "1.0.0";
    public static class_304 OPEN_GUI;
    private static SwiftClient INSTANCE;

    public static SwiftClient getInstance() {
        return INSTANCE;
    }

    public void onInitializeClient() {
        INSTANCE = this;
        ClientConfig.load();
        Theme.load(ClientConfig.getTheme());
        ModuleManager.init();
        WorldOverlay.register();
        OPEN_GUI = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.swiftclient.open_gui", class_3675.class_307.field_1668, 344, class_304.class_11900.field_62556));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ModuleManager.tick(client);
            while (OPEN_GUI.method_1436()) {
                if (client.field_1755 == null) {
                    client.method_1507((class_437)new SwiftClickGui());
                    continue;
                }
                if (!SwiftClient.isClientScreen(client.field_1755)) continue;
                client.method_1507(null);
            }
        });
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> ModuleManager.renderHud(drawContext, tickCounter.method_60637(true)));
    }

    public static void openGui() {
        class_310 client = class_310.method_1551();
        client.method_1507((class_437)new SwiftClickGui());
    }

    private static boolean isClientScreen(class_437 screen) {
        return screen instanceof SwiftClickGui || screen instanceof SwiftModsMenu || screen instanceof SwiftModuleSettings || screen instanceof SwiftHudEditor || screen instanceof SwiftAccountsScreen;
    }
}

