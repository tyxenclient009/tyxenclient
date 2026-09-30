/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
 *  net.minecraft.class_11658
 *  net.minecraft.class_11659
 *  net.minecraft.class_12075
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_4587
 *  net.minecraft.class_5250
 */
package net.fastclient.hud.render;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fastclient.hud.core.ModuleManager;
import net.fastclient.hud.modules.impl.render.WaypointsModule;
import net.minecraft.class_11658;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_4587;
import net.minecraft.class_5250;

@Environment(value=EnvType.CLIENT)
public final class WaypointWorldRenderer {
    private static final int FULL_BRIGHT = 0xF000F0;

    private WaypointWorldRenderer() {
    }

    public static void onBeforeEntities(WorldRenderContext context) {
        ModuleManager manager = ModuleManager.getInstance();
        if (manager == null) {
            return;
        }
        WaypointsModule module = manager.getModule(WaypointsModule.class);
        if (module == null || !module.isEnabled()) {
            return;
        }
        class_4587 poseStack = context.matrices();
        class_11659 output = context.commandQueue();
        class_11658 worldState = context.worldState();
        if (poseStack == null || output == null || worldState == null) {
            return;
        }
        class_12075 camera = worldState.field_63082;
        List<WaypointsModule.Waypoint> waypoints = module.getVisibleWorldWaypoints();
        for (WaypointsModule.Waypoint waypoint : waypoints) {
            WaypointWorldRenderer.renderMarker(poseStack, output, camera, camera.field_63078, module, waypoint);
        }
    }

    private static void renderMarker(class_4587 poseStack, class_11659 output, class_12075 camera, class_243 cameraPos, WaypointsModule module, WaypointsModule.Waypoint waypoint) {
        poseStack.method_22903();
        poseStack.method_22904(waypoint.x - cameraPos.field_1352, waypoint.y - cameraPos.field_1351, waypoint.z - cameraPos.field_1350);
        double distance = module.distanceToPlayer(waypoint);
        float markerScale = (float)Math.max(1.4, Math.min(64.0, distance / 16.0));
        poseStack.method_22905(markerScale, markerScale, markerScale);
        class_5250 label = class_2561.method_43470((String)"\u25c6").method_54663(waypoint.color & 0xFFFFFF).method_10852((class_2561)class_2561.method_43470((String)("  " + module.getWorldLabel(waypoint))).method_54663(0xFFFFFF));
        output.method_73482(poseStack, class_243.field_1353, 0, (class_2561)label, true, 0xF000F0, distance * distance, camera);
        poseStack.method_22909();
    }
}

