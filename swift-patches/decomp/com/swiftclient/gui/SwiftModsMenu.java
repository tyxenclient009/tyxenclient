/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.class_10799
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_156
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  net.minecraft.class_5348
 *  org.joml.Matrix3x2fStack
 */
package com.swiftclient.gui;

import com.swiftclient.config.ClientConfig;
import com.swiftclient.config.Theme;
import com.swiftclient.gui.ModuleIcons;
import com.swiftclient.gui.Sprites;
import com.swiftclient.gui.SwiftHudEditor;
import com.swiftclient.gui.SwiftModuleSettings;
import com.swiftclient.modules.Module;
import com.swiftclient.modules.ModuleManager;
import com.swiftclient.util.SwiftText;
import com.swiftclient.util.Ui;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_10799;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_5348;
import org.joml.Matrix3x2fStack;

public final class SwiftModsMenu
extends class_437 {
    public static final String TAB_MODS = "MODS";
    public static final String TAB_SETTINGS = "SETTINGS";
    public static final String TAB_WAYPOINTS = "WAYPOINTS";
    private static final String[] FILTERS = new String[]{"ALL", "NEW", "HUD", "RENDER", "PLAYER", "PERFORMANCE"};
    private static final int GREEN = -13722013;
    private static final int OFF = -14276806;
    private static final class_2960 TILE_GEAR = class_2960.method_60655((String)"swiftclient", (String)"textures/gui/icons/tile_settings_10.png");
    private static final int PANEL = -401666283;
    private static final int SIDE_BG = 2014452264;
    private static final int PANEL_TOP = 2014715696;
    private static final int PANEL_BOT = 1711803672;
    private static final int CARD_TOP = -1428955414;
    private static final int CARD_BOT = -1884897838;
    private static final int CARD_INK = -14998480;
    private static final int CARD_SUB = -13747897;
    private static final int DIM = 0x28000000;
    private static final int[] SHEEN = new int[]{234677484, 232187134, 233232126, 232193757, 234804968, 232187134};
    private static final int CARD = -1072360163;
    private final String startTab;
    private String tab;
    private int filter = 0;
    private int scroll;
    private int hoverCard = -1;
    private String query = "";
    private long openedAt;
    private float anim;
    private static final int HEADER = 30;
    private static final int FOOTER = 22;
    private static final int SIDE = 148;
    private static final int PAD = 10;
    private static final int GAP = 8;
    private static final int COLS = 3;
    private static final int CARD_H = 102;
    private static final int CHIP_H = 16;

    private static void iridescent(class_332 class_3322, int n, int n2, int n3, int n4) {
        int n5 = 8;
        int n6 = 0;
        for (int i = n2; i < n2 + n4; i += n5) {
            int n7 = Math.min(n5, n2 + n4 - i);
            class_3322.method_25294(n, i, n + n3, i + n7, SHEEN[n6++ % SHEEN.length]);
        }
    }

    public SwiftModsMenu() {
        this(TAB_MODS);
    }

    public SwiftModsMenu(String string) {
        super((class_2561)class_2561.method_43470((String)"Tyxen X Swift \u2014 Mods"));
        this.startTab = string;
        this.tab = string;
    }

    protected void method_25426() {
        this.openedAt = System.nanoTime();
        this.anim = 0.0f;
    }

    private static float ease(float f) {
        float f2 = 1.0f - f;
        return 1.0f - f2 * f2 * f2;
    }

    public boolean method_25421() {
        return false;
    }

    private int panelW() {
        return Math.min(880, this.field_22789 - 24);
    }

    private int panelH() {
        return Math.min(560, this.field_22790 - 24);
    }

    private int px() {
        return (this.field_22789 - this.panelW()) / 2;
    }

    private int py() {
        return (this.field_22790 - this.panelH()) / 2 + (int)((1.0f - this.anim) * 20.0f);
    }

    private List<Module> visible() {
        ArrayList<Module> arrayList = new ArrayList<Module>();
        String string = FILTERS[this.filter];
        for (Module module : ModuleManager.all()) {
            if (!module.name().toLowerCase(Locale.ROOT).contains(this.query)) continue;
            if (string.equals("ALL")) {
                arrayList.add(module);
                continue;
            }
            if (string.equals("NEW")) {
                if (!module.hasPosition()) continue;
                arrayList.add(module);
                continue;
            }
            if (!module.category().name().equals(string)) continue;
            arrayList.add(module);
        }
        return arrayList;
    }

    private int mainX() {
        return this.px() + 148;
    }

    private int mainW() {
        return this.panelW() - 148;
    }

    private int gridTop() {
        return this.py() + 30 + 8 + 16 + 8;
    }

    private int gridBottom() {
        return this.py() + this.panelH() - 22;
    }

    private float uiScale() {
        float f = Math.min((float)this.panelW() / 680.0f, (float)this.panelH() / 400.0f);
        if (f > 1.0f) {
            f = 1.0f;
        }
        return f;
    }

    private float pcx() {
        return (float)this.px() + (float)this.panelW() / 2.0f;
    }

    private float pcy() {
        return (float)this.py() + (float)this.panelH() / 2.0f;
    }

    private int unX(int n) {
        float f = this.uiScale();
        return Math.round(((float)n - this.pcx()) / f + this.pcx());
    }

    private int unY(int n) {
        float f = this.uiScale();
        return Math.round(((float)n - this.pcy()) / f + this.pcy());
    }

    private int presetRows() {
        return Math.max(1, Math.min(4, ClientConfig.presets().size()));
    }

    private int presetY(int n) {
        return this.py() + 44 + n * 16;
    }

    private int saveY() {
        return this.py() + 44 + this.presetRows() * 16 + 6;
    }

    private int divY() {
        return this.saveY() + 16 + 6;
    }

    private int catY() {
        return this.divY() + 6;
    }

    private int filterTop() {
        return this.catY() + 14;
    }

    private int filterBottomMax() {
        return this.py() + this.panelH() - 72 - 8;
    }

    private int filterH() {
        int n = this.filterBottomMax() - this.filterTop();
        int n2 = n / 6;
        if (n2 > 20) {
            n2 = 20;
        }
        if (n2 < 12) {
            n2 = 12;
        }
        return n2;
    }

    private int filterY(int n) {
        return this.filterTop() + n * this.filterH();
    }

    private int openFolderY() {
        return this.py() + this.panelH() - 72;
    }

    private int hudLayoutY() {
        return this.py() + this.panelH() - 44;
    }

    private static int mix(int n, int n2, float f) {
        int n3 = n >>> 24;
        int n4 = n >> 16 & 0xFF;
        int n5 = n >> 8 & 0xFF;
        int n6 = n & 0xFF;
        int n7 = n2 >>> 24;
        int n8 = n2 >> 16 & 0xFF;
        int n9 = n2 >> 8 & 0xFF;
        int n10 = n2 & 0xFF;
        int n11 = (int)((float)n3 + (float)(n7 - n3) * f);
        int n12 = (int)((float)n4 + (float)(n8 - n4) * f);
        int n13 = (int)((float)n5 + (float)(n9 - n5) * f);
        int n14 = (int)((float)n6 + (float)(n10 - n6) * f);
        return n11 << 24 | n12 << 16 | n13 << 8 | n14;
    }

    private static void vgrad(class_332 class_3322, int n, int n2, int n3, int n4, int n5, int n6) {
        int n7 = 8;
        for (int i = n2; i < n2 + n4; i += n7) {
            int n8 = Math.min(n7, n2 + n4 - i);
            float f = (float)(i - n2) / (float)Math.max(1, n4);
            int n9 = SwiftModsMenu.mix(n5, n6, f);
            class_3322.method_25294(n, i, n + n3, i + n8, n9);
        }
    }

    private void panelBg(class_332 class_3322, int n, int n2, int n3, int n4) {
        SwiftModsMenu.vgrad(class_3322, n, n2, n3, n4, 2014715696, 1711803672);
        SwiftModsMenu.iridescent(class_3322, n, n2, n3, n4);
        SwiftModsMenu.roundCut(class_3322, n, n2, n3, n4, 10, 0x28000000);
        class_3322.method_25294(n + 10, n2 + 1, n + n3 - 10, n2 + 2, Theme.accent() & 0xFFFFFF | 0x66000000);
    }

    private static void roundCut(class_332 class_3322, int n, int n2, int n3, int n4, int n5, int n6) {
        SwiftModsMenu.roundCut2(class_3322, n, n2, n3, n4, n5, n6, n6);
    }

    private static void roundCut2(class_332 class_3322, int n, int n2, int n3, int n4, int n5, int n6, int n7) {
        for (int i = 0; i < n5; ++i) {
            for (int j = 0; j < n5; ++j) {
                if (j * j + i * i < n5 * n5) continue;
                class_3322.method_25294(n + j, n2 + i, n + j + 1, n2 + i + 1, n6);
                class_3322.method_25294(n + n3 - 1 - j, n2 + i, n + n3 - j, n2 + i + 1, n6);
                class_3322.method_25294(n + j, n2 + n4 - 1 - i, n + j + 1, n2 + n4 - i, n7);
                class_3322.method_25294(n + n3 - 1 - j, n2 + n4 - 1 - i, n + n3 - j, n2 + n4 - i, n7);
            }
        }
    }

    private int panelGradAt(int n) {
        float f = (float)(n - this.py()) / (float)Math.max(1, this.panelH());
        if (f < 0.0f) {
            f = 0.0f;
        }
        if (f > 1.0f) {
            f = 1.0f;
        }
        return SwiftModsMenu.mix(2014715696, 1711803672, f);
    }

    private int cardW() {
        return (this.mainW() - 20 - 16) / 3;
    }

    private int gridRows() {
        return (this.visible().size() + 3 - 1) / 3;
    }

    private int maxScroll() {
        int n = this.gridRows() * 110 - 8;
        return Math.max(0, n - (this.gridBottom() - this.gridTop()));
    }

    private int cardX(int n) {
        return this.mainX() + 10 + n * (this.cardW() + 8);
    }

    private int cardY(int n) {
        return this.gridTop() - this.scroll + n * 110;
    }

    public boolean method_25402(class_11909 class_119092, boolean bl) {
        int n;
        if (class_119092.method_74245() != 0 && class_119092.method_74245() != 1) {
            return false;
        }
        boolean bl2 = class_119092.method_74245() == 0;
        int n2 = this.unX((int)class_119092.comp_4798());
        int n3 = this.unY((int)class_119092.comp_4799());
        int n4 = this.px();
        int n5 = this.py();
        int n6 = this.panelW();
        int n7 = this.panelH();
        if (bl2 && n2 >= n4 + n6 - 26 && n2 < n4 + n6 - 10 && n3 >= n5 + 7 && n3 < n5 + 23) {
            this.method_25419();
            return true;
        }
        int n8 = bl2 ? this.tabAt(n2, n3) : -1;
        int n9 = n8;
        if (n8 >= 0) {
            this.tab = n8 == 0 ? TAB_MODS : (n8 == 1 ? TAB_SETTINGS : TAB_WAYPOINTS);
            this.scroll = 0;
            return true;
        }
        if (bl2 && this.tab.equals(TAB_SETTINGS) && (n = this.swatchAt(n2, n3)) >= 0) {
            Theme.setAccent(Theme.ACCENTS[n]);
            return true;
        }
        if (this.tab.equals(TAB_MODS)) {
            int n10;
            int n11;
            List<String> list = ClientConfig.presets();
            int n12 = Math.min(4, list.size());
            for (n11 = 0; n11 < n12; ++n11) {
                n10 = this.presetY(n11);
                if (n2 < n4 + 10 || n2 >= n4 + 148 - 10 || n3 < n10 || n3 >= n10 + 16) continue;
                if (class_119092.method_74245() == 1) {
                    ClientConfig.deletePreset(list.get(n11));
                } else if (bl2 && ClientConfig.loadPreset(list.get(n11))) {
                    for (Module module : ModuleManager.all()) {
                        module.setEnabled(ClientConfig.enabled(module.name(), false));
                    }
                }
                return true;
            }
            if (bl2 && this.inSavePreset(n2, n3)) {
                ClientConfig.savePreset(ClientConfig.nextPresetName());
                return true;
            }
            if (bl2) {
                for (n11 = 0; n11 < FILTERS.length; ++n11) {
                    n10 = this.filterY(n11);
                    if (n2 < n4 + 10 || n2 >= n4 + 148 - 10 || n3 < n10 || n3 >= n10 + this.filterH()) continue;
                    this.filter = n11;
                    this.scroll = 0;
                    return true;
                }
            }
            int n13 = bl2 ? this.chipAt(n2, n3) : -1;
            int n14 = n13;
            if (n13 >= 0) {
                this.filter = n13;
                this.scroll = 0;
                return true;
            }
            List<Module> list2 = this.visible();
            int n15 = 3;
            int n16 = this.cardW();
            for (int i = 0; i < list2.size(); ++i) {
                int n17 = this.cardX(i % n15);
                int n18 = this.cardY(i / n15);
                if (n18 + 102 < this.gridTop() || n18 > this.gridBottom() || n2 < n17 || n2 >= n17 + n16 || n3 < n18 || n3 >= n18 + 102) continue;
                boolean bl3 = n3 >= n18 + 44 && n3 < n18 + 62;
                boolean bl4 = bl3;
                if (class_119092.method_74245() == 1 || bl3) {
                    this.field_22787.method_1507((class_437)new SwiftModuleSettings(list2.get(i), this));
                } else {
                    list2.get(i).toggle();
                }
                return true;
            }
            if (bl2 && this.inOpenFolder(n2, n3)) {
                try {
                    class_156.method_668().method_60932(FabricLoader.getInstance().getConfigDir());
                }
                catch (Exception exception) {
                    // empty catch block
                }
                return true;
            }
            if (bl2 && this.inHudLayout(n2, n3)) {
                this.field_22787.method_1507((class_437)new SwiftHudEditor(this));
                return true;
            }
        }
        return n2 >= n4 && n2 < n4 + n6 && n3 >= n5 && n3 < n5 + n7;
    }

    private int tabAt(int n, int n2) {
        int n3 = this.py();
        int n4 = this.px() + 148 + 12;
        String[] stringArray = new String[]{TAB_MODS, TAB_SETTINGS, TAB_WAYPOINTS};
        for (int i = 0; i < stringArray.length; ++i) {
            int n5 = this.field_22793.method_27525((class_5348)SwiftText.of(stringArray[i])) + 16;
            if (n >= n4 && n < n4 + n5 && n2 >= n3 + 7 && n2 < n3 + 23) {
                return i;
            }
            n4 += n5 + 4;
        }
        return -1;
    }

    public boolean method_25400(class_11905 class_119052) {
        if (!this.tab.equals(TAB_MODS) || !class_119052.method_74227()) {
            return false;
        }
        String string = class_119052.method_74226();
        if (string.length() != 1) {
            return false;
        }
        char c = string.charAt(0);
        if (c < ' ' || c > '~' || this.query.length() >= 24) {
            return true;
        }
        this.query = this.query + Character.toLowerCase(c);
        return true;
    }

    public boolean method_25404(class_11908 class_119082) {
        if (this.tab.equals(TAB_MODS) && class_119082.comp_4795() == 259 && !this.query.isEmpty()) {
            this.query = this.query.substring(0, this.query.length() - 1);
            return true;
        }
        return super.method_25404(class_119082);
    }

    private int chipAt(int n, int n2) {
        int n3 = this.py() + 30 + 8;
        int n4 = this.mainX() + 10;
        for (int i = 0; i < FILTERS.length; ++i) {
            int n5 = this.field_22793.method_27525((class_5348)SwiftText.of(FILTERS[i])) + 14;
            if (n >= n4 && n < n4 + n5 && n2 >= n3 && n2 < n3 + 16) {
                return i;
            }
            n4 += n5 + 6;
        }
        return -1;
    }

    private int swatchAt(int n, int n2) {
        int n3 = this.mainX() + 16;
        int n4 = this.py() + 30 + 52;
        if (n2 < n4 || n2 >= n4 + 16) {
            return -1;
        }
        int n5 = (n - n3) / 24;
        return n5 >= 0 && n5 < Theme.ACCENTS.length && n < n3 + n5 * 24 + 16 ? n5 : -1;
    }

    private boolean inSavePreset(int n, int n2) {
        int n3 = this.px();
        int n4 = this.saveY();
        return n >= n3 + 10 && n < n3 + 148 - 10 && n2 >= n4 && n2 < n4 + 16;
    }

    private boolean inOpenFolder(int n, int n2) {
        int n3 = this.px();
        int n4 = this.py();
        int n5 = this.panelH();
        return n >= n3 + 10 && n < n3 + 148 - 10 && n2 >= n4 + n5 - 72 && n2 < n4 + n5 - 48;
    }

    private boolean inHudLayout(int n, int n2) {
        int n3 = this.px();
        int n4 = this.py();
        int n5 = this.panelH();
        return n >= n3 + 10 && n < n3 + 148 - 10 && n2 >= n4 + n5 - 44 && n2 < n4 + n5 - 20;
    }

    public boolean method_25401(double d, double d2, double d3, double d4) {
        if (!this.tab.equals(TAB_MODS)) {
            return false;
        }
        int n = this.px();
        int n2 = this.py();
        int n3 = this.panelW();
        int n4 = this.panelH();
        if (d < (double)n || d >= (double)(n + n3) || d2 < (double)n2 || d2 >= (double)(n2 + n4)) {
            return false;
        }
        int n5 = this.scroll;
        this.scroll = (int)Math.max(0.0, Math.min((double)this.maxScroll(), (double)this.scroll - d4 * 26.0));
        return this.scroll != n5;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void method_25394(class_332 class_3322, int n, int n2, float f) {
        this.anim = SwiftModsMenu.ease(Math.min(1.0f, (float)(System.nanoTime() - this.openedAt) / 1.5E8f));
        try {
            class_3322.method_71278();
        }
        catch (IllegalStateException illegalStateException) {
            // empty catch block
        }
        class_3322.method_25294(0, 0, this.field_22789, this.field_22790, 0x28000000);
        int n3 = this.px();
        int n4 = this.py();
        int n5 = this.panelW();
        int n6 = this.panelH();
        this.panelBg(class_3322, n3, n4, n5, n6);
        int n7 = n;
        int n8 = n2;
        n = this.unX(n);
        n2 = this.unY(n2);
        float f2 = this.uiScale();
        float f3 = this.pcx();
        float f4 = this.pcy();
        Matrix3x2fStack matrix3x2fStack = class_3322.method_51448();
        matrix3x2fStack.pushMatrix();
        matrix3x2fStack.translate(f3, f4);
        matrix3x2fStack.scale(f2, f2);
        matrix3x2fStack.translate(-f3, -f4);
        try {
            this.drawHeader(class_3322, n, n2);
            if (this.tab.equals(TAB_MODS)) {
                this.drawSidebar(class_3322, n, n2);
                this.drawMods(class_3322, n, n2);
            } else if (this.tab.equals(TAB_SETTINGS)) {
                this.drawSettings(class_3322, n, n2);
            } else {
                this.drawWaypoints(class_3322);
            }
            this.drawFooter(class_3322);
        }
        finally {
            matrix3x2fStack.popMatrix();
        }
        super.method_25394(class_3322, n7, n8, f);
    }

    private void drawHeader(class_332 class_3322, int n, int n2) {
        int n3;
        int n4 = this.px();
        int n5 = this.py();
        int n6 = this.panelW();
        Ui.crescent(class_3322, n4 + 22, n5 + 15, 9, -723718);
        class_3322.method_27535(this.field_22793, SwiftText.bold("TYXEN X SWIFT"), n4 + 36, n5 + 11, -855308);
        String[] stringArray = new String[]{TAB_MODS, TAB_SETTINGS, TAB_WAYPOINTS};
        int n7 = n4 + 148 + 12;
        for (n3 = 0; n3 < stringArray.length; ++n3) {
            int n8 = this.field_22793.method_27525((class_5348)SwiftText.of(stringArray[n3])) + 16;
            boolean bl = this.tab.equals(stringArray[n3]);
            boolean bl2 = n >= n7 && n < n7 + n8 && n2 >= n5 + 7 && n2 < n5 + 23;
            boolean bl3 = bl2;
            if (bl) {
                Ui.rounded(class_3322, n7, n5 + 5, n8, 20, 9, -14771216, this.panelGradAt(n5 + 15));
            }
            class_3322.method_27534(this.field_22793, bl ? SwiftText.bold(stringArray[n3]) : SwiftText.of(stringArray[n3]), n7 + n8 / 2, n5 + 11, bl ? -1 : -7697506);
            if (!bl && bl2) {
                class_3322.method_25294(n7 + 2, n5 + 23, n7 + n8 - 2, n5 + 24, 0x44FFFFFF);
            }
            n7 += n8 + 4;
        }
        n3 = n >= n4 + n6 - 26 && n < n4 + n6 - 10 && n2 >= n5 + 7 && n2 < n5 + 23 ? 1 : 0;
        Ui.sprite(class_3322, n3 != 0 ? Sprites.TILE16_HOVER : Sprites.TILE16, n4 + n6 - 26, n5 + 7, 16, 16);
        class_3322.method_27534(this.field_22793, SwiftText.of("x"), n4 + n6 - 18, n5 + 11, n3 != 0 ? -1 : -7697506);
        class_3322.method_25294(n4, n5 + 30 - 1, n4 + n6, n5 + 30, -13027015);
    }

    private void drawSidebar(class_332 class_3322, int n, int n2) {
        boolean bl;
        int n3;
        int n4;
        int n5;
        int n6;
        int n7 = this.px();
        int n8 = this.py();
        int n9 = this.panelH();
        class_3322.method_25294(n7, n8 + 30, n7 + 148 - 1, n8 + n9 - 22, 2014452264);
        class_3322.method_25294(n7 + 148 - 1, n8 + 30, n7 + 148, n8 + n9 - 22, -13027015);
        class_3322.method_27535(this.field_22793, SwiftText.of("CONFIGS"), n7 + 12, n8 + 32, -10592396);
        List<String> list = ClientConfig.presets();
        int n10 = Math.min(4, list.size());
        for (n6 = 0; n6 < n10; ++n6) {
            n5 = this.presetY(n6);
            n4 = n >= n7 + 10 && n < n7 + 148 - 10 && n2 >= n5 && n2 < n5 + 16 ? 1 : 0;
            n3 = n4;
            if (n4 != 0) {
                Ui.rounded(class_3322, n7 + 8, n5, 132, 16, 4, -15461349, 2014452264);
            }
            class_3322.method_25294(n7 + 8, n5, n7 + 10, n5 + 16, n4 != 0 ? Theme.accent() : -13421244);
            class_3322.method_27535(this.field_22793, SwiftText.of(list.get(n6)), n7 + 16, n5 + 4, n4 != 0 ? -1 : -7697506);
        }
        if (list.isEmpty()) {
            class_3322.method_27535(this.field_22793, SwiftText.of("No presets yet"), n7 + 16, this.presetY(0) + 4, -7697776);
        }
        n6 = this.saveY();
        n4 = this.inSavePreset(n, n2);
        Ui.sprite(class_3322, n4 != 0 ? Sprites.BTN_BLUE_HOVER : Sprites.BTN_BLUE, n7 + 10, n6, 128, 16);
        class_3322.method_27534(this.field_22793, SwiftText.of("+ SAVE CURRENT"), n7 + 74, n6 + 4, -1);
        class_3322.method_25294(n7 + 12, this.divY(), n7 + 148 - 12, this.divY() + 1, -13027015);
        class_3322.method_27535(this.field_22793, SwiftText.of("CATEGORIES"), n7 + 12, this.catY(), -10592396);
        n5 = this.filterH();
        for (n3 = 0; n3 < FILTERS.length; ++n3) {
            int n11 = this.filterY(n3);
            boolean bl2 = this.filter == n3;
            bl = n >= n7 + 10 && n < n7 + 148 - 10 && n2 >= n11 && n2 < n11 + n5;
            boolean bl3 = bl;
            if (bl2) {
                Ui.rounded(class_3322, n7 + 8, n11, 132, n5, 4, -14771216, 2014452264);
            } else if (bl) {
                Ui.rounded(class_3322, n7 + 8, n11, 132, n5, 4, -871099365, 2014452264);
            }
            class_3322.method_27535(this.field_22793, SwiftText.of(FILTERS[n3]), n7 + 16, n11 + (n5 - 8) / 2, bl2 ? -1 : -7697506);
        }
        n3 = this.inOpenFolder(n, n2) ? 1 : 0;
        Ui.sprite(class_3322, n3 != 0 ? Sprites.BTN_HOVER : Sprites.BTN, n7 + 10, this.openFolderY(), 128, 24);
        class_3322.method_27534(this.field_22793, SwiftText.of("OPEN FOLDER"), n7 + 74, this.openFolderY() + 8, -4605239);
        bl = this.inHudLayout(n, n2);
        Ui.sprite(class_3322, bl ? Sprites.BTN_BLUE_HOVER : Sprites.BTN_BLUE, n7 + 10, this.hudLayoutY(), 128, 24);
        class_3322.method_27534(this.field_22793, SwiftText.of("EDIT HUD LAYOUT"), n7 + 74, this.hudLayoutY() + 8, -1);
    }

    private void drawMods(class_332 class_3322, int n, int n2) {
        int n3;
        int n4;
        int n5;
        int n6;
        int n7;
        int n8;
        int n9;
        int n10 = this.py() + 30 + 8;
        int n11 = this.mainX() + 10;
        for (n9 = 0; n9 < FILTERS.length; ++n9) {
            int n12 = this.field_22793.method_27525((class_5348)SwiftText.of(FILTERS[n9])) + 14;
            n8 = this.filter == n9 ? 1 : 0;
            n7 = n8;
            if (n8 != 0) {
                Ui.sprite(class_3322, Sprites.chipAccent(Theme.accIndex()), n11, n10, n12, 16);
            } else {
                Ui.sprite(class_3322, Sprites.CHIP_DARK, n11, n10, n12, 16);
            }
            class_3322.method_27534(this.field_22793, SwiftText.of(FILTERS[n9]), n11 + n12 / 2, n10 + 4, n8 != 0 ? -15856108 : -4605239);
            n11 += n12 + 6;
        }
        n9 = this.mainX() + this.mainW() - 8;
        n8 = n9 - 150;
        if (n8 > n11 + 4) {
            Ui.sprite(class_3322, Sprites.CHIP_DARK, n8, n10, n9 - n8, 16);
            Ui.ring(class_3322, n8 + 10, n10 + 8, 3, 1, -10592396);
            class_3322.method_25294(n8 + 12, n10 + 10, n8 + 15, n10 + 13, -10592396);
            String string = this.query.isEmpty() ? "Search" : this.query;
            class_3322.method_27535(this.field_22793, SwiftText.of(string), n8 + 19, n10 + 4, this.query.isEmpty() ? -10592396 : -855305);
            if (System.currentTimeMillis() / 500L % 2L == 0L) {
                n6 = this.field_22793.method_27525((class_5348)SwiftText.of(string));
                class_3322.method_25294(n8 + 20 + n6, n10 + 4, n8 + 21 + n6, n10 + 12, -4605239);
            }
        }
        List<Module> list = this.visible();
        this.hoverCard = -1;
        n6 = this.cardW();
        n7 = 3;
        int n13 = this.gridTop();
        int n14 = this.gridBottom();
        class_3322.method_44379(this.mainX(), n13, this.mainX() + this.mainW() - 8, n14);
        for (n5 = 0; n5 < list.size(); ++n5) {
            Module module = list.get(n5);
            n4 = this.cardX(n5 % 3);
            n3 = this.cardY(n5 / 3);
            if (n3 + 102 < n13 || n3 > n14) continue;
            boolean bl = n >= n4 && n < n4 + n6 && n2 >= n3 && n2 < n3 + 102 && n3 >= n13 - 2 && n3 + 102 <= n14 + 2;
            boolean bl2 = bl;
            if (bl) {
                this.hoverCard = n5;
            }
            this.drawCard(class_3322, module, n4, n3, n6, bl);
        }
        class_3322.method_44380();
        n5 = this.maxScroll();
        if (n5 > 0) {
            float f = n14 - n13;
            int n15 = Math.max(24, (int)(f * (f / ((float)list.size() / 3.0f * 110.0f))));
            n4 = n13 + (int)((f - (float)n15) * ((float)this.scroll / (float)n5));
            n3 = this.mainX() + this.mainW() - 7;
            class_3322.method_25294(n3, n13, n3 + 3, n14, -1726276568);
            class_3322.method_25294(n3, n4, n3 + 3, n4 + n15, Theme.accent());
        }
    }

    private void drawCard(class_332 class_3322, Module module, int n, int n2, int n3, boolean bl) {
        SwiftModsMenu.vgrad(class_3322, n, n2, n3, 102, -1428955414, -1884897838);
        SwiftModsMenu.iridescent(class_3322, n, n2, n3, 102);
        if (bl) {
            class_3322.method_25294(n + 1, n2 + 1, n + n3 - 1, n2 + 101, 0x8FFFFFF);
        }
        SwiftModsMenu.roundCut2(class_3322, n, n2, n3, 102, 6, this.panelGradAt(n2), this.panelGradAt(n2 + 102));
        class_3322.method_25294(n + 6, n2 + 1, n + n3 - 6, n2 + 2, 0x14FFFFFF);
        boolean bl2 = module.enabled();
        int n4 = ModuleIcons.color(module);
        class_3322.method_25294(n + 1, n2 + 6, n + 4, n2 + 102 - 6, bl ? Theme.accent() : (bl2 ? n4 : -13421244));
        if (bl) {
            class_3322.method_25294(n + 6, n2 + 6, n + n3 - 6, n2 + 7, Theme.accent());
        }
        int n5 = n + n3 / 2 - 8;
        int n6 = n2 + 8;
        int n7 = SwiftModsMenu.mix(n4, -1, 0.28f) & 0xFFFFFF | 0x26000000;
        int n8 = SwiftModsMenu.mix(n4, -16777216, 0.3f) & 0xFFFFFF | 0x26000000;
        SwiftModsMenu.vgrad(class_3322, n5 - 3, n6 - 3, 22, 22, n7, n8);
        class_3322.method_25293(class_10799.field_56883, ModuleIcons.id(module), n + n3 / 2 - 8, n2 + 8, 0.0f, 0.0f, 16, 16, 24, 24, 24, 24, ModuleIcons.color(module));
        class_3322.method_27534(this.field_22793, SwiftText.bold(module.name()), n + n3 / 2, n2 + 28, -14998480);
        int n9 = n2 + 44;
        class_3322.method_25294(n + 8, n9, n + n3 - 8, n9 + 18, 0x59FFFFFF);
        Ui.outline(class_3322, n + 8, n9, n3 - 16, 18, 0x33FFFFFF);
        class_3322.method_27534(this.field_22793, SwiftText.of("OPTIONS"), n + n3 / 2 - 6, n9 + 5, -13747897);
        class_3322.method_25293(class_10799.field_56883, TILE_GEAR, n + n3 - 8 - 12, n9 + 4, 0.0f, 0.0f, 10, 10, 10, 10, 10, 10, -13747897);
        int n10 = n2 + 102 - 32;
        if (bl2) {
            class_3322.method_25294(n + 5, n10 - 2, n + n3 - 5, n10 + 20, 775741562);
            Ui.sprite(class_3322, Sprites.PILL_GREEN, n + 8, n10, n3 - 16, 18);
            class_3322.method_27534(this.field_22793, SwiftText.of("ENABLED"), n + n3 / 2, n10 + 5, -1);
        } else {
            class_3322.method_25294(n + 8, n10, n + n3 - 8, n10 + 18, 1717193356);
            Ui.outline(class_3322, n + 8, n10, n3 - 16, 18, 1720417236);
            class_3322.method_27534(this.field_22793, SwiftText.of("DISABLED"), n + n3 / 2, n10 + 5, -3555601);
        }
    }

    private void drawSettings(class_332 class_3322, int n, int n2) {
        int n3 = this.mainX();
        int n4 = this.py() + 30 + 14;
        class_3322.method_27535(this.field_22793, SwiftText.of("THEME"), n3 + 16, n4, -855305);
        class_3322.method_27535(this.field_22793, SwiftText.of("Pick your accent color. It updates the whole client instantly."), n3 + 16, n4 + 13, -7697776);
        int n5 = n3 + 16;
        int n6 = n4 + 38;
        for (int i = 0; i < Theme.ACCENTS.length; ++i) {
            int n7 = n5 + i * 24;
            boolean bl = n >= n7 && n < n7 + 16 && n2 >= n6 && n2 < n6 + 16;
            class_3322.method_25294(n7, n6, n7 + 16, n6 + 16, Theme.ACCENTS[i]);
            if (Theme.ACCENTS[i] != Theme.accent() && !bl) continue;
            Ui.outline(class_3322, n7 - 1, n6 - 1, 18, 18, -1);
        }
        class_3322.method_27535(this.field_22793, SwiftText.of("PREVIEW"), n3 + 16, n6 + 28, -855305);
        Ui.toggle(class_3322, n3 + 16, n6 + 44, 52, 24, true);
        class_3322.method_27535(this.field_22793, SwiftText.of("Enabled modules render in this color."), n3 + 76, n6 + 51, -7697776);
    }

    private void drawWaypoints(class_332 class_3322) {
        int n = this.mainX();
        int n2 = this.mainW();
        int n3 = this.py() + 30 + (this.panelH() - 30 - 22) / 2 - 8;
        class_3322.method_27534(this.field_22793, SwiftText.of("Waypoints are coming soon."), n + n2 / 2, n3, -855305);
        class_3322.method_27534(this.field_22793, SwiftText.of("They will live right here in a future update."), n + n2 / 2, n3 + 13, -7697776);
    }

    private void drawFooter(class_332 class_3322) {
        Object object;
        Object object2;
        int n = this.px();
        int n2 = this.py() + this.panelH() - 22 + 6;
        int n3 = this.panelW();
        if (this.tab.equals(TAB_MODS) && this.hoverCard >= 0 && this.hoverCard < this.visible().size()) {
            object2 = this.visible().get(this.hoverCard);
            object = ((Module)object2).name() + " \u2014 " + ((Module)object2).description();
        } else {
            object = "Tyxen X Swift Client  \u2022  left-click toggles, right-click configures";
        }
        class_3322.method_27535(this.field_22793, SwiftText.of((String)object), n + 12, n2, -7697506);
        object2 = "Presets: click to apply, right-click to delete";
        class_3322.method_27535(this.field_22793, SwiftText.of((String)object2), n + n3 - this.field_22793.method_27525((class_5348)SwiftText.of((String)object2)) - 12, n2, -10592396);
    }
}

