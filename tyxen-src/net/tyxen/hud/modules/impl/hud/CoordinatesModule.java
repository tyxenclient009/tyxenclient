/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_2338
 *  net.minecraft.class_332
 *  net.minecraft.class_6880
 */
package net.tyxen.hud.modules.impl.hud;

import java.util.ArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.gui.TyxenUI;
import net.tyxen.hud.modules.Category;
import net.tyxen.hud.modules.Module;
import net.tyxen.hud.modules.settings.BooleanSetting;
import net.tyxen.hud.modules.settings.ColorSetting;
import net.tyxen.hud.modules.settings.ModeSetting;
import net.tyxen.hud.modules.settings.NumberSetting;
import net.minecraft.class_2338;
import net.minecraft.class_332;
import net.minecraft.class_5321;
import net.minecraft.class_5321;
import net.minecraft.class_6880;

@Environment(value=EnvType.CLIENT)
public class CoordinatesModule
extends Module {
    private final ModeSetting displayMode = this.register(new ModeSetting("display_mode", "Display format", "compact", new String[]{"compact", "detailed", "minimal"}));
    private final BooleanSetting showDimension = this.register(new BooleanSetting("show_dimension", "Show dimension", true));
    private final BooleanSetting showDirection = this.register(new BooleanSetting("show_direction", "Show facing direction", true));
    private final BooleanSetting showBiome = this.register(new BooleanSetting("show_biome", "Show biome", false));
    private final ModeSetting colorMode = this.register(new ModeSetting("color_mode", "Color mode", "static", new String[]{"static", "axis"}));
    private final ColorSetting textColor = this.register(new ColorSetting("color", "Text color", 255, 255, 255));
    private final BooleanSetting background = this.register(new BooleanSetting("background", "Show background", true));
    private final NumberSetting bgOpacity = this.register(new NumberSetting("opacity", "Background opacity", 80.0, 0.0, 255.0, 5.0));

    public CoordinatesModule() {
        super("Coordinates", "Display player coordinates on screen", Category.HUD);
        this.textColor.visibleWhen(() -> this.colorMode.is("static"));
        this.bgOpacity.visibleWhen(this.background::isEnabled);
    }

    @Override
    public void onRender(class_332 graphics, float tickDelta) {
        String[] lines;
        if (!this.isInGame() || CoordinatesModule.mc.field_1724 == null) {
            return;
        }
        int x = this.getHudX();
        int y = this.getHudY();
        float scale = this.getHudScale();
        graphics.method_51448().pushMatrix();
        graphics.method_51448().translate((float)x, (float)y);
        graphics.method_51448().scale(scale, scale);
        graphics.method_51448().translate((float)(-x), (float)(-y));
        double playerX = CoordinatesModule.mc.field_1724.method_23317();
        double playerY = CoordinatesModule.mc.field_1724.method_23318();
        double playerZ = CoordinatesModule.mc.field_1724.method_23321();
        int lineHeight = 11;
        int currentY = y;
        int maxWidth = 0;
        for (String line : lines = this.buildDisplayLines(playerX, playerY, playerZ)) {
            int lineWidth = CoordinatesModule.mc.field_1772.method_1727(line.replaceAll("\u00a7.", ""));
            if (lineWidth <= maxWidth) continue;
            maxWidth = lineWidth;
        }
        if (((Boolean)this.background.getValue()).booleanValue()) {
            int totalHeight = lines.length * lineHeight + 6;
            TyxenUI.hudPanel(graphics, x - 5, y - 4, maxWidth + 10, totalHeight, this.bgOpacity.getIntValue());
        }
        for (int i = 0; i < lines.length; ++i) {
            int color = this.getLineColor(i);
            graphics.method_51433(CoordinatesModule.mc.field_1772, lines[i], x, currentY, color, true);
            currentY += lineHeight;
        }
        graphics.method_51448().popMatrix();
    }

    private String[] buildDisplayLines(double playerX, double playerY, double playerZ) {
        ArrayList<Object> lines = new ArrayList<Object>();
        if (this.displayMode.is("minimal")) {
            lines.add(String.format("%.0f, %.0f, %.0f", playerX, playerY, playerZ));
        } else if (this.displayMode.is("compact")) {
            lines.add(String.format("XYZ: %.1f / %.1f / %.1f", playerX, playerY, playerZ));
            if (((Boolean)this.showDirection.getValue()).booleanValue()) {
                lines.add("Facing: " + this.getCardinalDirection());
            }
            if (((Boolean)this.showDimension.getValue()).booleanValue()) {
                lines.add("Dim: " + this.getDimensionName());
            }
        } else {
            lines.add(String.format("X: %.2f", playerX));
            lines.add(String.format("Y: %.2f", playerY));
            lines.add(String.format("Z: %.2f", playerZ));
            if (((Boolean)this.showDirection.getValue()).booleanValue()) {
                float yaw = CoordinatesModule.mc.field_1724.method_36454();
                lines.add(String.format("Facing: %s (%.1f\u00b0)", this.getCardinalDirection(), Float.valueOf(this.wrapDegrees(yaw))));
            }
            if (((Boolean)this.showDimension.getValue()).booleanValue()) {
                lines.add("Dimension: " + this.getDimensionName());
            }
            if (((Boolean)this.showBiome.getValue()).booleanValue()) {
                lines.add("Biome: " + this.getBiomeName());
            }
            class_2338 blockPos = CoordinatesModule.mc.field_1724.method_24515();
            lines.add(String.format("Block: %d, %d, %d", blockPos.method_10263(), blockPos.method_10264(), blockPos.method_10260()));
        }
        return lines.toArray(new String[0]);
    }

    private int getLineColor(int lineIndex) {
        if (this.colorMode.is("axis")) {
            if (this.displayMode.is("detailed") && lineIndex < 3) {
                return switch (lineIndex) {
                    case 0 -> -43691;
                    case 1 -> -11141291;
                    case 2 -> -11184641;
                    default -> this.textColor.getRGB();
                };
            }
            return this.textColor.getRGB();
        }
        return this.textColor.getRGB();
    }

    private String getCardinalDirection() {
        if (CoordinatesModule.mc.field_1724 == null) {
            return "?";
        }
        float yaw = this.wrapDegrees(CoordinatesModule.mc.field_1724.method_36454());
        if ((double)yaw >= -22.5 && (double)yaw < 22.5) {
            return "S";
        }
        if ((double)yaw >= 22.5 && (double)yaw < 67.5) {
            return "SW";
        }
        if ((double)yaw >= 67.5 && (double)yaw < 112.5) {
            return "W";
        }
        if ((double)yaw >= 112.5 && (double)yaw < 157.5) {
            return "NW";
        }
        if ((double)yaw >= 157.5 || (double)yaw < -157.5) {
            return "N";
        }
        if ((double)yaw >= -157.5 && (double)yaw < -112.5) {
            return "NE";
        }
        if ((double)yaw >= -112.5 && (double)yaw < -67.5) {
            return "E";
        }
        if ((double)yaw >= -67.5 && (double)yaw < -22.5) {
            return "SE";
        }
        return "?";
    }

    private float wrapDegrees(float degrees) {
        if ((degrees %= 360.0f) >= 180.0f) {
            degrees -= 360.0f;
        }
        if (degrees < -180.0f) {
            degrees += 360.0f;
        }
        return degrees;
    }

    private String getDimensionName() {
        String dimension;
        if (CoordinatesModule.mc.field_1687 == null) {
            return "Unknown";
        }
        return switch (dimension = CoordinatesModule.mc.field_1687.method_27983().method_29177().method_12832()) {
            case "overworld" -> "Overworld";
            case "the_nether" -> "Nether";
            case "the_end" -> "End";
            default -> dimension;
        };
    }

    private String getBiomeName() {
        if (CoordinatesModule.mc.field_1687 == null || CoordinatesModule.mc.field_1724 == null) {
            return "Unknown";
        }
        class_2338 pos = CoordinatesModule.mc.field_1724.method_24515();
        class_6880 biomeHolder = CoordinatesModule.mc.field_1687.method_23753(pos);
        String biomePath = (String)biomeHolder.method_40230().map(key -> ((class_5321)key).method_29177().method_12832()).orElse("unknown");
        return this.prettifyName(biomePath);
    }

    private String prettifyName(String name) {
        String[] parts = name.split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
        }
        return result.toString().trim();
    }

    @Override
    public int getHudWidth() {
        if (CoordinatesModule.mc.field_1724 == null) {
            return 120;
        }
        double playerX = CoordinatesModule.mc.field_1724.method_23317();
        double playerY = CoordinatesModule.mc.field_1724.method_23318();
        double playerZ = CoordinatesModule.mc.field_1724.method_23321();
        String[] lines = this.buildDisplayLines(playerX, playerY, playerZ);
        int maxWidth = 0;
        for (String line : lines) {
            int lineWidth = CoordinatesModule.mc.field_1772.method_1727(line.replaceAll("\u00a7.", ""));
            if (lineWidth <= maxWidth) continue;
            maxWidth = lineWidth;
        }
        return maxWidth + 10;
    }

    @Override
    public int getHudHeight() {
        if (CoordinatesModule.mc.field_1724 == null) {
            return 33;
        }
        double playerX = CoordinatesModule.mc.field_1724.method_23317();
        double playerY = CoordinatesModule.mc.field_1724.method_23318();
        double playerZ = CoordinatesModule.mc.field_1724.method_23321();
        String[] lines = this.buildDisplayLines(playerX, playerY, playerZ);
        return lines.length * 11 + 6;
    }
}

