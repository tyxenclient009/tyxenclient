/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
 *  net.minecraft.class_11658
 *  net.minecraft.class_12075
 *  net.minecraft.class_12249
 *  net.minecraft.class_2338
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_3965
 *  net.minecraft.class_4587
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_4588
 *  net.minecraft.class_4597
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package net.fastclient.hud.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fastclient.hud.core.ModuleManager;
import net.fastclient.hud.modules.impl.render.BlockOverlayModule;
import net.minecraft.class_11658;
import net.minecraft.class_12075;
import net.minecraft.class_12249;
import net.minecraft.class_2338;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3965;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

@Environment(value=EnvType.CLIENT)
public class BlockOverlayRenderer {
    public static void onAfterEntities(WorldRenderContext context) {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) {
            return;
        }
        BlockOverlayModule module = mm.getModule(BlockOverlayModule.class);
        if (module == null || !module.isEnabled()) {
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
        BlockOverlayRenderer.renderBlockIndicator(poseStack, consumers, cameraPos, module);
    }

    private static void renderBlockIndicator(class_4587 poseStack, class_4597 consumers, class_243 cameraPos, BlockOverlayModule module) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1765 == null || mc.field_1765.method_17783() != class_239.class_240.field_1332) {
            return;
        }
        class_3965 blockHit = (class_3965)mc.field_1765;
        class_2338 blockPos = blockHit.method_17777();
        float expand = (float)module.getExpandAmount();
        float minX = (float)((double)blockPos.method_10263() - cameraPos.field_1352) - expand;
        float minY = (float)((double)blockPos.method_10264() - cameraPos.field_1351) - expand;
        float minZ = (float)((double)blockPos.method_10260() - cameraPos.field_1350) - expand;
        float maxX = minX + 1.0f + expand * 2.0f;
        float maxY = minY + 1.0f + expand * 2.0f;
        float maxZ = minZ + 1.0f + expand * 2.0f;
        class_4587.class_4665 pose = poseStack.method_23760();
        if (module.shouldShowFill()) {
            int fillColor = module.getOverlayColorARGB();
            BlockOverlayRenderer.renderFilledBox(consumers, pose, minX, minY, minZ, maxX, maxY, maxZ, fillColor);
        }
        if (module.shouldShowOutline()) {
            int outlineColor = module.getOutlineColorARGB();
            float lineWidth = module.getOutlineWidth();
            BlockOverlayRenderer.renderBoxOutline(consumers, pose, minX, minY, minZ, maxX, maxY, maxZ, outlineColor, lineWidth);
        }
    }

    private static void renderFilledBox(class_4597 consumers, class_4587.class_4665 pose, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int color) {
        class_4588 buffer = consumers.method_73477(class_12249.method_76019());
        Matrix4f matrix = pose.method_23761();
        buffer.method_22918((Matrix4fc)matrix, minX, minY, minZ).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, maxX, minY, minZ).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, maxX, minY, maxZ).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, minX, minY, maxZ).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, minX, maxY, maxZ).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, maxX, maxY, maxZ).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, maxX, maxY, minZ).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, minX, maxY, minZ).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, minX, minY, minZ).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, minX, maxY, minZ).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, maxX, maxY, minZ).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, maxX, minY, minZ).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, maxX, minY, maxZ).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, maxX, maxY, maxZ).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, minX, maxY, maxZ).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, minX, minY, maxZ).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, minX, minY, maxZ).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, minX, maxY, maxZ).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, minX, maxY, minZ).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, minX, minY, minZ).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, maxX, minY, minZ).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, maxX, maxY, minZ).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, maxX, maxY, maxZ).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, maxX, minY, maxZ).method_39415(color);
    }

    private static void renderBoxOutline(class_4597 consumers, class_4587.class_4665 pose, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int color, float lineWidth) {
        class_4588 buffer = consumers.method_73477(class_12249.method_76015());
        BlockOverlayRenderer.renderLine(buffer, pose, minX, minY, minZ, maxX, minY, minZ, color, lineWidth);
        BlockOverlayRenderer.renderLine(buffer, pose, maxX, minY, minZ, maxX, minY, maxZ, color, lineWidth);
        BlockOverlayRenderer.renderLine(buffer, pose, maxX, minY, maxZ, minX, minY, maxZ, color, lineWidth);
        BlockOverlayRenderer.renderLine(buffer, pose, minX, minY, maxZ, minX, minY, minZ, color, lineWidth);
        BlockOverlayRenderer.renderLine(buffer, pose, minX, maxY, minZ, maxX, maxY, minZ, color, lineWidth);
        BlockOverlayRenderer.renderLine(buffer, pose, maxX, maxY, minZ, maxX, maxY, maxZ, color, lineWidth);
        BlockOverlayRenderer.renderLine(buffer, pose, maxX, maxY, maxZ, minX, maxY, maxZ, color, lineWidth);
        BlockOverlayRenderer.renderLine(buffer, pose, minX, maxY, maxZ, minX, maxY, minZ, color, lineWidth);
        BlockOverlayRenderer.renderLine(buffer, pose, minX, minY, minZ, minX, maxY, minZ, color, lineWidth);
        BlockOverlayRenderer.renderLine(buffer, pose, maxX, minY, minZ, maxX, maxY, minZ, color, lineWidth);
        BlockOverlayRenderer.renderLine(buffer, pose, maxX, minY, maxZ, maxX, maxY, maxZ, color, lineWidth);
        BlockOverlayRenderer.renderLine(buffer, pose, minX, minY, maxZ, minX, maxY, maxZ, color, lineWidth);
    }

    private static void renderLine(class_4588 buffer, class_4587.class_4665 pose, float x1, float y1, float z1, float x2, float y2, float z2, int color, float lineWidth) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float len = class_3532.method_15355((float)(dx * dx + dy * dy + dz * dz));
        if (len < 1.0E-4f) {
            return;
        }
        buffer.method_56824(pose, x1, y1, z1).method_39415(color).method_60831(pose, dx /= len, dy /= len, dz /= len).method_75298(lineWidth);
        buffer.method_56824(pose, x2, y2, z2).method_39415(color).method_60831(pose, dx, dy, dz).method_75298(lineWidth);
    }
}

