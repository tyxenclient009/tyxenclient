/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10799
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_370
 *  net.minecraft.class_370$class_9037
 *  net.minecraft.class_374
 *  net.minecraft.class_407
 *  net.minecraft.class_412
 *  net.minecraft.class_429
 *  net.minecraft.class_4325
 *  net.minecraft.class_437
 *  net.minecraft.class_442
 *  net.minecraft.class_500
 *  net.minecraft.class_526
 *  net.minecraft.class_5375
 *  net.minecraft.class_639
 *  net.minecraft.class_642
 *  net.minecraft.class_642$class_8678
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.tyxen.hud.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.gui.DisplaySpace;
import net.tyxen.hud.gui.screens.ClickGUIScreen;
import net.tyxen.hud.gui.screens.ScreenshotGalleryScreen;
import net.tyxen.hud.launcher.LauncherRenderer;
import net.tyxen.hud.launcher.LauncherSkinPreference;
import net.tyxen.hud.launcher.OptionalMenuIntegrations;
import net.minecraft.class_10799;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_370;
import net.minecraft.class_374;
import net.minecraft.class_407;
import net.minecraft.class_412;
import net.minecraft.class_429;
import net.minecraft.class_4325;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_500;
import net.minecraft.class_526;
import net.minecraft.class_5375;
import net.minecraft.class_639;
import net.minecraft.class_642;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_442.class})
public abstract class TitleScreenMixin extends class_437 {
    protected TitleScreenMixin(class_2561 title) {
        super(title);
    }

    private static final int BACKGROUND_WIDTH = 3840;
    private static final int BACKGROUND_HEIGHT = 2160;
    private static final class_2960 BACKGROUND = class_2960.method_60655((String)"tyxen", (String)"textures/gui/new-background.png");

    @Inject(method={"method_25426"}, at={@At(value="HEAD")}, cancellable=true)
    private void onInit(CallbackInfo ci) {
        if (LauncherSkinPreference.isTyxenSkinEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method={"method_25420"}, at={@At(value="HEAD")}, cancellable=true)
    private void onExtractBackground(class_332 graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (LauncherSkinPreference.isTyxenSkinEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method={"method_25394"}, at={@At(value="HEAD")}, cancellable=true)
    private void onExtractRenderState(class_332 graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!LauncherSkinPreference.isTyxenSkinEnabled()) {
            return;
        }
        class_310 mc = class_310.method_1551();
        int width = DisplaySpace.width();
        int height = DisplaySpace.height();
        int pxMouseX = DisplaySpace.mouseX(mouseX);
        int pxMouseY = DisplaySpace.mouseY(mouseY);
        DisplaySpace.push(graphics);
        float backgroundAspect = 1.7777778f;
        int backgroundWidth = width;
        int backgroundHeight = Math.round((float)backgroundWidth / backgroundAspect);
        if (backgroundHeight < height) {
            backgroundHeight = height;
            backgroundWidth = Math.round((float)backgroundHeight * backgroundAspect);
        }
        int backgroundX = (width - backgroundWidth) / 2;
        int backgroundY = (height - backgroundHeight) / 2;
        graphics.method_25291(class_10799.field_56883, DisplaySpace.texture(BACKGROUND), backgroundX, backgroundY, 0.0f, 0.0f, backgroundWidth, backgroundHeight, backgroundWidth, backgroundHeight, -1);
        LauncherRenderer.render(graphics, mc.field_1772, width, height, pxMouseX, pxMouseY);
        DisplaySpace.pop(graphics);
        ci.cancel();
    }

    @Inject(method={"method_25394"}, at={@At(value="TAIL")})
    private void onVanillaRender(class_332 graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (LauncherSkinPreference.isTyxenSkinEnabled()) {
            return;
        }
        DisplaySpace.push(graphics);
        LauncherRenderer.renderVanillaOverlay(graphics, class_310.method_1551().field_1772, DisplaySpace.width(), DisplaySpace.height(), DisplaySpace.mouseX(mouseX), DisplaySpace.mouseY(mouseY));
        DisplaySpace.pop(graphics);
    }

    @Inject(method={"method_25402"}, at={@At(value="HEAD")}, cancellable=true)
    private void onMouseClicked(class_11909 event, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        if (event.method_74245() != 0) {
            return;
        }
        int mouseX = DisplaySpace.mouseX(event.comp_4798());
        int mouseY = DisplaySpace.mouseY(event.comp_4799());
        class_310 mc = class_310.method_1551();
        if (LauncherRenderer.isSkinToggleClicked(DisplaySpace.width(), DisplaySpace.height(), mouseX, mouseY)) {
            LauncherSkinPreference.toggle();
            mc.method_1507((class_437)new class_442());
            cir.setReturnValue(true);
            return;
        }
        if (!LauncherSkinPreference.isTyxenSkinEnabled()) {
            if (LauncherRenderer.isDiscordClicked(DisplaySpace.width(), DisplaySpace.height(), mouseX, mouseY)) {
                class_407.method_60866((class_437)((class_437)this), (String)"https://dsc.gg/tyxenclient", (boolean)true);
                cir.setReturnValue(true);
            }
            return;
        }
        String clicked = LauncherRenderer.getClickedButton(mouseX, mouseY);
        if (clicked == null) {
            cir.setReturnValue(true);
            return;
        }
        class_437 self = (class_437)this;
        switch (clicked) {
            case "singleplayer": {
                mc.method_1507((class_437)new class_526(self));
                break;
            }
            case "multiplayer": {
                mc.method_1507((class_437)new class_500(self));
                break;
            }
            case "modmenu": {
                OptionalMenuIntegrations.openModMenu(self);
                break;
            }
            case "flashback_replays": {
                OptionalMenuIntegrations.openFlashbackReplays(self);
                break;
            }
            case "quit": {
                mc.method_1592();
                break;
            }
            case "bananasmp": {
                break;
            }
            case "settings": {
                mc.method_1507((class_437)new class_429(self, mc.field_1690));
                break;
            }
            case "box": {
                TitleScreenMixin.openResourcePacks(mc, self);
                break;
            }
            case "camera": {
                mc.method_1507((class_437)new ScreenshotGalleryScreen(self));
                break;
            }
            case "diamond": {
                mc.method_1507((class_437)new class_4325(self));
                break;
            }
            case "window": {
                mc.method_1507((class_437)new ClickGUIScreen());
                break;
            }
            case "joindiscord1": {
                class_407.method_60866((class_437)self, (String)"https://dsc.gg/tyxenclient", (boolean)true);
                break;
            }
            default: {
                class_370.method_27024((class_374)mc.method_1566(), (class_370.class_9037)class_370.class_9037.field_47588, (class_2561)class_2561.method_43470((String)"Tyxen"), (class_2561)class_2561.method_43470((String)"Coming Soon"));
            }
        }
        cir.setReturnValue(true);
    }

    private static void openResourcePacks(class_310 mc, class_437 parent) {
        mc.method_1507((class_437)new class_5375(mc.method_1520(), repository -> {
            mc.field_1690.method_49598(repository);
            mc.method_1507(parent);
        }, mc.method_1479(), (class_2561)class_2561.method_43471((String)"resourcePack.title")));
    }

    private static void connectToServer(class_437 parent, class_310 mc, String name, String ip) {
        class_639 address = class_639.method_2950((String)ip);
        class_642 data = new class_642(name, ip, class_642.class_8678.field_45611);
        class_412.method_36877((class_437)parent, (class_310)mc, (class_639)address, (class_642)data, (boolean)false, null);
    }
}

