/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1297
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_4587
 *  net.minecraft.class_4588
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package net.fastclient.hud.modules.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fastclient.hud.modules.Category;
import net.fastclient.hud.modules.Module;
import net.fastclient.hud.modules.settings.BooleanSetting;
import net.fastclient.hud.modules.settings.ColorSetting;
import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

@Environment(value=EnvType.CLIENT)
public class HitboxModule
extends Module {
    private final ColorSetting boxColor = this.register(new ColorSetting("box_color", "Hitbox color", 255, 255, 255, 255));
    private final ColorSetting eyeHeightColor = this.register(new ColorSetting("eye_height_color", "Eye height indicator color", 255, 0, 0, 255));
    private final ColorSetting lookDirColor = this.register(new ColorSetting("look_dir_color", "Look direction line color", 0, 0, 255, 255));
    private final BooleanSetting showEyeHeight = this.register(new BooleanSetting("show_eye_height", "Show eye height indicator", true));
    private final BooleanSetting showLookVector = this.register(new BooleanSetting("show_look_vector", "Show look direction line", true));

    public HitboxModule() {
        super("Hitbox", "Customizable entity hitbox rendering (F3+B)", Category.RENDER);
        this.eyeHeightColor.visibleWhen(this.showEyeHeight::getValue);
        this.lookDirColor.visibleWhen(this.showLookVector::getValue);
    }

    @Override
    protected void onEnable() {
        this.sendMessage("Hitbox customization enabled - use F3+B to toggle hitboxes");
    }

    public void renderCustomHitbox(class_4587 poseStack, class_4588 vertices, class_1297 entity, float tickDelta) {
        double eyeHeight;
        class_238 box = entity.method_5829().method_989(-entity.method_23317(), -entity.method_23318(), -entity.method_23321());
        float r = (float)this.boxColor.getRed() / 255.0f;
        float g = (float)this.boxColor.getGreen() / 255.0f;
        float b = (float)this.boxColor.getBlue() / 255.0f;
        float a = (float)this.boxColor.getAlpha() / 255.0f;
        Matrix4f matrix = poseStack.method_23760().method_23761();
        this.drawBoxOutline(vertices, matrix, box, r, g, b, a);
        if (((Boolean)this.showEyeHeight.getValue()).booleanValue()) {
            float eyeR = (float)this.eyeHeightColor.getRed() / 255.0f;
            float eyeG = (float)this.eyeHeightColor.getGreen() / 255.0f;
            float eyeB = (float)this.eyeHeightColor.getBlue() / 255.0f;
            float eyeA = (float)this.eyeHeightColor.getAlpha() / 255.0f;
            eyeHeight = entity.method_5751();
            float minX = (float)box.field_1323;
            float maxX = (float)box.field_1320;
            float minZ = (float)box.field_1321;
            float maxZ = (float)box.field_1324;
            this.drawLine(vertices, matrix, minX, (float)eyeHeight, minZ, maxX, (float)eyeHeight, minZ, eyeR, eyeG, eyeB, eyeA);
            this.drawLine(vertices, matrix, minX, (float)eyeHeight, maxZ, maxX, (float)eyeHeight, maxZ, eyeR, eyeG, eyeB, eyeA);
            this.drawLine(vertices, matrix, minX, (float)eyeHeight, minZ, minX, (float)eyeHeight, maxZ, eyeR, eyeG, eyeB, eyeA);
            this.drawLine(vertices, matrix, maxX, (float)eyeHeight, minZ, maxX, (float)eyeHeight, maxZ, eyeR, eyeG, eyeB, eyeA);
        }
        if (((Boolean)this.showLookVector.getValue()).booleanValue()) {
            float lookR = (float)this.lookDirColor.getRed() / 255.0f;
            float lookG = (float)this.lookDirColor.getGreen() / 255.0f;
            float lookB = (float)this.lookDirColor.getBlue() / 255.0f;
            float lookA = (float)this.lookDirColor.getAlpha() / 255.0f;
            eyeHeight = entity.method_5751();
            class_243 look = entity.method_5828(tickDelta);
            this.drawLine(vertices, matrix, 0.0f, (float)eyeHeight, 0.0f, (float)(look.field_1352 * 2.0), (float)(eyeHeight + look.field_1351 * 2.0), (float)(look.field_1350 * 2.0), lookR, lookG, lookB, lookA);
        }
    }

    private void drawBoxOutline(class_4588 vertices, Matrix4f matrix, class_238 box, float r, float g, float b, float a) {
        float minX = (float)box.field_1323;
        float minY = (float)box.field_1322;
        float minZ = (float)box.field_1321;
        float maxX = (float)box.field_1320;
        float maxY = (float)box.field_1325;
        float maxZ = (float)box.field_1324;
        this.drawLine(vertices, matrix, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
        this.drawLine(vertices, matrix, minX, minY, minZ, minX, minY, maxZ, r, g, b, a);
        this.drawLine(vertices, matrix, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
        this.drawLine(vertices, matrix, minX, minY, maxZ, maxX, minY, maxZ, r, g, b, a);
        this.drawLine(vertices, matrix, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
        this.drawLine(vertices, matrix, minX, maxY, minZ, minX, maxY, maxZ, r, g, b, a);
        this.drawLine(vertices, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
        this.drawLine(vertices, matrix, minX, maxY, maxZ, maxX, maxY, maxZ, r, g, b, a);
        this.drawLine(vertices, matrix, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
        this.drawLine(vertices, matrix, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
        this.drawLine(vertices, matrix, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
        this.drawLine(vertices, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
    }

    private void drawLine(class_4588 vertices, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float len = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len > 0.0f) {
            dx /= len;
            dy /= len;
            dz /= len;
        }
        vertices.method_22918((Matrix4fc)matrix, x1, y1, z1).method_22915(r, g, b, a).method_22914(dx, dy, dz).method_75298(1.0f);
        vertices.method_22918((Matrix4fc)matrix, x2, y2, z2).method_22915(r, g, b, a).method_22914(dx, dy, dz).method_75298(1.0f);
    }

    public int getBoxColorARGB() {
        return this.boxColor.getAlpha() << 24 | this.boxColor.getRed() << 16 | this.boxColor.getGreen() << 8 | this.boxColor.getBlue();
    }

    public int getEyeHeightColorARGB() {
        return this.eyeHeightColor.getAlpha() << 24 | this.eyeHeightColor.getRed() << 16 | this.eyeHeightColor.getGreen() << 8 | this.eyeHeightColor.getBlue();
    }

    public int getLookDirColorARGB() {
        return this.lookDirColor.getAlpha() << 24 | this.lookDirColor.getRed() << 16 | this.lookDirColor.getGreen() << 8 | this.lookDirColor.getBlue();
    }

    public boolean shouldShowEyeHeight() {
        return (Boolean)this.showEyeHeight.getValue();
    }

    public boolean shouldShowLookVector() {
        return (Boolean)this.showLookVector.getValue();
    }

    public float getBoxColorR() {
        return (float)this.boxColor.getRed() / 255.0f;
    }

    public float getBoxColorG() {
        return (float)this.boxColor.getGreen() / 255.0f;
    }

    public float getBoxColorB() {
        return (float)this.boxColor.getBlue() / 255.0f;
    }
}

