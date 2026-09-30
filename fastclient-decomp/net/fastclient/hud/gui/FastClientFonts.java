/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_11719
 *  net.minecraft.class_11719$class_11721
 *  net.minecraft.class_2561
 *  net.minecraft.class_2583
 *  net.minecraft.class_2960
 */
package net.fastclient.hud.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11719;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;

@Environment(value=EnvType.CLIENT)
public final class FastClientFonts {
    private static final Typeface ACTIVE_TYPEFACE = Typeface.MINECRAFT_DEFAULT;
    private static final float FIXED_MINECRAFT_UI_SCALE = 2.0f;
    private static final class_11719.class_11721 INTER_SEMIBOLD = new class_11719.class_11721(class_2960.method_60655((String)"fastclient-hud", (String)"inter-semibold"));
    private static final class_11719.class_11721 INTER_BOLD = new class_11719.class_11721(class_2960.method_60655((String)"fastclient-hud", (String)"inter-bold"));
    private static final class_11719.class_11721 INTER_BOLD_14 = new class_11719.class_11721(class_2960.method_60655((String)"fastclient-hud", (String)"inter-bold-14"));
    private static final class_11719.class_11721 MINECRAFT_UNIFORM = new class_11719.class_11721(class_2960.method_60655((String)"minecraft", (String)"uniform"));
    private static final class_11719.class_11721 MATERIAL_SYMBOLS_ROUNDED = new class_11719.class_11721(class_2960.method_60655((String)"fastclient-hud", (String)"material-symbols-rounded"));
    private static final class_11719.class_11721 MATERIAL_SYMBOLS_ROUNDED_FILLED = new class_11719.class_11721(class_2960.method_60655((String)"fastclient-hud", (String)"material-symbols-rounded-filled"));

    private FastClientFonts() {
    }

    public static class_2561 body(String text) {
        return FastClientFonts.styled(text, TextRole.BODY);
    }

    public static class_2561 strong(String text) {
        return FastClientFonts.styled(text, TextRole.STRONG);
    }

    public static class_2561 title(String text) {
        return FastClientFonts.styled(text, TextRole.TITLE);
    }

    public static class_2561 moduleName(String text) {
        return FastClientFonts.body(text);
    }

    public static Typeface activeTypeface() {
        return ACTIVE_TYPEFACE;
    }

    public static float bodyScale() {
        return FastClientFonts.configuredUiScale();
    }

    public static float strongScale() {
        return FastClientFonts.configuredUiScale();
    }

    public static float titleScale() {
        return FastClientFonts.configuredUiScale();
    }

    private static float configuredUiScale() {
        return ACTIVE_TYPEFACE == Typeface.MINECRAFT_DEFAULT ? 2.0f : 1.0f;
    }

    private static class_2561 styled(String text, TextRole role) {
        class_2583 style;
        if (ACTIVE_TYPEFACE == Typeface.MINECRAFT_DEFAULT) {
            style = class_2583.field_24360;
        } else if (ACTIVE_TYPEFACE == Typeface.MINECRAFT_UNIFORM) {
            style = class_2583.field_24360.method_27704((class_11719)MINECRAFT_UNIFORM);
            if (role != TextRole.BODY) {
                style = style.method_10982(Boolean.valueOf(true));
            }
        } else {
            class_11719.class_11721 font = switch (role.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> INTER_SEMIBOLD;
                case 1 -> INTER_BOLD;
                case 2 -> INTER_BOLD_14;
            };
            style = class_2583.field_24360.method_27704((class_11719)font);
        }
        return class_2561.method_43470((String)text).method_27696(style);
    }

    public static class_2561 materialSymbol(String symbol) {
        return class_2561.method_43470((String)symbol).method_27696(class_2583.field_24360.method_27704((class_11719)MATERIAL_SYMBOLS_ROUNDED));
    }

    public static class_2561 filledMaterialSymbol(String symbol) {
        return class_2561.method_43470((String)symbol).method_27696(class_2583.field_24360.method_27704((class_11719)MATERIAL_SYMBOLS_ROUNDED_FILLED));
    }

    public static class_2561 materialSymbolLabel(String symbol, String label) {
        return class_2561.method_43473().method_10852(FastClientFonts.materialSymbol(symbol)).method_10852((class_2561)class_2561.method_43470((String)(" " + label)));
    }

    public static class_2561 filledMaterialSymbolLabel(String symbol, String label) {
        return class_2561.method_43473().method_10852(FastClientFonts.filledMaterialSymbol(symbol)).method_10852((class_2561)class_2561.method_43470((String)(" " + label)));
    }

    @Environment(value=EnvType.CLIENT)
    private static enum TextRole {
        BODY,
        STRONG,
        TITLE;

    }

    @Environment(value=EnvType.CLIENT)
    public static enum Typeface {
        INTER,
        MINECRAFT_DEFAULT,
        MINECRAFT_UNIFORM;

    }

    @Environment(value=EnvType.CLIENT)
    public static final class Symbols {
        public static final String ALIGN_HORIZONTAL_CENTER = "\ue00f";
        public static final String ALIGN_VERTICAL_CENTER = "\ue011";
        public static final String APPS = "\ue5c3";
        public static final String ARROW_BACK = "\ue5c4";
        public static final String CHECKROOM = "\uf19e";
        public static final String CLOSE = "\ue5cd";
        public static final String DASHBOARD = "\ue871";
        public static final String DASHBOARD_CUSTOMIZE = "\ue99b";
        public static final String DELETE = "\ue872";
        public static final String DIAMOND = "\uead5";
        public static final String DESKTOP_WINDOWS = "\ue30c";
        public static final String DIRECTIONS_RUN = "\ue566";
        public static final String FAVORITE = "\ue87e";
        public static final String FOLDER_OPEN = "\ue2c8";
        public static final String FORUM = "\ue8af";
        public static final String GRID_VIEW = "\ue9b0";
        public static final String GROUP = "\uea21";
        public static final String HANDYMAN = "\uf10b";
        public static final String LOGOUT = "\ue9ba";
        public static final String MANAGE_ACCOUNTS = "\uf02e";
        public static final String PERSON = "\uf0d3";
        public static final String PHOTO_CAMERA = "\ue412";
        public static final String REPORT = "\ue160";
        public static final String REMOVE = "\ue15b";
        public static final String RESTART_ALT = "\uf053";
        public static final String SEARCH = "\uef7a";
        public static final String SETTINGS = "\ue8b8";
        public static final String SPACE_DASHBOARD = "\ue66b";
        public static final String SPEED = "\ue9e4";
        public static final String BAR_CHART = "\ue26b";
        public static final String EMOJI_EVENTS = "\uea23";
        public static final String STOREFRONT = "\uea12";
        public static final String VISIBILITY = "\ue8f4";

        private Symbols() {
        }
    }
}

