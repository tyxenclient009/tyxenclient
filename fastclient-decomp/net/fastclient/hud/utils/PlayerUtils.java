/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1297
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_746
 */
package net.fastclient.hud.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_746;

@Environment(value=EnvType.CLIENT)
public class PlayerUtils {
    private static final class_310 mc = class_310.method_1551();

    public static class_746 getPlayer() {
        return PlayerUtils.mc.field_1724;
    }

    public static boolean isInGame() {
        return PlayerUtils.mc.field_1724 != null && PlayerUtils.mc.field_1687 != null;
    }

    public static class_243 getPosition() {
        if (PlayerUtils.mc.field_1724 == null) {
            return class_243.field_1353;
        }
        return PlayerUtils.mc.field_1724.method_73189();
    }

    public static double getX() {
        if (PlayerUtils.mc.field_1724 == null) {
            return 0.0;
        }
        return PlayerUtils.mc.field_1724.method_23317();
    }

    public static double getY() {
        if (PlayerUtils.mc.field_1724 == null) {
            return 0.0;
        }
        return PlayerUtils.mc.field_1724.method_23318();
    }

    public static double getZ() {
        if (PlayerUtils.mc.field_1724 == null) {
            return 0.0;
        }
        return PlayerUtils.mc.field_1724.method_23321();
    }

    public static float getYaw() {
        if (PlayerUtils.mc.field_1724 == null) {
            return 0.0f;
        }
        return PlayerUtils.mc.field_1724.method_36454();
    }

    public static float getPitch() {
        if (PlayerUtils.mc.field_1724 == null) {
            return 0.0f;
        }
        return PlayerUtils.mc.field_1724.method_36455();
    }

    public static String getFacingDirection() {
        if (PlayerUtils.mc.field_1724 == null) {
            return "N/A";
        }
        float yaw = PlayerUtils.mc.field_1724.method_36454();
        if ((yaw = (yaw % 360.0f + 360.0f) % 360.0f) >= 315.0f || yaw < 45.0f) {
            return "South";
        }
        if (yaw >= 45.0f && yaw < 135.0f) {
            return "West";
        }
        if (yaw >= 135.0f && yaw < 225.0f) {
            return "North";
        }
        if (yaw >= 225.0f && yaw < 315.0f) {
            return "East";
        }
        return "N/A";
    }

    public static double getSpeed() {
        if (PlayerUtils.mc.field_1724 == null) {
            return 0.0;
        }
        class_243 velocity = PlayerUtils.mc.field_1724.method_18798();
        return Math.sqrt(velocity.field_1352 * velocity.field_1352 + velocity.field_1350 * velocity.field_1350) * 20.0;
    }

    public static double distanceTo(class_1297 entity) {
        if (PlayerUtils.mc.field_1724 == null || entity == null) {
            return 0.0;
        }
        return PlayerUtils.mc.field_1724.method_5739(entity);
    }
}

