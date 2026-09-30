package gg.tyx.client.mixin;

import gg.tyx.client.TyxClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lunar-style player cosmetics on the modern (1.21.9+) command-queue
 * pipeline: the Tyx badge left of every player's nameplate (camera-facing,
 * full-bright, distance-culled) and the swinging T-emblem cape behind
 * them (body-oriented). Submitted as custom commands so they ride the
 * vanilla frame — no pipeline hacks.
 */
@Mixin(EntityRenderManager.class)
public abstract class ManagerCosmeticsMixin {
    private static final Identifier BADGE = Identifier.of("tyxclient", "textures/gui/badge.png");
    private static final Identifier CAPE = Identifier.of("tyxclient", "textures/gui/cape.png");

    @Inject(method = "render", at = @At("TAIL"))
    private void tyx$cosmetics(EntityRenderState state, CameraRenderState camState,
            double x, double y, double z, MatrixStack matrices,
            OrderedRenderCommandQueue queue, CallbackInfo ci) {
        if (!(state instanceof PlayerEntityRenderState p)) return;
        if (p.invisible || p.spectator || p.displayName == null) return;
        if (p.squaredDistanceToCamera > 64.0 * 64.0) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        if (TyxClient.CONFIG.badge) {
            float w = client.textRenderer.getWidth(p.displayName);
            matrices.push();
            matrices.translate(x, y + p.height + 0.5, z);
            matrices.multiply(camState.orientation);
            matrices.scale(-0.025f, -0.025f, 0.025f);
            Matrix4f m = new Matrix4f(matrices.peek().getPositionMatrix());
            float bx = -w / 2.0f - 10.0f; // 8px badge + 2px gap, left of the name
            queue.submitCustom(matrices, RenderLayers.entityCutout(BADGE),
                    (entry, buf) -> quad(m, buf, bx, -4.0f, 0.0f, bx + 8.0f, 4.0f, 0.0f,
                            0.0f, 0.0f, 1.0f, 1.0f));
            matrices.pop();
        }

        if (TyxClient.CONFIG.cape && state instanceof LivingEntityRenderState living) {
            float swing = p.sneaking ? 0.30f
                    : 0.10f + (float) Math.sin(p.age * 0.08f) * 0.02f;
            float top = p.sneaking ? 1.28f : 1.40f;
            matrices.push();
            matrices.translate(x, y, z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - living.bodyYaw));
            Matrix4f m = new Matrix4f(matrices.peek().getPositionMatrix());
            queue.submitCustom(matrices, RenderLayers.entityCutoutNoCull(CAPE), (entry, buf) -> {
                quad(m, buf, -0.30f, top, -0.13f, 0.30f, 0.95f, -0.16f,
                        0.0f, 0.0f, 1.0f, 0.55f);
                quad(m, buf, -0.30f, 0.95f, -0.16f, 0.30f, 0.50f, -0.16f - swing,
                        0.0f, 0.55f, 1.0f, 1.0f);
            });
            matrices.pop();
        }
    }

    private static void quad(Matrix4f m, VertexConsumer buf,
            float x0, float y0, float z0, float x1, float y1, float z1,
            float u0, float v0, float u1, float v1) {
        v(m, buf, x0, y0, z0, u0, v0);
        v(m, buf, x0, y1, z1, u0, v1);
        v(m, buf, x1, y1, z1, u1, v1);
        v(m, buf, x1, y0, z0, u1, v0);
    }

    private static void v(Matrix4f m, VertexConsumer buf,
            float x, float y, float z, float u, float v) {
        Vector4f p = new Vector4f(x, y, z, 1.0f).mul(m);
        buf.vertex(p.x(), p.y(), p.z()).color(255, 255, 255, 255).texture(u, v)
                .overlay(0, 10).light(15, 15).normal(0.0f, 0.0f, -1.0f);
    }
}
