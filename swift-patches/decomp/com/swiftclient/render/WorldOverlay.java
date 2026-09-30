/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
 *  net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
 *  net.minecraft.class_1297
 *  net.minecraft.class_1921
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_265
 *  net.minecraft.class_310
 *  net.minecraft.class_4587
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_4588
 *  net.minecraft.class_4597
 */
package com.swiftclient.render;

import com.swiftclient.config.ClientConfig;
import com.swiftclient.config.Theme;
import com.swiftclient.modules.ModuleManager;
import com.swiftclient.render.BlockOutlineModule;
import com.swiftclient.render.ChunkBordersModule;
import com.swiftclient.render.HitboxesModule;
import java.lang.reflect.Method;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.class_1297;
import net.minecraft.class_1921;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;

public final class WorldOverlay {
    private static class_1921 linesLayer;
    private static boolean linesResolved;
    private static Method lineWidthMethod;
    private static boolean lineWidthResolved;

    private WorldOverlay() {
    }

    public static void register() {
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((ctx, state) -> {
            try {
                BlockOutlineModule m = ModuleManager.get(BlockOutlineModule.class);
                if (m == null || !m.enabled() || state == null) {
                    return true;
                }
                class_265 shape = state.comp_4935();
                if (shape == null || shape.method_1110()) {
                    return true;
                }
                int color = Theme.accent();
                for (class_238 b : shape.method_1090()) {
                    WorldOverlay.drawBox(ctx, b.method_996(state.comp_4932()).method_1014(0.002), color);
                }
                return false;
            }
            catch (Throwable t) {
                return true;
            }
        });
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(ctx -> {
            try {
                HitboxesModule hb;
                ChunkBordersModule cb = ModuleManager.get(ChunkBordersModule.class);
                if (cb != null && cb.enabled()) {
                    WorldOverlay.drawChunks(ctx, cb);
                }
                if ((hb = ModuleManager.get(HitboxesModule.class)) != null && hb.enabled()) {
                    WorldOverlay.drawHitboxes(ctx);
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        });
    }

    private static void drawChunks(WorldRenderContext ctx, ChunkBordersModule m) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) {
            return;
        }
        int r = ClientConfig.getInt(m.name(), "radius", 2);
        r = Math.max(1, Math.min(4, r));
        int pcx = mc.field_1724.method_31477() >> 4;
        int pcz = mc.field_1724.method_31479() >> 4;
        double y0 = mc.field_1724.method_23318() - 16.0;
        double y1 = mc.field_1724.method_23318() + 16.0;
        int color = 0x96000000 | Theme.accent() & 0xFFFFFF;
        for (int cx = pcx - r; cx <= pcx + r; ++cx) {
            for (int cz = pcz - r; cz <= pcz + r; ++cz) {
                double x0 = (double)cx * 16.0;
                double x1 = x0 + 16.0;
                double z0 = (double)cz * 16.0;
                double z1 = z0 + 16.0;
                WorldOverlay.line(ctx, x0, y0, z0, x0, y1, z0, color);
                WorldOverlay.line(ctx, x1, y0, z0, x1, y1, z0, color);
                WorldOverlay.line(ctx, x0, y0, z1, x0, y1, z1, color);
                WorldOverlay.line(ctx, x1, y0, z1, x1, y1, z1, color);
            }
        }
    }

    private static void drawHitboxes(WorldRenderContext ctx) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1724 == null) {
            return;
        }
        class_243 pp = new class_243(mc.field_1724.method_23317(), mc.field_1724.method_23318(), mc.field_1724.method_23321());
        int color = Theme.accent();
        for (class_1297 e : mc.field_1687.method_18112()) {
            if (e == mc.field_1724 || e.method_7325() || e.method_5649(pp.field_1352, pp.field_1351, pp.field_1350) > 2304.0) continue;
            WorldOverlay.drawBox(ctx, e.method_5829().method_1014(0.002), color);
        }
    }

    public static void drawBox(WorldRenderContext ctx, class_238 box, int color) {
        WorldOverlay.line(ctx, box.field_1323, box.field_1322, box.field_1321, box.field_1320, box.field_1322, box.field_1321, color);
        WorldOverlay.line(ctx, box.field_1320, box.field_1322, box.field_1321, box.field_1320, box.field_1322, box.field_1324, color);
        WorldOverlay.line(ctx, box.field_1320, box.field_1322, box.field_1324, box.field_1323, box.field_1322, box.field_1324, color);
        WorldOverlay.line(ctx, box.field_1323, box.field_1322, box.field_1324, box.field_1323, box.field_1322, box.field_1321, color);
        WorldOverlay.line(ctx, box.field_1323, box.field_1325, box.field_1321, box.field_1320, box.field_1325, box.field_1321, color);
        WorldOverlay.line(ctx, box.field_1320, box.field_1325, box.field_1321, box.field_1320, box.field_1325, box.field_1324, color);
        WorldOverlay.line(ctx, box.field_1320, box.field_1325, box.field_1324, box.field_1323, box.field_1325, box.field_1324, color);
        WorldOverlay.line(ctx, box.field_1323, box.field_1325, box.field_1324, box.field_1323, box.field_1325, box.field_1321, color);
        WorldOverlay.line(ctx, box.field_1323, box.field_1322, box.field_1321, box.field_1323, box.field_1325, box.field_1321, color);
        WorldOverlay.line(ctx, box.field_1320, box.field_1322, box.field_1321, box.field_1320, box.field_1325, box.field_1321, color);
        WorldOverlay.line(ctx, box.field_1320, box.field_1322, box.field_1324, box.field_1320, box.field_1325, box.field_1324, color);
        WorldOverlay.line(ctx, box.field_1323, box.field_1322, box.field_1324, box.field_1323, box.field_1325, box.field_1324, color);
    }

    private static class_1921 lines() {
        if (!linesResolved) {
            linesResolved = true;
            try {
                linesLayer = (class_1921)Class.forName("net.minecraft.client.render.RenderLayers").getField("LINES").get(null);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            if (linesLayer == null) {
                try {
                    linesLayer = (class_1921)class_1921.class.getMethod("getLines", new Class[0]).invoke(null, new Object[0]);
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }
        return linesLayer;
    }

    private static Method lineWidth() {
        if (!lineWidthResolved) {
            lineWidthResolved = true;
            try {
                lineWidthMethod = class_4588.class.getMethod("lineWidth", Float.TYPE);
            }
            catch (Throwable ignored) {
                lineWidthMethod = null;
            }
        }
        return lineWidthMethod;
    }

    public static void line(WorldRenderContext ctx, double x1, double y1, double z1, double x2, double y2, double z2, int color) {
        Method lw;
        class_1921 layer = WorldOverlay.lines();
        if (layer == null) {
            return;
        }
        class_243 cam = class_310.method_1551().field_1773.method_19418().method_71156();
        class_4587 matrices = ctx.matrices();
        class_4597 consumers = ctx.consumers();
        matrices.method_22903();
        matrices.method_22904(-cam.field_1352, -cam.field_1351, -cam.field_1350);
        class_4587.class_4665 entry = matrices.method_23760();
        class_4588 buf = consumers.method_73477(layer);
        float r = (float)(color >> 16 & 0xFF) / 255.0f;
        float g = (float)(color >> 8 & 0xFF) / 255.0f;
        float b = (float)(color & 0xFF) / 255.0f;
        float a = (float)(color >>> 24 & 0xFF) / 255.0f;
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        float nx = 0.0f;
        float ny = 1.0f;
        float nz = 0.0f;
        if (len > 1.0E-6) {
            nx = (float)(dx / len);
            ny = (float)(dy / len);
            nz = (float)(dz / len);
        }
        if ((lw = WorldOverlay.lineWidth()) != null) {
            try {
                lw.invoke((Object)buf.method_56824(entry, (float)x1, (float)y1, (float)z1).method_22915(r, g, b, a).method_60831(entry, nx, ny, nz), Float.valueOf(1.0f));
                lw.invoke((Object)buf.method_56824(entry, (float)x2, (float)y2, (float)z2).method_22915(r, g, b, a).method_60831(entry, nx, ny, nz), Float.valueOf(1.0f));
            }
            catch (Throwable throwable) {}
        } else {
            buf.method_56824(entry, (float)x1, (float)y1, (float)z1).method_22915(r, g, b, a).method_60831(entry, nx, ny, nz);
            buf.method_56824(entry, (float)x2, (float)y2, (float)z2).method_22915(r, g, b, a).method_60831(entry, nx, ny, nz);
        }
        matrices.method_22909();
    }
}

