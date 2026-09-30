/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_2960
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 */
package net.tyxen.hud.gui;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.gui.DisplaySpace;
import net.tyxen.hud.modules.Module;
import net.minecraft.class_2960;
import net.minecraft.class_327;
import net.minecraft.class_332;

@Environment(value=EnvType.CLIENT)
public final class TyxenUI {
    public static final int OVERLAY = -1207959552;
    public static final int PANEL = -234156528;
    public static final int PANEL_SOFT = -435219433;
    public static final int CARD = -653586413;
    public static final int CARD_HOVER = -434955745;
    public static final int BUTTON = -435153640;
    public static final int BUTTON_HOVER = -266722777;
    public static final int RED = -14498466;
    public static final int RED_HOVER = -11870592;
    public static final int GREEN = -15511009;
    public static final int GREEN_HOVER = -15243738;
    public static final int GREEN_TEXT = -8658034;
    public static final int BORDER = 1143616571;
    public static final int TEXT = -723724;
    public static final int TEXT_SOFT = -4671304;
    public static final int TEXT_MUTED = -7303024;
    public static final int TEXT_DIM = -9934744;
    public static final int WHITE_ICON = -1;
    public static final int HUD_SURFACE = -1978658025;
    public static final int HUD_SURFACE_STRONG = -1475275496;
    public static final int HUD_BORDER = 1725422816;
    public static final int HUD_BORDER_SOFT = 1154997472;
    public static final int HUD_TEXT = -1;
    public static final int HUD_TEXT_MUTED = -3025448;
    private static final Map<String, String> ICONS = new HashMap<String, String>();

    private TyxenUI() {
    }

    private static void put(String moduleName, String fileName) {
        ICONS.put(moduleName, fileName);
    }

    public static class_2960 icon(Module module) {
        String file = ICONS.getOrDefault(module.getName().toLowerCase(Locale.ROOT), "ui-scaling.png");
        return DisplaySpace.texture(class_2960.method_60655((String)"tyxen", (String)("textures/gui/icons/" + file)));
    }

    public static int withAlpha(int color, int alpha) {
        return color & 0xFFFFFF | (alpha & 0xFF) << 24;
    }

    public static int blend(int a, int b, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        int aa = a >> 24 & 0xFF;
        int ar = a >> 16 & 0xFF;
        int ag = a >> 8 & 0xFF;
        int ab = a & 0xFF;
        int ba = b >> 24 & 0xFF;
        int br = b >> 16 & 0xFF;
        int bg = b >> 8 & 0xFF;
        int bb = b & 0xFF;
        return (int)((float)aa + (float)(ba - aa) * t) << 24 | (int)((float)ar + (float)(br - ar) * t) << 16 | (int)((float)ag + (float)(bg - ag) * t) << 8 | (int)((float)ab + (float)(bb - ab) * t);
    }

    public static void rect(class_332 graphics, int x, int y, int w, int h, int color) {
        graphics.method_25294(x, y, x + w, y + h, color);
    }

    public static void roundedRect(class_332 graphics, int x, int y, int w, int h, int radius, int color) {
        int r = Math.max(0, Math.min(radius, Math.min(w, h) / 2));
        graphics.method_25294(x + r, y, x + w - r, y + h, color);
        graphics.method_25294(x, y + r, x + w, y + h - r, color);
        for (int i = 0; i < r; ++i) {
            int dx = r - i;
            graphics.method_25294(x + dx, y + i, x + w - dx, y + i + 1, color);
            graphics.method_25294(x + dx, y + h - i - 1, x + w - dx, y + h - i, color);
        }
    }

    public static void outline(class_332 graphics, int x, int y, int w, int h, int color) {
        graphics.method_25294(x, y, x + w, y + 1, color);
        graphics.method_25294(x, y + h - 1, x + w, y + h, color);
        graphics.method_25294(x, y, x + 1, y + h, color);
        graphics.method_25294(x + w - 1, y, x + w, y + h, color);
    }

    public static void roundedOutline(class_332 graphics, int x, int y, int w, int h, int radius, int color) {
        int r = Math.max(0, Math.min(radius, Math.min(w, h) / 2));
        graphics.method_25294(x + r, y, x + w - r, y + 1, color);
        graphics.method_25294(x + r, y + h - 1, x + w - r, y + h, color);
        graphics.method_25294(x, y + r, x + 1, y + h - r, color);
        graphics.method_25294(x + w - 1, y + r, x + w, y + h - r, color);
        for (int i = 0; i < r; ++i) {
            int dx = r - i;
            graphics.method_25294(x + dx, y + i, x + dx + 1, y + i + 1, color);
            graphics.method_25294(x + w - dx - 1, y + i, x + w - dx, y + i + 1, color);
            graphics.method_25294(x + dx, y + h - i - 1, x + dx + 1, y + h - i, color);
            graphics.method_25294(x + w - dx - 1, y + h - i - 1, x + w - dx, y + h - i, color);
        }
    }

    public static void borderedRoundedRect(class_332 graphics, int x, int y, int w, int h, int radius, int fillColor, int borderColor) {
        TyxenUI.roundedRect(graphics, x, y, w, h, radius, borderColor);
        if (w > 2 && h > 2) {
            TyxenUI.roundedRect(graphics, x + 1, y + 1, w - 2, h - 2, Math.max(0, radius - 1), fillColor);
        }
    }

    public static void hudPanel(class_332 graphics, int x, int y, int w, int h) {
        TyxenUI.hudPanel(graphics, x, y, w, h, 138);
    }

    public static void hudPanel(class_332 graphics, int x, int y, int w, int h, int opacity) {
        int safeW = Math.max(1, w);
        int safeH = Math.max(1, h);
        int clampedOpacity = Math.max(0, Math.min(255, opacity));
        graphics.method_25294(x, y, x + safeW, y + safeH, TyxenUI.withAlpha(-1978658025, clampedOpacity));
        TyxenUI.outline(graphics, x, y, safeW, safeH, TyxenUI.withAlpha(1154997472, Math.min(96, clampedOpacity)));
    }

    public static void hudPanelStrong(class_332 graphics, int x, int y, int w, int h) {
        int safeW = Math.max(1, w);
        int safeH = Math.max(1, h);
        graphics.method_25294(x, y, x + safeW, y + safeH, -1475275496);
        TyxenUI.outline(graphics, x, y, safeW, safeH, 1725422816);
    }

    public static void hudText(class_332 graphics, class_327 font, String text, int x, int y, int color, boolean shadow) {
        TyxenUI.hudText(graphics, font, text, x, y, color, shadow, 138);
    }

    public static void hudText(class_332 graphics, class_327 font, String text, int x, int y, int color, boolean shadow, int opacity) {
        int cleanWidth = font.method_1727(text.replaceAll("\u00a7.", ""));
        TyxenUI.hudPanel(graphics, x - 5, y - 4, cleanWidth + 10, 17, opacity);
        graphics.method_51433(font, text, x, y, color, shadow);
    }

    public static void hudTwoLine(class_332 graphics, class_327 font, String primary, String secondary, int x, int y, int primaryColor, int secondaryColor) {
        TyxenUI.hudTwoLine(graphics, font, primary, secondary, x, y, primaryColor, secondaryColor, 138);
    }

    public static void hudTwoLine(class_332 graphics, class_327 font, String primary, String secondary, int x, int y, int primaryColor, int secondaryColor, int opacity) {
        int width = Math.max(font.method_1727(primary), font.method_1727(secondary));
        TyxenUI.hudPanel(graphics, x - 5, y - 4, width + 10, 28, opacity);
        graphics.method_51433(font, primary, x, y, primaryColor, true);
        graphics.method_51433(font, secondary, x, y + 11, secondaryColor, true);
    }

    static {
        TyxenUI.put("armorhud", "armor-status.png");
        TyxenUI.put("arraylist", "tablist.png");
        TyxenUI.put("autogg", "toast-cpmtrpl.png");
        TyxenUI.put("autotext", "autotext.png");
        TyxenUI.put("biome", "horses.png");
        TyxenUI.put("block overlay", "block-overlay-.png");
        TyxenUI.put("chattimestamps", "custom-chat.png");
        TyxenUI.put("clock", "stopwatch.png");
        TyxenUI.put("combocounter", "combo-display.png");
        TyxenUI.put("coordinates", "coordinates.png");
        TyxenUI.put("cpscounter", "cps.png");
        TyxenUI.put("crosshair", "crosshair.png");
        TyxenUI.put("damageindicator", "damage-indicator.png");
        TyxenUI.put("daycounter", "playtime.png");
        TyxenUI.put("directionhud", "direction-.png");
        TyxenUI.put("fps", "fps.png");
        TyxenUI.put("fullbright", "brightness.png");
        TyxenUI.put("guisettings", "ui-scaling.png");
        TyxenUI.put("hitbox", "hitbox.png");
        TyxenUI.put("keystrokes", "keystokes.png");
        TyxenUI.put("memory", "system-resources.png");
        TyxenUI.put("motion blur", "motion-blur.png");
        TyxenUI.put("nametagicon", "playermodel.png");
        TyxenUI.put("nobossbar", "hearts.png");
        TyxenUI.put("nohurtcam", "hit-indicator.png");
        TyxenUI.put("notifications", "toast-cpmtrpl.png");
        TyxenUI.put("packdisplay", "pack-display.png");
        TyxenUI.put("particles", "color-saturarion.png");
        TyxenUI.put("pingdisplay", "ping.png");
        TyxenUI.put("pingoverlay", "ping.png");
        TyxenUI.put("potionhud", "potion.png");
        TyxenUI.put("saturation", "saturation.png");
        TyxenUI.put("scoreboardmod", "scoreboard.png");
        TyxenUI.put("serverinfo", "server-adress.png");
        TyxenUI.put("speedhud", "speed-meter.png");
        TyxenUI.put("sprint", "toggle-sprint.png");
        TyxenUI.put("timechanger", "nightmode.png");
        TyxenUI.put("togglesneak", "toggle-sprint.png");
        TyxenUI.put("toolwarning", "tooltips.png");
        TyxenUI.put("waypoints", "direction-.png");
        TyxenUI.put("zoom", "fov.png");
    }
}

