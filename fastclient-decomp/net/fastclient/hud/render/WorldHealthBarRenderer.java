/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
 *  net.minecraft.class_10017
 *  net.minecraft.class_11658
 *  net.minecraft.class_12075
 *  net.minecraft.class_12249
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_327$class_6415
 *  net.minecraft.class_4587
 *  net.minecraft.class_4588
 *  net.minecraft.class_4597
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 */
package net.fastclient.hud.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fastclient.hud.accessor.EntityRenderStateAccessor;
import net.fastclient.hud.core.ModuleManager;
import net.fastclient.hud.modules.impl.render.DamageIndicator;
import net.minecraft.class_10017;
import net.minecraft.class_11658;
import net.minecraft.class_12075;
import net.minecraft.class_12249;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class WorldHealthBarRenderer {
    private static final float BAR_WIDTH = 0.8f;
    private static final float BAR_HEIGHT = 0.06f;
    private static final float BORDER_SIZE = 0.015f;
    private static final float HEIGHT_OFFSET = 0.3f;
    private static final float TEXT_SCALE = 0.006f;

    public static void onAfterEntities(WorldRenderContext context) {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) {
            return;
        }
        DamageIndicator module = mm.getModule(DamageIndicator.class);
        if (module == null || !module.isEnabled()) {
            return;
        }
        if (!module.isWorldMode()) {
            return;
        }
        class_4587 poseStack = context.matrices();
        class_4597 consumers = context.consumers();
        class_11658 worldState = context.worldState();
        if (poseStack == null || consumers == null || worldState == null) {
            return;
        }
        class_12075 cameraState = worldState.field_63082;
        class_243 cameraPos = cameraState.field_63078;
        class_310 mc = class_310.method_1551();
        class_327 font = mc.field_1772;
        for (class_10017 entityState : worldState.field_61735) {
            class_1309 living;
            EntityRenderStateAccessor accessor;
            class_1297 class_12972;
            if (!(entityState instanceof EntityRenderStateAccessor) || !((class_12972 = (accessor = (EntityRenderStateAccessor)entityState).fastclient$getEntity()) instanceof class_1309) || (living = (class_1309)class_12972) == mc.field_1724 || living.method_29504()) continue;
            float health = living.method_6032();
            float maxHealth = living.method_6063();
            if (maxHealth <= 0.0f) continue;
            double barY = entityState.field_53326 + (double)entityState.field_53330 + (double)0.3f;
            WorldHealthBarRenderer.renderHealthBar(poseStack, consumers, cameraState, cameraPos, font, entityState.field_53325, barY, entityState.field_53327, living, health, maxHealth, module);
        }
    }

    private static void renderHealthBar(class_4587 poseStack, class_4597 consumers, class_12075 cameraState, class_243 cameraPos, class_327 font, double worldX, double worldY, double worldZ, class_1309 entity, float health, float maxHealth, DamageIndicator module) {
        poseStack.method_22903();
        poseStack.method_22904(worldX - cameraPos.field_1352, worldY - cameraPos.field_1351, worldZ - cameraPos.field_1350);
        poseStack.method_22907((Quaternionfc)cameraState.field_63081);
        float halfWidth = 0.4f;
        float healthPercent = Math.max(0.0f, Math.min(1.0f, health / maxHealth));
        int healthColor = module.getHealthColor(healthPercent);
        int r = healthColor >> 16 & 0xFF;
        int g = healthColor >> 8 & 0xFF;
        int b = healthColor & 0xFF;
        Matrix4f matrix = poseStack.method_23760().method_23761();
        class_4588 buffer = consumers.method_73477(class_12249.method_76023());
        buffer.method_22918((Matrix4fc)matrix, -halfWidth - 0.015f, -0.015f, 0.0f).method_1336(0, 0, 0, 255);
        buffer.method_22918((Matrix4fc)matrix, halfWidth + 0.015f, -0.015f, 0.0f).method_1336(0, 0, 0, 255);
        buffer.method_22918((Matrix4fc)matrix, halfWidth + 0.015f, 0.074999996f, 0.0f).method_1336(0, 0, 0, 255);
        buffer.method_22918((Matrix4fc)matrix, -halfWidth - 0.015f, 0.074999996f, 0.0f).method_1336(0, 0, 0, 255);
        float zOffset = 0.001f;
        buffer.method_22918((Matrix4fc)matrix, -halfWidth, 0.0f, zOffset).method_1336(40, 40, 40, 255);
        buffer.method_22918((Matrix4fc)matrix, halfWidth, 0.0f, zOffset).method_1336(40, 40, 40, 255);
        buffer.method_22918((Matrix4fc)matrix, halfWidth, 0.06f, zOffset).method_1336(40, 40, 40, 255);
        buffer.method_22918((Matrix4fc)matrix, -halfWidth, 0.06f, zOffset).method_1336(40, 40, 40, 255);
        float filledWidth = 0.8f * healthPercent;
        float filledRight = -halfWidth + filledWidth;
        float zOffset2 = 0.002f;
        buffer.method_22918((Matrix4fc)matrix, -halfWidth, 0.0f, zOffset2).method_1336(r, g, b, 255);
        buffer.method_22918((Matrix4fc)matrix, filledRight, 0.0f, zOffset2).method_1336(r, g, b, 255);
        buffer.method_22918((Matrix4fc)matrix, filledRight, 0.06f, zOffset2).method_1336(r, g, b, 255);
        buffer.method_22918((Matrix4fc)matrix, -halfWidth, 0.06f, zOffset2).method_1336(r, g, b, 255);
        poseStack.method_22903();
        poseStack.method_46416(0.0f, 0.125f, 0.0f);
        poseStack.method_22905(0.006f, -0.006f, 0.006f);
        String name = entity.method_5476().getString();
        int nameWidth = font.method_1727(name);
        font.method_27522((class_2561)class_2561.method_43470((String)name), (float)(-nameWidth) / 2.0f, 0.0f, -1, false, poseStack.method_23760().method_23761(), consumers, class_327.class_6415.field_33993, 0, 0xF000F0);
        poseStack.method_22909();
        poseStack.method_22903();
        poseStack.method_46416(0.0f, -0.035f, 0.0f);
        poseStack.method_22905(0.006f, -0.006f, 0.006f);
        String healthText = WorldHealthBarRenderer.formatHealth(health, maxHealth);
        int textWidth = font.method_1727(healthText);
        int textColor = 0xFF000000 | healthColor;
        font.method_27522((class_2561)class_2561.method_43470((String)healthText), (float)(-textWidth) / 2.0f, 0.0f, textColor, false, poseStack.method_23760().method_23761(), consumers, class_327.class_6415.field_33993, 0, 0xF000F0);
        poseStack.method_22909();
        poseStack.method_22909();
    }

    private static String formatHealth(float health, float maxHealth) {
        if ((double)health == Math.floor(health) && (double)maxHealth == Math.floor(maxHealth)) {
            return String.format("%.0f / %.0f", Float.valueOf(health), Float.valueOf(maxHealth));
        }
        return String.format("%.1f / %.0f", Float.valueOf(health), Float.valueOf(maxHealth));
    }
}

