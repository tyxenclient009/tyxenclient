/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10017
 *  net.minecraft.class_12249
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1429
 *  net.minecraft.class_1531
 *  net.minecraft.class_1588
 *  net.minecraft.class_1657
 *  net.minecraft.class_239
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_332
 *  net.minecraft.class_3966
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_4588
 *  net.minecraft.class_4597
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package net.fastclient.hud.modules.impl.render;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fastclient.hud.accessor.EntityRenderStateAccessor;
import net.fastclient.hud.gui.DisplaySpace;
import net.fastclient.hud.modules.Category;
import net.fastclient.hud.modules.Module;
import net.fastclient.hud.modules.settings.BooleanSetting;
import net.fastclient.hud.modules.settings.ColorSetting;
import net.fastclient.hud.modules.settings.ModeSetting;
import net.fastclient.hud.modules.settings.NumberSetting;
import net.minecraft.class_10017;
import net.minecraft.class_12249;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1429;
import net.minecraft.class_1531;
import net.minecraft.class_1588;
import net.minecraft.class_1657;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_332;
import net.minecraft.class_3966;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class DamageIndicator
extends Module {
    private final ModeSetting displayMode = this.register(new ModeSetting("mode", "Display mode", "Crosshair", new String[]{"Crosshair", "World", "Panel", "Both"}));
    private final BooleanSetting showPlayers = this.register(new BooleanSetting("show_players", "Show for players", true));
    private final BooleanSetting showHostile = this.register(new BooleanSetting("show_hostile", "Show for hostile mobs", true));
    private final BooleanSetting showPassive = this.register(new BooleanSetting("show_passive", "Show for passive mobs", true));
    private final NumberSetting worldRange = this.register(new NumberSetting("world_range", "Range for world health bars", 24.0, 8.0, 64.0, 4.0));
    private final NumberSetting worldBarWidth = this.register(new NumberSetting("world_bar_width", "World health bar width", 1.5, 0.5, 3.0, 0.1));
    private final NumberSetting worldBarHeight = this.register(new NumberSetting("world_bar_height", "World health bar height", 0.1, 0.05, 0.3, 0.01));
    private final BooleanSetting worldShowName = this.register(new BooleanSetting("world_show_name", "Show name in world", false));
    private final BooleanSetting worldOnlyDamaged = this.register(new BooleanSetting("world_only_damaged", "Only show when damaged", false));
    private final BooleanSetting showName = this.register(new BooleanSetting("show_name", "Show entity name", true));
    private final BooleanSetting showHealthText = this.register(new BooleanSetting("health_text", "Show health as text", true));
    private final BooleanSetting showPercent = this.register(new BooleanSetting("show_percent", "Show as percentage", false));
    private final NumberSetting barWidth = this.register(new NumberSetting("bar_width", "Health bar width", 80.0, 40.0, 150.0, 5.0));
    private final NumberSetting verticalOffset = this.register(new NumberSetting("offset", "Vertical offset from crosshair", 50.0, 20.0, 100.0, 5.0));
    private final ColorSetting healthyColor = this.register(new ColorSetting("healthy_color", "Color when health is high", 100, 255, 100));
    private final ColorSetting damagedColor = this.register(new ColorSetting("damaged_color", "Color when health is low", 255, 80, 80));
    private static final int PANEL_WIDTH = 130;
    private static final int PANEL_HEIGHT = 38;
    private float displayedHealth = 0.0f;
    private float animatedHealthLag = 0.0f;
    private class_1309 targetEntity = null;
    private long lastTargetTime = 0L;
    private float fadeAlpha = 0.0f;
    private final Map<Integer, Float> entityDisplayedHealth = new HashMap<Integer, Float>();
    private final Map<Integer, Float> entityLagHealth = new HashMap<Integer, Float>();

    public DamageIndicator() {
        super("DamageIndicator", "Shows health of entities", Category.HUD);
        this.showPercent.visibleWhen(this.showHealthText::isEnabled);
        this.worldRange.visibleWhen(() -> ((String)this.displayMode.getValue()).equals("World"));
        this.worldBarWidth.visibleWhen(() -> ((String)this.displayMode.getValue()).equals("World"));
        this.worldBarHeight.visibleWhen(() -> ((String)this.displayMode.getValue()).equals("World"));
        this.worldShowName.visibleWhen(() -> ((String)this.displayMode.getValue()).equals("World"));
        this.worldOnlyDamaged.visibleWhen(() -> ((String)this.displayMode.getValue()).equals("World"));
        this.verticalOffset.visibleWhen(() -> {
            String m = (String)this.displayMode.getValue();
            return m.equals("Crosshair") || m.equals("Both");
        });
        this.barWidth.visibleWhen(() -> {
            String m = (String)this.displayMode.getValue();
            return m.equals("Crosshair") || m.equals("Both");
        });
        this.showName.visibleWhen(() -> !((String)this.displayMode.getValue()).equals("World"));
        this.showHealthText.visibleWhen(() -> !((String)this.displayMode.getValue()).equals("World"));
    }

    @Override
    public int getHudWidth() {
        return (int)(130.0f * this.getHudScale());
    }

    @Override
    public int getHudHeight() {
        return (int)(38.0f * this.getHudScale());
    }

    @Override
    public boolean isHudVisible() {
        String mode = (String)this.displayMode.getValue();
        return mode.equals("Panel") || mode.equals("Both");
    }

    @Override
    public void onTick() {
        class_3966 entityHit;
        class_1297 entity;
        if (!this.isInGame()) {
            this.targetEntity = null;
            return;
        }
        class_239 hitResult = DamageIndicator.mc.field_1765;
        if (hitResult != null && hitResult.method_17783() == class_239.class_240.field_1331 && (entity = (entityHit = (class_3966)hitResult).method_17782()) instanceof class_1309) {
            class_1309 living = (class_1309)entity;
            if (this.shouldShowFor(entity)) {
                if (this.targetEntity != living) {
                    this.displayedHealth = living.method_6032();
                    this.animatedHealthLag = living.method_6032();
                }
                this.targetEntity = living;
                this.lastTargetTime = System.currentTimeMillis();
                return;
            }
        }
        if (System.currentTimeMillis() - this.lastTargetTime > 500L) {
            this.targetEntity = null;
        }
    }

    @Override
    public void onRender(class_332 graphics, float tickDelta) {
        if (!this.isInGame() || DamageIndicator.mc.field_1687 == null || DamageIndicator.mc.field_1724 == null) {
            return;
        }
        String mode = (String)this.displayMode.getValue();
        if (mode.equals("World")) {
            return;
        }
        float targetAlpha = this.targetEntity != null && !this.targetEntity.method_29504() ? 1.0f : 0.0f;
        this.fadeAlpha += (targetAlpha - this.fadeAlpha) * 0.3f;
        if (this.fadeAlpha < 0.01f) {
            return;
        }
        if (this.targetEntity != null) {
            float currentHealth = this.targetEntity.method_6032();
            this.displayedHealth += (currentHealth - this.displayedHealth) * 0.5f;
            this.animatedHealthLag += (currentHealth - this.animatedHealthLag) * 0.1f;
        }
        if (mode.equals("Crosshair") || mode.equals("Both")) {
            this.renderCrosshairIndicator(graphics);
        }
        if (mode.equals("Panel") || mode.equals("Both")) {
            this.renderPanel(graphics);
        }
    }

    private void renderCrosshairIndicator(class_332 graphics) {
        if (this.targetEntity == null) {
            return;
        }
        int screenWidth = DisplaySpace.width();
        int screenHeight = DisplaySpace.height();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        float maxHealth = this.targetEntity.method_6063();
        float healthPercent = Math.min(1.0f, Math.max(0.0f, this.displayedHealth / maxHealth));
        float lagPercent = Math.min(1.0f, Math.max(0.0f, this.animatedHealthLag / maxHealth));
        int barW = this.barWidth.getIntValue();
        int barH = 4;
        int yOffset = this.verticalOffset.getIntValue();
        int alpha = (int)(this.fadeAlpha * 255.0f);
        int bgAlpha = (int)(this.fadeAlpha * 200.0f);
        int indicatorX = centerX - barW / 2;
        int indicatorY = centerY - yOffset;
        if (this.showName.isEnabled()) {
            String name = this.targetEntity.method_5476().getString();
            int nameWidth = DamageIndicator.mc.field_1772.method_1727(name);
            int nameX = centerX - nameWidth / 2;
            int nameY = indicatorY - 12;
            graphics.method_51433(DamageIndicator.mc.field_1772, name, nameX, nameY, alpha << 24 | 0xFFFFFF, true);
            indicatorY += 2;
        }
        int barX = indicatorX;
        int barY = indicatorY;
        int bgColor = bgAlpha << 24 | 0xA0A0A;
        graphics.method_25294(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, bgAlpha << 24 | 0);
        graphics.method_25294(barX, barY, barX + barW, barY + barH, bgColor);
        if (lagPercent > healthPercent) {
            int lagWidth = (int)((float)barW * lagPercent);
            int damageColor = alpha << 24 | 0xFF3333;
            graphics.method_25294(barX, barY, barX + lagWidth, barY + barH, damageColor);
        }
        int healthColor = this.interpolateColor(healthPercent);
        int filledWidth = Math.max(0, (int)((float)barW * healthPercent));
        if (filledWidth > 0) {
            graphics.method_25294(barX, barY, barX + filledWidth, barY + barH, alpha << 24 | healthColor);
            int highlight = this.brightenColor(healthColor, 1.4f);
            graphics.method_25294(barX, barY, barX + filledWidth, barY + 1, alpha << 24 | highlight);
        }
        int borderAlpha = (int)(this.fadeAlpha * 100.0f);
        int borderColor = borderAlpha << 24 | 0x333333;
        graphics.method_25294(barX - 1, barY - 1, barX + barW + 1, barY, borderColor);
        graphics.method_25294(barX - 1, barY + barH, barX + barW + 1, barY + barH + 1, borderColor);
        graphics.method_25294(barX - 1, barY, barX, barY + barH, borderColor);
        graphics.method_25294(barX + barW, barY, barX + barW + 1, barY + barH, borderColor);
        if (this.showHealthText.isEnabled()) {
            String healthText = this.formatHealth(this.displayedHealth, maxHealth, healthPercent);
            int textWidth = DamageIndicator.mc.field_1772.method_1727(healthText);
            int textX = centerX - textWidth / 2;
            int textY = barY + barH + 3;
            graphics.method_51433(DamageIndicator.mc.field_1772, healthText, textX, textY, alpha << 24 | 0xCCCCCC, true);
        }
    }

    private void renderPanel(class_332 graphics) {
        if (this.targetEntity == null) {
            return;
        }
        float maxHealth = this.targetEntity.method_6063();
        float healthPercent = Math.min(1.0f, Math.max(0.0f, this.displayedHealth / maxHealth));
        int healthColor = this.interpolateColor(healthPercent);
        double distance = DamageIndicator.mc.field_1724.method_5739((class_1297)this.targetEntity);
        int x = this.getHudX();
        int y = this.getHudY();
        float scale = this.getHudScale();
        int panelW = 130;
        int panelH = 38;
        int padding = 6;
        int alpha = (int)(this.fadeAlpha * 240.0f);
        int bgAlpha = (int)(this.fadeAlpha * 220.0f);
        graphics.method_51448().pushMatrix();
        graphics.method_51448().translate((float)x, (float)y);
        graphics.method_51448().scale(scale, scale);
        graphics.method_51448().translate((float)(-x), (float)(-y));
        graphics.method_25294(x, y, x + panelW, y + panelH, bgAlpha << 24 | 0x141418);
        graphics.method_25294(x, y, x + 2, y + panelH, alpha << 24 | healthColor);
        int contentX = x + padding + 2;
        int contentY = y + padding;
        int barW = panelW - padding * 2 - 2;
        Object name = this.targetEntity.method_5476().getString();
        if (((String)name).length() > 14) {
            name = ((String)name).substring(0, 14) + "...";
        }
        String distText = String.format("%.1fm", distance);
        graphics.method_51433(DamageIndicator.mc.field_1772, (String)name, contentX, contentY, alpha << 24 | 0xFFFFFF, false);
        int distWidth = DamageIndicator.mc.field_1772.method_1727(distText);
        graphics.method_51433(DamageIndicator.mc.field_1772, distText, x + panelW - padding - distWidth, contentY, alpha << 24 | 0x888888, false);
        int barH = 4;
        graphics.method_25294(contentX, contentY += 11, contentX + barW, contentY + barH, bgAlpha << 24 | 0x222222);
        int filledW = Math.max(0, (int)((float)barW * healthPercent));
        if (filledW > 0) {
            graphics.method_25294(contentX, contentY, contentX + filledW, contentY + barH, alpha << 24 | healthColor);
            graphics.method_25294(contentX, contentY, contentX + filledW, contentY + 1, alpha << 24 | this.brightenColor(healthColor, 1.3f));
        }
        String healthStr = this.formatHealth(this.displayedHealth, maxHealth, healthPercent);
        graphics.method_51433(DamageIndicator.mc.field_1772, healthStr, contentX, contentY += 7, alpha << 24 | healthColor, false);
        graphics.method_51448().popMatrix();
    }

    public void renderWorldHealthBars(class_4587 poseStack, class_4597 bufferSource, class_4184 camera, float partialTicks) {
        if (!this.isEnabled() || !((String)this.displayMode.getValue()).equals("World")) {
            return;
        }
        if (DamageIndicator.mc.field_1687 == null || DamageIndicator.mc.field_1724 == null) {
            return;
        }
        class_243 cameraPos = camera.method_71156();
        double range = (Double)this.worldRange.getValue();
        for (class_1297 entity : DamageIndicator.mc.field_1687.method_18112()) {
            double dist;
            if (!(entity instanceof class_1309)) continue;
            class_1309 living = (class_1309)entity;
            if (!this.shouldShowFor(entity) || living.method_29504() || (dist = (double)DamageIndicator.mc.field_1724.method_5739((class_1297)living)) > range || this.worldOnlyDamaged.isEnabled() && living.method_6032() >= living.method_6063()) continue;
            this.renderEntityHealthBar(poseStack, bufferSource, living, cameraPos, partialTicks);
        }
    }

    private void renderEntityHealthBar(class_4587 poseStack, class_4597 bufferSource, class_1309 entity, class_243 cameraPos, float partialTicks) {
        double x = entity.field_6038 + (entity.method_23317() - entity.field_6038) * (double)partialTicks;
        double y = entity.field_5971 + (entity.method_23318() - entity.field_5971) * (double)partialTicks;
        double z = entity.field_5989 + (entity.method_23321() - entity.field_5989) * (double)partialTicks;
        float height = entity.method_17682() + 0.5f;
        double offsetX = x - cameraPos.field_1352;
        double offsetY = y - cameraPos.field_1351 + (double)height;
        double offsetZ = z - cameraPos.field_1350;
        poseStack.method_22903();
        poseStack.method_22904(offsetX, offsetY, offsetZ);
        float yaw = DamageIndicator.mc.field_1724.method_36454();
        float pitch = DamageIndicator.mc.field_1724.method_36455();
        Quaternionf rotation = new Quaternionf().rotationY((float)Math.toRadians(yaw + 180.0f)).rotateX((float)Math.toRadians(-pitch));
        poseStack.method_22907((Quaternionfc)rotation);
        float barScale = 0.025f;
        poseStack.method_22905(-barScale, -barScale, barScale);
        int entityId = entity.method_5628();
        float currentHealth = entity.method_6032();
        float maxHealth = entity.method_6063();
        float displayHealth = this.entityDisplayedHealth.getOrDefault(entityId, Float.valueOf(currentHealth)).floatValue();
        float lagHealth = this.entityLagHealth.getOrDefault(entityId, Float.valueOf(currentHealth)).floatValue();
        displayHealth += (currentHealth - displayHealth) * 0.3f;
        lagHealth += (currentHealth - lagHealth) * 0.08f;
        this.entityDisplayedHealth.put(entityId, Float.valueOf(displayHealth));
        this.entityLagHealth.put(entityId, Float.valueOf(lagHealth));
        float healthPercent = Math.min(1.0f, Math.max(0.0f, displayHealth / maxHealth));
        float lagPercent = Math.min(1.0f, Math.max(0.0f, lagHealth / maxHealth));
        float barW = (float)((Double)this.worldBarWidth.getValue() * 40.0);
        float barH = (float)((Double)this.worldBarHeight.getValue() * 40.0);
        class_4588 consumer = bufferSource.method_73477(class_12249.method_76023());
        Matrix4f matrix = poseStack.method_23760().method_23761();
        float halfW = barW / 2.0f;
        float yOffset = this.worldShowName.isEnabled() ? 10.0f : 0.0f;
        this.renderQuad(consumer, matrix, -halfW, yOffset, halfW, yOffset + barH, 0.1f, 0.1f, 0.1f, 0.8f);
        if (lagPercent > healthPercent) {
            float lagWidth = barW * lagPercent;
            this.renderQuad(consumer, matrix, -halfW, yOffset, -halfW + lagWidth, yOffset + barH, 0.8f, 0.2f, 0.2f, 0.9f);
        }
        int healthColor = this.interpolateColor(healthPercent);
        float r = (float)(healthColor >> 16 & 0xFF) / 255.0f;
        float g = (float)(healthColor >> 8 & 0xFF) / 255.0f;
        float b = (float)(healthColor & 0xFF) / 255.0f;
        float healthWidth = barW * healthPercent;
        if (healthWidth > 0.0f) {
            this.renderQuad(consumer, matrix, -halfW, yOffset, -halfW + healthWidth, yOffset + barH, r, g, b, 1.0f);
            float hr = Math.min(1.0f, r * 1.4f);
            float hg = Math.min(1.0f, g * 1.4f);
            float hb = Math.min(1.0f, b * 1.4f);
            this.renderQuad(consumer, matrix, -halfW, yOffset, -halfW + healthWidth, yOffset + barH * 0.3f, hr, hg, hb, 0.8f);
        }
        float borderSize = 0.5f;
        this.renderQuad(consumer, matrix, -halfW - borderSize, yOffset - borderSize, halfW + borderSize, yOffset, 0.2f, 0.2f, 0.2f, 0.6f);
        this.renderQuad(consumer, matrix, -halfW - borderSize, yOffset + barH, halfW + borderSize, yOffset + barH + borderSize, 0.2f, 0.2f, 0.2f, 0.6f);
        this.renderQuad(consumer, matrix, -halfW - borderSize, yOffset, -halfW, yOffset + barH, 0.2f, 0.2f, 0.2f, 0.6f);
        this.renderQuad(consumer, matrix, halfW, yOffset, halfW + borderSize, yOffset + barH, 0.2f, 0.2f, 0.2f, 0.6f);
        poseStack.method_22909();
    }

    private void renderQuad(class_4588 consumer, Matrix4f matrix, float x1, float y1, float x2, float y2, float r, float g, float b, float a) {
        consumer.method_22918((Matrix4fc)matrix, x1, y1, 0.0f).method_22915(r, g, b, a);
        consumer.method_22918((Matrix4fc)matrix, x1, y2, 0.0f).method_22915(r, g, b, a);
        consumer.method_22918((Matrix4fc)matrix, x2, y2, 0.0f).method_22915(r, g, b, a);
        consumer.method_22918((Matrix4fc)matrix, x2, y1, 0.0f).method_22915(r, g, b, a);
    }

    public boolean isWorldMode() {
        return ((String)this.displayMode.getValue()).equals("World");
    }

    public double getWorldRange() {
        return (Double)this.worldRange.getValue();
    }

    public boolean isOnlyDamaged() {
        return this.worldOnlyDamaged.isEnabled();
    }

    public float getWorldBarWidth() {
        return ((Double)this.worldBarWidth.getValue()).floatValue();
    }

    public float getWorldBarHeight() {
        return ((Double)this.worldBarHeight.getValue()).floatValue();
    }

    public float getEntityHealth(class_10017 state) {
        EntityRenderStateAccessor accessor;
        class_1297 entity;
        if (state instanceof EntityRenderStateAccessor && (entity = (accessor = (EntityRenderStateAccessor)state).fastclient$getEntity()) instanceof class_1309) {
            class_1309 living = (class_1309)entity;
            if (this.shouldShowFor(entity)) {
                return living.method_6032();
            }
        }
        return -1.0f;
    }

    public float getEntityMaxHealth(class_10017 state) {
        EntityRenderStateAccessor accessor;
        class_1297 entity;
        if (state instanceof EntityRenderStateAccessor && (entity = (accessor = (EntityRenderStateAccessor)state).fastclient$getEntity()) instanceof class_1309) {
            class_1309 living = (class_1309)entity;
            if (this.shouldShowFor(entity)) {
                return living.method_6063();
            }
        }
        return 0.0f;
    }

    public int getHealthColor(float healthPercent) {
        return this.interpolateColor(healthPercent);
    }

    public boolean shouldShowPlayers() {
        return this.showPlayers.isEnabled();
    }

    public boolean shouldShowHostile() {
        return this.showHostile.isEnabled();
    }

    public boolean shouldShowPassive() {
        return this.showPassive.isEnabled();
    }

    public boolean shouldRenderWorldHealthBar(class_1309 entity) {
        if (!this.shouldShowFor((class_1297)entity)) {
            return false;
        }
        if (entity.method_29504()) {
            return false;
        }
        return !this.worldOnlyDamaged.isEnabled() || !(entity.method_6032() >= entity.method_6063());
    }

    private String formatHealth(float health, float maxHealth, float healthPercent) {
        if (this.showPercent.isEnabled()) {
            return String.format("%.0f%%", Float.valueOf(healthPercent * 100.0f));
        }
        if ((double)health == Math.floor(health) && (double)maxHealth == Math.floor(maxHealth)) {
            return String.format("%.0f / %.0f \u2764", Float.valueOf(health), Float.valueOf(maxHealth));
        }
        return String.format("%.1f / %.0f \u2764", Float.valueOf(health), Float.valueOf(maxHealth));
    }

    private boolean shouldShowFor(class_1297 entity) {
        if (entity instanceof class_1531) {
            return false;
        }
        if (entity == DamageIndicator.mc.field_1724) {
            return false;
        }
        if (entity instanceof class_1657) {
            return this.showPlayers.isEnabled();
        }
        if (entity instanceof class_1588) {
            return this.showHostile.isEnabled();
        }
        if (entity instanceof class_1429) {
            return this.showPassive.isEnabled();
        }
        return true;
    }

    private int interpolateColor(float percent) {
        int r1 = this.damagedColor.getRed();
        int g1 = this.damagedColor.getGreen();
        int b1 = this.damagedColor.getBlue();
        int r2 = this.healthyColor.getRed();
        int g2 = this.healthyColor.getGreen();
        int b2 = this.healthyColor.getBlue();
        float t = percent * percent * (3.0f - 2.0f * percent);
        int r = (int)((float)r1 + (float)(r2 - r1) * t);
        int g = (int)((float)g1 + (float)(g2 - g1) * t);
        int b = (int)((float)b1 + (float)(b2 - b1) * t);
        return r << 16 | g << 8 | b;
    }

    private int brightenColor(int color, float factor) {
        int r = Math.min(255, (int)((float)(color >> 16 & 0xFF) * factor));
        int g = Math.min(255, (int)((float)(color >> 8 & 0xFF) * factor));
        int b = Math.min(255, (int)((float)(color & 0xFF) * factor));
        return r << 16 | g << 8 | b;
    }
}

