/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_329
 *  net.minecraft.class_332
 *  net.minecraft.class_337
 *  net.minecraft.class_9779
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.swiftclient.mixin;

import com.swiftclient.mixin.BossBarHudAccessor;
import com.swiftclient.modules.ModuleManager;
import com.swiftclient.render.CrosshairModule;
import com.swiftclient.restyle.ActionBarModule;
import com.swiftclient.restyle.BossbarModule;
import com.swiftclient.restyle.ScoreboardModule;
import net.minecraft.class_2561;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_337;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_329.class})
public abstract class HudMixin {
    @Shadow
    private class_2561 field_2018;
    @Shadow
    private int field_2041;
    @Shadow
    private class_337 field_2030;

    @Inject(method={"method_55800"}, at={@At(value="HEAD")}, cancellable=true)
    private void swift$actionBar(class_332 ctx, class_9779 tickCounter, CallbackInfo ci) {
        ActionBarModule m = ModuleManager.get(ActionBarModule.class);
        if (m != null && m.enabled() && this.field_2018 != null && this.field_2041 > 0) {
            ci.cancel();
            ActionBarModule.render(ctx, this.field_2018, this.field_2041);
        }
    }

    @Inject(method={"method_55803"}, at={@At(value="HEAD")}, cancellable=true)
    private void swift$scoreboard(class_332 ctx, class_9779 tickCounter, CallbackInfo ci) {
        ScoreboardModule m = ModuleManager.get(ScoreboardModule.class);
        if (m != null && m.enabled()) {
            ci.cancel();
            ScoreboardModule.render(ctx);
        }
    }

    @Inject(method={"method_1736"}, at={@At(value="HEAD")}, cancellable=true)
    private void swift$crosshair(class_332 ctx, class_9779 tickCounter, CallbackInfo ci) {
        CrosshairModule m = ModuleManager.get(CrosshairModule.class);
        if (m != null && m.enabled()) {
            ci.cancel();
            CrosshairModule.render(ctx);
        }
    }

    @Inject(method={"method_70837"}, at={@At(value="HEAD")}, cancellable=true)
    private void swift$bossbar(class_332 ctx, class_9779 tickCounter, CallbackInfo ci) {
        BossbarModule m = ModuleManager.get(BossbarModule.class);
        if (m != null && m.enabled()) {
            ci.cancel();
            BossbarModule.render(ctx, ((BossBarHudAccessor)this.field_2030).swift$bossBars());
        }
    }
}

