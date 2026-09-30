/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_332
 */
package net.fastclient.hud.modules.impl.hud;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fastclient.hud.gui.FastClientUI;
import net.fastclient.hud.modules.Category;
import net.fastclient.hud.modules.Module;
import net.fastclient.hud.modules.settings.BooleanSetting;
import net.fastclient.hud.modules.settings.ColorSetting;
import net.fastclient.hud.modules.settings.ModeSetting;
import net.minecraft.class_332;

@Environment(value=EnvType.CLIENT)
public class MemoryHUD
extends Module {
    private final ModeSetting displayMode = this.register(new ModeSetting("display", "Display format", "percent", new String[]{"percent", "mb", "both"}));
    private final BooleanSetting colorByUsage = this.register(new BooleanSetting("color_by_usage", "Color by usage", true));
    private final BooleanSetting background = this.register(new BooleanSetting("background", "Show background", true));
    private final ColorSetting staticColor = this.register(new ColorSetting("color", "Static color", 255, 255, 255));

    public MemoryHUD() {
        super("Memory", "Shows RAM/memory usage", Category.HUD);
        this.staticColor.visibleWhen(() -> !this.colorByUsage.isEnabled());
    }

    @Override
    public void onRender(class_332 graphics, float tickDelta) {
        String text;
        int x = this.getHudX();
        int y = this.getHudY();
        float scale = this.getHudScale();
        graphics.method_51448().pushMatrix();
        graphics.method_51448().translate((float)x, (float)y);
        graphics.method_51448().scale(scale, scale);
        graphics.method_51448().translate((float)(-x), (float)(-y));
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        int percent = (int)(usedMemory * 100L / maxMemory);
        long usedMB = usedMemory / 0x100000L;
        long maxMB = maxMemory / 0x100000L;
        switch ((String)this.displayMode.getValue()) {
            case "mb": {
                String string = usedMB + "/" + maxMB + " MB";
                break;
            }
            case "both": {
                String string = percent + "% (" + usedMB + "/" + maxMB + " MB)";
                break;
            }
            default: {
                String string = text = "RAM: " + percent + "%";
            }
        }
        int color = this.colorByUsage.isEnabled() ? (percent < 50 ? -11141291 : (percent < 75 ? -171 : (percent < 90 ? -22016 : -43691))) : this.staticColor.getRGB() | 0xFF000000;
        if (this.background.isEnabled()) {
            FastClientUI.hudText(graphics, MemoryHUD.mc.field_1772, text, x, y, color, true);
        } else {
            graphics.method_51433(MemoryHUD.mc.field_1772, text, x, y, color, true);
        }
        graphics.method_51448().popMatrix();
    }

    @Override
    public int getHudWidth() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        int percent = (int)(usedMemory * 100L / maxMemory);
        long usedMB = usedMemory / 0x100000L;
        long maxMB = maxMemory / 0x100000L;
        String text = switch ((String)this.displayMode.getValue()) {
            case "mb" -> usedMB + "/" + maxMB + " MB";
            case "both" -> percent + "% (" + usedMB + "/" + maxMB + " MB)";
            default -> "RAM: " + percent + "%";
        };
        return MemoryHUD.mc.field_1772.method_1727(text) + 10;
    }

    @Override
    public int getHudHeight() {
        return 17;
    }
}

