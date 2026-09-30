/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10017
 *  net.minecraft.class_10055
 *  net.minecraft.class_11659
 *  net.minecraft.class_12075
 *  net.minecraft.class_12249
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_4587
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_4588
 *  net.minecraft.class_5348
 *  net.minecraft.class_898
 *  org.joml.Quaternionfc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.swiftclient.mixin;

import com.swiftclient.modules.ModuleManager;
import com.swiftclient.render.BadgeModule;
import net.minecraft.class_10017;
import net.minecraft.class_10055;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_12249;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_5348;
import net.minecraft.class_898;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_898.class})
public abstract class NametagBadgeMixin {
    private static final class_2960 BADGE = class_2960.method_60655((String)"swiftclient", (String)"textures/gui/badge.png");

    @Inject(method={"method_72976(Lnet/minecraft/class_10017;Lnet/minecraft/class_12075;DDDLnet/minecraft/class_4587;Lnet/minecraft/class_11659;)V"}, at={@At(value="TAIL")}, require=0)
    private void swift$badge(class_10017 class_100172, class_12075 class_120752, double d, double d2, double d3, class_4587 class_45872, class_11659 class_116592, CallbackInfo callbackInfo) {
        try {
            if (!(class_100172 instanceof class_10055)) {
                return;
            }
            class_10055 class_100552 = (class_10055)class_100172;
            if (class_100552.field_53333 || class_100552.field_53542 || class_100552.field_53337 == null) {
                return;
            }
            if (class_100552.field_53332 > 4096.0) {
                return;
            }
            BadgeModule badgeModule = ModuleManager.get(BadgeModule.class);
            if (badgeModule == null || !badgeModule.enabled()) {
                return;
            }
            class_310 class_3102 = class_310.method_1551();
            if (class_3102 == null || class_3102.field_1724 == null) {
                return;
            }
            class_327 class_3272 = class_3102.field_1772;
            float f = class_3272.method_27525((class_5348)class_100552.field_53337);
            float f2 = -f / 2.0f - 10.0f;
            class_45872.method_22903();
            class_45872.method_22904(d, d2 + (double)class_100552.field_53330 + 0.5, d3);
            class_45872.method_22907((Quaternionfc)class_120752.field_63081);
            class_45872.method_46416(-0.025f, -0.025f, 0.025f);
            class_116592.method_73483(class_45872, class_12249.method_75990((class_2960)BADGE), (class_46652, class_45882) -> NametagBadgeMixin.quad(class_46652, class_45882, f2, -4.0f, 0.0f, f2 + 8.0f, 4.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f));
            class_45872.method_22909();
        }
        catch (Throwable throwable) {
            ModuleManager.fault(ModuleManager.get(BadgeModule.class), throwable, "badge");
        }
    }

    private static void quad(class_4587.class_4665 class_46652, class_4588 class_45882, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10) {
        NametagBadgeMixin.vert(class_46652, class_45882, f, f2, f3, f7, f8);
        NametagBadgeMixin.vert(class_46652, class_45882, f, f5, f6, f7, f10);
        NametagBadgeMixin.vert(class_46652, class_45882, f4, f5, f6, f9, f10);
        NametagBadgeMixin.vert(class_46652, class_45882, f4, f2, f3, f9, f8);
    }

    private static void vert(class_4587.class_4665 class_46652, class_4588 class_45882, float f, float f2, float f3, float f4, float f5) {
        class_45882.method_56824(class_46652, f, f2, f3).method_1336(255, 255, 255, 255).method_22913(f4, f5).method_60796(0, 10).method_60803(0xF000F0).method_60831(class_46652, 0.0f, 0.0f, -1.0f);
    }
}

