/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
 *  net.minecraft.class_156
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_370
 *  net.minecraft.class_370$class_9037
 *  net.minecraft.class_374
 *  net.minecraft.class_407
 *  net.minecraft.class_429
 *  net.minecraft.class_4325
 *  net.minecraft.class_433
 *  net.minecraft.class_436
 *  net.minecraft.class_437
 *  net.minecraft.class_447
 *  net.minecraft.class_457
 *  net.minecraft.class_5375
 *  net.minecraft.class_5522
 *  net.minecraft.class_638
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.fastclient.hud.mixin.client;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fastclient.hud.gui.DisplaySpace;
import net.fastclient.hud.gui.screens.ClickGUIScreen;
import net.fastclient.hud.launcher.LauncherRenderer;
import net.fastclient.hud.launcher.LauncherSkinPreference;
import net.fastclient.hud.launcher.OptionalMenuIntegrations;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_370;
import net.minecraft.class_374;
import net.minecraft.class_407;
import net.minecraft.class_429;
import net.minecraft.class_4325;
import net.minecraft.class_433;
import net.minecraft.class_436;
import net.minecraft.class_437;
import net.minecraft.class_447;
import net.minecraft.class_457;
import net.minecraft.class_5375;
import net.minecraft.class_5522;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_433.class})
public class PauseScreenMixin {
    @Inject(method={"method_25426"}, at={@At(value="HEAD")}, cancellable=true)
    private void onInit(CallbackInfo ci) {
        if (this.showsMenu()) {
            class_433 screen = (class_433)this;
            ScreenMouseEvents.allowMouseClick((class_437)screen).register((ignored, event) -> !this.handlePauseClick(event.comp_4798(), event.comp_4799(), event.method_74245()));
            if (LauncherSkinPreference.isFastClientSkinEnabled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method={"method_25420"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderBackground(class_332 graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.showsMenu() && LauncherSkinPreference.isFastClientSkinEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method={"method_25394"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRender(class_332 graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!this.showsMenu() || !LauncherSkinPreference.isFastClientSkinEnabled()) {
            return;
        }
        class_310 mc = class_310.method_1551();
        DisplaySpace.push(graphics);
        LauncherRenderer.renderPause(graphics, mc.field_1772, DisplaySpace.width(), DisplaySpace.height(), DisplaySpace.mouseX(mouseX), DisplaySpace.mouseY(mouseY));
        DisplaySpace.pop(graphics);
        ci.cancel();
    }

    @Inject(method={"method_25394"}, at={@At(value="TAIL")})
    private void onVanillaRender(class_332 graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!this.showsMenu() || LauncherSkinPreference.isFastClientSkinEnabled()) {
            return;
        }
        DisplaySpace.push(graphics);
        LauncherRenderer.renderVanillaOverlay(graphics, class_310.method_1551().field_1772, DisplaySpace.width(), DisplaySpace.height(), DisplaySpace.mouseX(mouseX), DisplaySpace.mouseY(mouseY));
        DisplaySpace.pop(graphics);
    }

    private boolean handlePauseClick(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }
        int pxMouseX = DisplaySpace.mouseX(mouseX);
        int pxMouseY = DisplaySpace.mouseY(mouseY);
        class_310 mc = class_310.method_1551();
        if (LauncherRenderer.isSkinToggleClicked(DisplaySpace.width(), DisplaySpace.height(), pxMouseX, pxMouseY)) {
            LauncherSkinPreference.toggle();
            mc.method_1507((class_437)new class_433(this.showsMenu()));
            return true;
        }
        if (!LauncherSkinPreference.isFastClientSkinEnabled()) {
            if (LauncherRenderer.isDiscordClicked(DisplaySpace.width(), DisplaySpace.height(), pxMouseX, pxMouseY)) {
                class_407.method_60866((class_437)((class_437)this), (String)"https://discord.gg/RGzATq3v7J", (boolean)true);
                return true;
            }
            return false;
        }
        String clicked = LauncherRenderer.getClickedButton(pxMouseX, pxMouseY);
        if (clicked == null) {
            return true;
        }
        class_437 self = (class_437)this;
        switch (clicked) {
            case "pause_backtogame": {
                mc.method_1507(null);
                mc.field_1729.method_1612();
                break;
            }
            case "pause_fastclient_settings": 
            case "window": {
                mc.method_1507((class_437)new ClickGUIScreen());
                break;
            }
            case "pause_store": {
                class_370.method_27024((class_374)mc.method_1566(), (class_370.class_9037)class_370.class_9037.field_47588, (class_2561)class_2561.method_43470((String)"FastClient Store"), (class_2561)class_2561.method_43470((String)"Coming Soon"));
                break;
            }
            case "pause_modmenu": {
                OptionalMenuIntegrations.openModMenu(self);
                break;
            }
            case "pause_options": 
            case "settings": {
                mc.method_1507((class_437)new class_429(self, mc.field_1690));
                break;
            }
            case "pause_open_to_lan": {
                mc.method_1507((class_437)new class_436(self));
                break;
            }
            case "box": {
                PauseScreenMixin.openResourcePacks(mc, self);
                break;
            }
            case "diamond": {
                mc.method_1507((class_437)new class_4325(self));
                break;
            }
            case "pause_disconnect": {
                mc.method_73360(class_638.field_61021);
                break;
            }
            case "pause_advancements": {
                mc.method_1507((class_437)new class_457(mc.field_1724.field_3944.method_2869(), self));
                break;
            }
            case "pause_statistics": {
                mc.method_1507((class_437)new class_447(self, mc.field_1724.method_3143()));
                break;
            }
            case "pause_player_reporting": {
                mc.method_1507((class_437)new class_5522(self));
                break;
            }
            case "pause_minecraftfolder": {
                PauseScreenMixin.openGameDirectory(mc);
                break;
            }
            case "flashback_record_start": {
                OptionalMenuIntegrations.startFlashbackRecording();
                break;
            }
            case "flashback_record_finish": {
                OptionalMenuIntegrations.finishFlashbackRecording();
                break;
            }
            case "flashback_record_pause": {
                OptionalMenuIntegrations.pauseFlashbackRecording(true);
                break;
            }
            case "flashback_record_resume": {
                OptionalMenuIntegrations.pauseFlashbackRecording(false);
                break;
            }
            case "flashback_record_cancel": {
                OptionalMenuIntegrations.confirmCancelFlashbackRecording();
                break;
            }
            default: {
                class_370.method_27024((class_374)mc.method_1566(), (class_370.class_9037)class_370.class_9037.field_47588, (class_2561)class_2561.method_43470((String)"FastClient"), (class_2561)class_2561.method_43470((String)"Coming Soon"));
            }
        }
        return true;
    }

    private static void openResourcePacks(class_310 mc, class_437 parent) {
        mc.method_1507((class_437)new class_5375(mc.method_1520(), repository -> {
            mc.field_1690.method_49598(repository);
            mc.method_1507(parent);
        }, mc.method_1479(), (class_2561)class_2561.method_43471((String)"resourcePack.title")));
    }

    private static void openGameDirectory(class_310 mc) {
        boolean opened = false;
        if (PauseScreenMixin.isWindows()) {
            try {
                new ProcessBuilder("explorer.exe", mc.field_1697.getAbsolutePath()).start();
                opened = true;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (!opened) {
            try {
                class_156.method_668().method_672(mc.field_1697);
                opened = true;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (!opened) {
            class_370.method_27024((class_374)mc.method_1566(), (class_370.class_9037)class_370.class_9037.field_47588, (class_2561)class_2561.method_43470((String)"FastClient"), (class_2561)class_2561.method_43470((String)"Could not open folder"));
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows");
    }

    private boolean showsMenu() {
        return ((class_433)this).method_53558();
    }
}

