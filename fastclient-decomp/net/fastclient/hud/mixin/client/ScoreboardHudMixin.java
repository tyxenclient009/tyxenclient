/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_329
 *  net.minecraft.class_332
 *  net.minecraft.class_9779
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.fastclient.hud.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fastclient.hud.core.ModuleManager;
import net.fastclient.hud.modules.impl.render.ScoreboardMod;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_329.class})
public class ScoreboardHudMixin {
    @Inject(method={"method_55803"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderScoreboardSidebar(class_332 graphics, class_9779 deltaTracker, CallbackInfo ci) {
        ScoreboardMod scoreboard;
        ModuleManager mm = ModuleManager.getInstance();
        if (mm != null && (scoreboard = mm.getModule(ScoreboardMod.class)) != null && scoreboard.isEnabled() && scoreboard.shouldHideScoreboard()) {
            ci.cancel();
        }
    }
}

