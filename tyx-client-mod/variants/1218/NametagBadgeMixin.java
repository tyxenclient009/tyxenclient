package gg.tyx.client.mixin;

import gg.tyx.client.TyxClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * VARIANT 1218 — old dispatcher pipeline (MC 1.21.1–1.21.8).
 * Lunar-style cosmetics: Tyx badge left of every other player's nameplate
 * (camera-facing, full-bright, culled like the vanilla label) + swinging
 * T-emblem cape behind them (body-oriented).
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class NametagBadgeMixin {
    private static final Identifier BADGE = Identifier.of("tyxclient", "textures/gui/badge.png");
    private static final Identifier CAPE = Identifier.of("tyxclient", "textures/gui/cape.png");
    private static final int FULL_BRIGHT = 0xF000F0;

    @Shadow @Final public Camera camera;

    @Inject(method = "render", at = @At("TAIL"))
    private void tyx$cosmetics(Entity entity, double x, double y, double z, float yaw, float tickDelta,
            MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !(entity instanceof PlayerEntity player)) return;
        if (player.isInvisible()) return;
        // Own body in F5: show badge + cape in third-person (with the
        // SelfNametagMixin the vanilla label above it shows too). First
        // person never reaches here — the self entity isn't rendered there.
        if (player == client.player) {
            if (!TyxClient.CONFIG.selfNametag) return;
            if (client.gameRenderer == null || client.gameRenderer.getCamera() == null
                    || !client.gameRenderer.getCamera().isThirdPerson()) return;
        }
        Vec3d cam = this.camera.getPos();
        double dx = cam.x - x, dy = cam.y - y, dz = cam.z - z;
        if (dx * dx + dy * dy + dz * dz > 64.0 * 64.0) return;

        if (TyxClient.CONFIG.badge) {
            float nameW = client.textRenderer.getWidth(player.getDisplayName());
            float bx = -nameW / 2.0f - 10.0f;
            matrices.push();
            matrices.translate(x, y + entity.getHeight() + 0.5f, z);
            matrices.multiply(this.camera.getRotation());
            matrices.scale(-0.025f, -0.025f, 0.025f);
            Matrix4f m = matrices.peek().getPositionMatrix();
            VertexConsumer buf = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(BADGE));
            quad(m, buf, FULL_BRIGHT, bx, -4.0f, 0.0f, bx + 8.0f, 4.0f, 0.0f,
                    0.0f, 0.0f, 1.0f, 1.0f);
            matrices.pop();
        }

        if (TyxClient.CONFIG.cape) {
            float swing = player.isSneaking() ? 0.30f
                    : player.isSprinting() ? 0.22f
                    : 0.10f + (float) Math.sin(player.age * 0.08f) * 0.02f;
            float top = player.isSneaking() ? 1.28f : 1.40f;
            matrices.push();
            matrices.translate(x, y, z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - player.getBodyYaw()));
            Matrix4f m = matrices.peek().getPositionMatrix();
            VertexConsumer buf = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(CAPE));
            quad(m, buf, light, -0.30f, top, -0.13f, 0.30f, 0.95f, -0.16f,
                    0.0f, 0.0f, 1.0f, 0.55f);
            quad(m, buf, light, -0.30f, 0.95f, -0.16f, 0.30f, 0.50f, -0.16f - swing,
                    0.0f, 0.55f, 1.0f, 1.0f);
            matrices.pop();
        }
    }

    private static void quad(Matrix4f m, VertexConsumer buf, int light,
            float x0, float y0, float z0, float x1, float y1, float z1,
            float u0, float v0, float u1, float v1) {
        vert(m, buf, light, x0, y0, z0, u0, v0);
        vert(m, buf, light, x0, y1, z1, u0, v1);
        vert(m, buf, light, x1, y1, z1, u1, v1);
        vert(m, buf, light, x1, y0, z0, u1, v0);
    }

    private static void vert(Matrix4f m, VertexConsumer buf, int light,
            float x, float y, float z, float u, float v) {
        buf.vertex(m, x, y, z).color(255, 255, 255, 255).texture(u, v)
                .overlay(0, 10).light(light).normal(0.0f, 0.0f, -1.0f);
    }
}
