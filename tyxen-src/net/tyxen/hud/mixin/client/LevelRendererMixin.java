/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_11658
 *  net.minecraft.class_11659
 *  net.minecraft.class_243
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_761
 *  net.minecraft.class_9779
 *  net.minecraft.class_9922
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector4f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.tyxen.hud.mixin.client;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.render.MotionBlurShaderManager;
import net.minecraft.class_11658;
import net.minecraft.class_11659;
import net.minecraft.class_243;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_761;
import net.minecraft.class_9779;
import net.minecraft.class_9922;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_761.class})
public class LevelRendererMixin {
    @Unique
    private final Matrix4f prevModelView = new Matrix4f();
    @Unique
    private final Matrix4f prevProjection = new Matrix4f();
    @Unique
    private double prevCamX;
    @Unique
    private double prevCamY;
    @Unique
    private double prevCamZ;
    @Unique
    private boolean tyxen$appliedThisFrame = false;

    @Inject(method={"method_22710"}, at={@At(value="HEAD")})
    private void tyxen$onRenderHead(class_9922 resourceAllocator, class_9779 deltaTracker, boolean renderOutline, class_4184 camera, Matrix4f modelViewMatrix, Matrix4f projection, Matrix4f matrix4f3, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci) {
        this.tyxen$appliedThisFrame = false;
        MotionBlurShaderManager.captureAllocator(resourceAllocator);
        class_243 pos = camera.method_71156();
        double cx = pos.method_10216();
        double cy = pos.method_10214();
        double cz = pos.method_10215();
        float dx = (float)(cx - this.prevCamX);
        float dy = (float)(cy - this.prevCamY);
        float dz = (float)(cz - this.prevCamZ);
        Matrix4f modelView = new Matrix4f((Matrix4fc)modelViewMatrix);
        Matrix4f proj = new Matrix4f((Matrix4fc)projection);
        MotionBlurShaderManager.setFrameMotionBlur(modelView, this.prevModelView, proj, this.prevProjection, dx, dy, dz);
        this.prevModelView.set((Matrix4fc)modelView);
        this.prevProjection.set((Matrix4fc)proj);
        this.prevCamX = cx;
        this.prevCamY = cy;
        this.prevCamZ = cz;
    }

    @Inject(method={"method_72916"}, at={@At(value="HEAD")})
    private void tyxen$beforeSubmitEntities(class_4587 poseStack, class_11658 levelRenderState, class_11659 output, CallbackInfo ci) {
        if (!this.tyxen$appliedThisFrame && MotionBlurShaderManager.shouldExcludeEntities()) {
            this.tyxen$appliedThisFrame = true;
            MotionBlurShaderManager.applyMotionBlur();
        }
    }

    @Inject(method={"method_22710"}, at={@At(value="TAIL")})
    private void tyxen$onRenderLevelTail(class_9922 resourceAllocator, class_9779 deltaTracker, boolean renderOutline, class_4184 camera, Matrix4f modelViewMatrix, Matrix4f projection, Matrix4f matrix4f3, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci) {
        if (!this.tyxen$appliedThisFrame) {
            this.tyxen$appliedThisFrame = true;
            MotionBlurShaderManager.applyMotionBlur();
        }
    }
}

