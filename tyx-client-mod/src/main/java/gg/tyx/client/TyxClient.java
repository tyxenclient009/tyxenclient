package gg.tyx.client;

import gg.tyx.client.gui.TyxMenuScreen;
import gg.tyx.client.hud.GearHud;
import gg.tyx.client.hud.KeystrokesHud;
import gg.tyx.client.hud.TyxHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Tyx Client — Lunar-style in-game companion mod.
 * Right Shift opens the mod menu, the Tyx badge renders on player nametags
 * (see NametagBadgeMixin), plus HUD modules and movement toggles.
 */
public final class TyxClient implements ClientModInitializer {
    public static TyxConfig CONFIG;
    public static KeyBinding MENU_KEY;
    public static KeyBinding ZOOM_KEY;
    private static int savedFov = -1;

    @Override
    public void onInitializeClient() {
        CONFIG = TyxConfig.load();
        MENU_KEY = KeyBindingHelper.registerKeyBinding(Keybinds.create(
                "key.tyxclient.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT));
        ZOOM_KEY = KeyBindingHelper.registerKeyBinding(Keybinds.create(
                "key.tyxclient.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C));
        HudRenderCallback.EVENT.register(new TyxHud());
        HudRenderCallback.EVENT.register(new KeystrokesHud());
        HudRenderCallback.EVENT.register(new GearHud());
        AutoGg.init();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (MENU_KEY.wasPressed() && client.currentScreen == null) {
                client.setScreen(new TyxMenuScreen(null));
            }
            if (client.player == null) return;
            if (client.options.attackKey.wasPressed()) KeystrokesHud.click(true);
            if (client.options.useKey.wasPressed()) KeystrokesHud.click(false);
            movement(client);
            zoom(client);
            AutoGg.tick(client);
            applyGamma(client);
        });
    }

    /** ToggleSprint / ToggleSneak that never fights manual input. */
    private static void movement(MinecraftClient client) {
        var p = client.player;
        if (CONFIG.toggleSprint && client.options.forwardKey.isPressed() && !p.isSneaking()
                && !p.horizontalCollision) {
            p.setSprinting(true);
        }
        if (CONFIG.toggleSneak) {
            client.options.sneakKey.setPressed(true);
        }
    }

    /** Hold-to-zoom (OptiFine-style), restores your exact FOV on release. */
    private static void zoom(MinecraftClient client) {
        try {
            var fov = client.options.getFov();
            if (ZOOM_KEY.isPressed()) {
                if (savedFov < 0) savedFov = fov.getValue();
                fov.setValue(30);
            } else if (savedFov >= 0) {
                fov.setValue(savedFov);
                savedFov = -1;
            }
        } catch (Exception ignored) {
            // Option internals differ across snapshots — menu still works.
        }
    }

    /** Fullbright that never fights the user: only touches gamma while enabled. */
    private static void applyGamma(MinecraftClient client) {
        if (client.options == null) return;
        try {
            double want = CONFIG.fullbright ? 16.0 : 1.0;
            double cur = client.options.getGamma().getValue();
            if (Math.abs(cur - want) > 0.001) {
                client.options.getGamma().setValue(want);
            }
        } catch (Exception ignored) {
            // Option internals differ across snapshots — HUD still works.
        }
    }
}
