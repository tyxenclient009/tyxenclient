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
import net.minecraft.class_898;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * TYX addition — nametag badge on the modern (1.21.10+) command-queue
 * pipeline. Draws the Tyx emblem left of every other player's nameplate
 * (camera-facing, full-bright, distance-culled), submitted as a custom
 * command so it rides the vanilla frame. Follows the exact pattern
 * vanilla renderers use (entry-aware vertex writes, balanced push/pop).
 */
@Mixin(value = {class_898.class})
public abstract class NametagBadgeMixin {
    private static final class_2960 BADGE = class_2960.method_60655("swiftclient", "textures/gui/badge.png");

    @Inject(method = {
        "method_72976(Lnet/minecraft/class_10017;Lnet/minecraft/class_12075;DDDLnet/minecraft/class_4587;Lnet/minecraft/class_11659;)V"}, at = {
            @At(value = "TAIL")}, require = 0)
    private void swift$badge(class_10017 state, class_12075 cam, double x, double y, double z,
            class_4587 matrices, class_11659 queue, CallbackInfo ci) {
        try {
            if (!(state instanceof class_10055)) {
                return;
            }
            class_10055 p = (class_10055)state;
            if (p.field_53333 || p.field_53542 || p.field_53337 == null) {
                return;
            }
            if (p.field_53332 > 64.0 * 64.0) {
                return;
            }
            BadgeModule mod = ModuleManager.get(BadgeModule.class);
            if (mod == null || !mod.enabled()) {
                return;
            }
            class_310 client = class_310.method_1551();
            if (client == null || client.field_1724 == null) {
                return;
            }
            class_327 tr = client.field_1772;
            float w = tr.method_27525(p.field_53337);
            float bx = -w / 2.0f - 10.0f;
            matrices.method_22903();
            matrices.method_22904(x, y + (double)p.field_53330 + 0.5, z);
            matrices.method_22907(cam.field_63081);
            matrices.method_46416(-0.025f, -0.025f, 0.025f);
            queue.method_73483(matrices, class_12249.method_75990(BADGE), (entry, buf) -> {
                NametagBadgeMixin.quad(entry, buf, bx, -4.0f, 0.0f, bx + 8.0f, 4.0f, 0.0f,
                        0.0f, 0.0f, 1.0f, 1.0f);
            });
            matrices.method_22909();
        } catch (Throwable t) {
            ModuleManager.fault(ModuleManager.get(BadgeModule.class), t, "badge");
        }
    }

    private static void quad(net.minecraft.class_4587.class_4665 entry, class_4588 buf,
            float x0, float y0, float z0, float x1, float y1, float z1,
            float u0, float v0, float u1, float v1) {
        NametagBadgeMixin.vert(entry, buf, x0, y0, z0, u0, v0);
        NametagBadgeMixin.vert(entry, buf, x0, y1, z1, u0, v1);
        NametagBadgeMixin.vert(entry, buf, x1, y1, z1, u1, v1);
        NametagBadgeMixin.vert(entry, buf, x1, y0, z0, u1, v0);
    }

    private static void vert(net.minecraft.class_4587.class_4665 entry, class_4588 buf,
            float x, float y, float z, float u, float v) {
        buf.method_56824(entry, x, y, z).method_1336(255, 255, 255, 255).method_22913(u, v)
                .method_60796(0, 10).method_60803(15728880).method_60831(entry, 0.0f, 0.0f, -1.0f);
    }
}
