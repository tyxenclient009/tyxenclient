/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_11719
 *  net.minecraft.class_11719$class_11721
 *  net.minecraft.class_2561
 *  net.minecraft.class_2583
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_355
 *  net.minecraft.class_5250
 *  net.minecraft.class_640
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.tyxen.hud.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.core.ModuleManager;

import net.tyxen.hud.modules.impl.render.PingOverlay;
import net.tyxen.hud.network.TyxenUserCache;
import net.minecraft.class_11719;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_355;
import net.minecraft.class_5250;
import net.minecraft.class_640;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_355.class})
public class PlayerTabOverlayMixin {
    private static final String FC_ICON_CHAR = "\ue000";
    private static final class_2583 FC_ICON_STYLE = class_2583.field_24360.method_27704((class_11719)new class_11719.class_11721(class_2960.method_60655((String)"tyxen", (String)"icon")));

    @Inject(method={"method_1923"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderPingIcon(class_332 graphics, int width, int x, int y, class_640 playerInfo, CallbackInfo ci) {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) {
            return;
        }
        PingOverlay pingOverlay = mm.getModule(PingOverlay.class);
        if (pingOverlay == null || !pingOverlay.isEnabled()) {
            return;
        }
        ci.cancel();
        int latency = playerInfo.method_2959();
        String text = pingOverlay.formatPing(latency);
        int color = pingOverlay.getPingColor(latency);
        class_310 mc = class_310.method_1551();
        int textWidth = mc.field_1772.method_1727(text);
        graphics.method_51433(mc.field_1772, text, x + width - textWidth - 1, y, color, true);
    }

    @Inject(method={"method_1918"}, at={@At(value="RETURN")}, cancellable=true)
    private void onGetNameForDisplay(class_640 playerInfo, CallbackInfoReturnable<class_2561> cir) {
        class_2561 original = cir.getReturnValue();
        boolean isSelf;
        String name = playerInfo.method_2966().name();
        class_310 mc = class_310.method_1551();
        boolean bl = isSelf = mc.field_1724 != null && mc.field_1724.method_7334().name().equals(name);
        if (!isSelf && !TyxenUserCache.getInstance().isTyxenUser(name)) {
            return;
        }
        class_5250 icon = class_2561.method_43470((String)FC_ICON_CHAR).method_27696(FC_ICON_STYLE);
        class_5250 prefix = class_2561.method_43470((String)"").method_10852((class_2561)icon).method_10852((class_2561)class_2561.method_43470((String)" "));
        cir.setReturnValue(prefix.method_10852(original));
    }
}

