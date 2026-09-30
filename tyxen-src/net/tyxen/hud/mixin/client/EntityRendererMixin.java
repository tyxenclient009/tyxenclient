/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10017
 *  net.minecraft.class_11719
 *  net.minecraft.class_11719$class_11721
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_2561
 *  net.minecraft.class_2583
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_5250
 *  net.minecraft.class_897
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.tyxen.hud.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.accessor.EntityRenderStateAccessor;
import net.tyxen.hud.network.TyxenUserCache;
import net.minecraft.class_10017;
import net.minecraft.class_11719;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_5250;
import net.minecraft.class_897;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_897.class})
public class EntityRendererMixin<T extends class_1297, S extends class_10017> {
    private static final String FC_ICON_CHAR = "\ue000";
    private static final class_2583 FC_ICON_STYLE = class_2583.field_24360.method_27704((class_11719)new class_11719.class_11721(class_2960.method_60655((String)"tyxen", (String)"icon")));

    @Inject(method={"method_62354"}, at={@At(value="TAIL")})
    private void onExtractRenderState(T entity, S state, float partialTicks, CallbackInfo ci) {
        boolean isSelf;
        if (state instanceof EntityRenderStateAccessor) {
            EntityRenderStateAccessor accessor = (EntityRenderStateAccessor)state;
            accessor.tyxen$setEntity((class_1297)entity);
        }
        if (!(entity instanceof class_1657)) {
            return;
        }
        class_1657 player = (class_1657)entity;
        boolean bl = isSelf = entity == class_310.method_1551().field_1724;
        if (!isSelf && !TyxenUserCache.getInstance().isTyxenUser(player.method_7334().name())) {
            return;
        }
        if (((class_10017)state).field_53337 == null) {
            return;
        }
        class_5250 icon = class_2561.method_43470((String)FC_ICON_CHAR).method_27696(FC_ICON_STYLE);
        ((class_10017)state).field_53337 = class_2561.method_43470((String)"").method_10852((class_2561)icon).method_27693(" ").method_10852(((class_10017)state).field_53337);
    }
}

