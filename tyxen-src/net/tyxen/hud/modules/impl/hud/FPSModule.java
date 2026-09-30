/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_2561
 *  net.minecraft.class_2583
 *  net.minecraft.class_332
 *  net.minecraft.class_5250
 *  net.minecraft.class_5348
 */
package net.tyxen.hud.modules.impl.hud;

import java.util.LinkedList;
import java.util.Queue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.gui.TyxenUI;
import net.tyxen.hud.modules.Category;
import net.tyxen.hud.modules.Module;
import net.tyxen.hud.modules.settings.BooleanSetting;
import net.tyxen.hud.modules.settings.ColorSetting;
import net.tyxen.hud.modules.settings.ModeSetting;
import net.tyxen.hud.modules.settings.NumberSetting;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_332;
import net.minecraft.class_5250;
import net.minecraft.class_5348;

@Environment(value=EnvType.CLIENT)
public class FPSModule
extends Module {
    private static final int MUTED_TEXT = 0xAAAAAA;
    private final BooleanSetting showAverage = this.register(new BooleanSetting("show_average", "Show average FPS", true));
    private final BooleanSetting showMin = this.register(new BooleanSetting("show_min", "Show minimum FPS", true));
    private final ModeSetting colorMode = this.register(new ModeSetting("color_mode", "Color mode", "dynamic", new String[]{"dynamic", "static"}));
    private final ColorSetting staticColor = this.register(new ColorSetting("color", "Static color", 255, 255, 255));
    private final BooleanSetting background = this.register(new BooleanSetting("background", "Show background", true));
    private final NumberSetting bgOpacity = this.register(new NumberSetting("opacity", "Background opacity", 80.0, 0.0, 255.0, 5.0));
    private final Queue<Integer> fpsHistory = new LinkedList<Integer>();
    private int minFps = Integer.MAX_VALUE;
    private int avgFps = 0;
    private long lastUpdate = 0L;

    public FPSModule() {
        super("FPS", "Advanced FPS counter", Category.HUD);
        this.staticColor.visibleWhen(() -> this.colorMode.is("static"));
        this.bgOpacity.visibleWhen(this.background::isEnabled);
    }

    @Override
    protected void onEnable() {
        this.fpsHistory.clear();
        this.minFps = Integer.MAX_VALUE;
    }

    @Override
    public void onTick() {
        if (System.currentTimeMillis() - this.lastUpdate > 50L) {
            int currentFps = mc.method_47599();
            this.fpsHistory.add(currentFps);
            if (this.fpsHistory.size() > 100) {
                this.fpsHistory.poll();
            }
            if (currentFps < this.minFps && currentFps > 0) {
                this.minFps = currentFps;
            }
            this.avgFps = (int)this.fpsHistory.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            this.lastUpdate = System.currentTimeMillis();
        }
    }

    @Override
    public void onRender(class_332 graphics, float tickDelta) {
        if (!this.isInGame()) {
            return;
        }
        int fps = mc.method_47599();
        int x = Math.max(3, this.getHudX());
        int y = Math.max(3, this.getHudY());
        float scale = this.getHudScale();
        graphics.method_51448().pushMatrix();
        graphics.method_51448().translate((float)x, (float)y);
        graphics.method_51448().scale(scale, scale);
        graphics.method_51448().translate((float)(-x), (float)(-y));
        int color = this.getDisplayColor(fps);
        class_2561 displayText = this.buildDisplayText(fps, color);
        if (((Boolean)this.background.getValue()).booleanValue()) {
            int width = FPSModule.mc.field_1772.method_27525((class_5348)displayText);
            TyxenUI.hudPanel(graphics, x - 6, y - 5, width + 12, 19, this.bgOpacity.getIntValue());
        }
        graphics.method_51439(FPSModule.mc.field_1772, displayText, x, y, -1, true);
        graphics.method_51448().popMatrix();
    }

    private class_2561 buildDisplayText(int fps, int color) {
        class_5250 text = class_2561.method_43473().method_10852((class_2561)class_2561.method_43470((String)("FPS: " + fps)).method_27696(class_2583.field_24360.method_36139(color & 0xFFFFFF)));
        if (((Boolean)this.showAverage.getValue()).booleanValue()) {
            text.method_10852((class_2561)class_2561.method_43470((String)(" (Avg: " + this.avgFps + ")")).method_27696(class_2583.field_24360.method_36139(0xAAAAAA)));
        }
        if (((Boolean)this.showMin.getValue()).booleanValue() && this.minFps != Integer.MAX_VALUE) {
            text.method_10852((class_2561)class_2561.method_43470((String)(" (Min: " + this.minFps + ")")).method_27696(class_2583.field_24360.method_36139(0xAAAAAA)));
        }
        return text;
    }

    private int getDisplayColor(int fps) {
        if (this.colorMode.is("static")) {
            return this.staticColor.getRGB();
        }
        if (fps >= 60) {
            return -11141291;
        }
        if (fps >= 30) {
            return -171;
        }
        return -43691;
    }

    @Override
    public int getHudWidth() {
        int fps = mc.method_47599();
        return FPSModule.mc.field_1772.method_27525((class_5348)this.buildDisplayText(fps, this.getDisplayColor(fps))) + 12;
    }

    @Override
    public int getHudHeight() {
        return 19;
    }
}

