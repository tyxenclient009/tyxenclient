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
import net.minecraft.class_310;
import net.minecraft.class_320;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_5348;

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
    private static final int SIDE_BG = 0x78121A28;
    private static final int PANEL_TOP = 0x78161F30;
    private static final int PANEL_BOT = 0x66080D18;
    private static final int CARD_TOP = 0xC8D6E4F2;
    private static final int CARD_BOT = 0xAAB4CFE2;
    private static final int CARD_INK = 0xFF1B2430;
    private static final int CARD_SUB = 0xFF2E3947;
    private static final int DIM = 0x28000000;
    private static final int[] SHEEN = new int[]{0x0DFCE4EC, 0x0DD6E4FE, 0x0DE6D6FE, 0x0DD6FEDD, 0x0DFED6E8, 0x0DD6E4FE};

    /** Pearlescent hint: thin pastel bands cycling pink/blue/mint. */
    private static void iridescent(class_332 c, int x, int y, int w, int h) {
        int step = 8;
        int i = 0;
        for (int yy = y; yy < y + h; yy += step) {
            int hh = Math.min(step, y + h - yy);
            c.method_25294(x, yy, x + w, yy + hh, SHEEN[(i++) % SHEEN.length]);
        }
    }
    private static final int CARD = -1072360163;
    private final String startTab;
    private String tab;
    private int filter = 0;
    private int scroll;
    private int hoverCard = -1;
    private String query = "";
    private String toastText = null;
    private long toastUntil = 0L;
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

    public SwiftModsMenu() {
        this(TAB_MODS);
    }

    public SwiftModsMenu(String startTab) {
        super((class_2561)class_2561.method_43470((String)"Tyxen X Swift \u2014 Mods"));
        this.startTab = startTab;
        this.tab = startTab;
    }

    protected void method_25426() {
        this.openedAt = System.nanoTime();
        this.anim = 0.0f;
    }

    private static float ease(float x) {
        float inv = 1.0f - x;
        return 1.0f - inv * inv * inv;
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
        ArrayList<Module> out = new ArrayList<Module>();
        String f = FILTERS[this.filter];
        for (Module m : ModuleManager.all()) {
            if (!m.name().toLowerCase(Locale.ROOT).contains(this.query)) continue;
            if (f.equals("ALL")) {
                out.add(m);
                continue;
            }
            if (f.equals("NEW")) {
                if (!m.hasPosition()) continue;
                out.add(m);
                continue;
            }
              if (!m.category().name().equals(f)) continue;
              out.add(m);
          }
          if (f.equals("ALL") && this.query.isEmpty()) {
              ArrayList<Module> fav = new ArrayList<Module>();
              ArrayList<Module> rest = new ArrayList<Module>();
              for (Module m : out) {
                  if (SwiftModsMenu.isFav(m)) fav.add(m); else rest.add(m);
              }
              out.clear();
              out.addAll(fav);
              out.addAll(rest);
          }
          return out;
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

    /**
     * TYX addition — whole-panel adaptive scale: the layout is authored
     * for 680x400 and matrix-scaled about the panel center on smaller
     * screens, so sidebar, cards and footer always fit together instead
     * of overlapping. Hit-testing unscales mouse coords to match.
     */
    private float uiScale() {
        float s = Math.min(this.panelW() / 680.0f, this.panelH() / 400.0f);
        if (s > 1.0f) {
            s = 1.0f;
        }
        return s;
    }

    private float pcx() {
        return this.px() + this.panelW() / 2.0f;
    }

    private float pcy() {
        return this.py() + this.panelH() / 2.0f;
    }

    private int unX(int mx) {
        float s = this.uiScale();
        return Math.round((mx - this.pcx()) / s + this.pcx());
    }

    private int unY(int my) {
        float s = this.uiScale();
        return Math.round((my - this.pcy()) / s + this.pcy());
    }

    /**
     * TYX addition — flowing sidebar: every row below CONFIGS is placed
     * from the running cursor instead of fixed offsets, so the filter
     * list and the bottom buttons can never overlap, however short the
     * panel gets. Filter rows compact (20px -> 12px) before anything
     * collides; the panel matrix then fits the whole thing on screen.
     */
    private int presetRows() {
        return Math.max(1, Math.min(4, ClientConfig.presets().size()));
    }

    private int presetY(int i) {
        return this.py() + 44 + i * 16;
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
        int avail = this.filterBottomMax() - this.filterTop();
        int fh = avail / 6;
        if (fh > 20) {
            fh = 20;
        }
        if (fh < 12) {
            fh = 12;
        }
        return fh;
    }

    private int filterY(int i) {
        return this.filterTop() + i * this.filterH();
    }

    private int openFolderY() {
        return this.py() + this.panelH() - 72;
    }

    private int hudLayoutY() {
        return this.py() + this.panelH() - 44;
    }

    /**
     * TYX premium paint kit — vertical gradient fills, color mixing and a
     * shared panel backdrop. Everything stays flat-shaded rectangles
     * (crisp at any scale, zero texture seams).
     */
    private static int mix(int a, int b, float t) {
        int aa = (a >>> 24), ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int ba = (b >>> 24), br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        int al = (int)(aa + (ba - aa) * t), r = (int)(ar + (br - ar) * t),
                g = (int)(ag + (bg - ag) * t), bl = (int)(ab + (bb - ab) * t);
        return (al << 24) | (r << 16) | (g << 8) | bl;
    }

    private static void vgrad(class_332 c, int x, int y, int w, int h, int top, int bottom) {
        int step = 8;
        for (int yy = y; yy < y + h; yy += step) {
            int hh = Math.min(step, y + h - yy);
            float t = (float)(yy - y) / Math.max(1, h);
            int col = SwiftModsMenu.mix(top, bottom, t);
            c.method_25294(x, yy, x + w, yy + hh, col);
        }
    }

    /**
     * TYX liquid glass — near-black to deep navy, lightly translucent so
     * the dimmed game bleeds through, with rounded silhouette corners.
     */
    private void panelBg(class_332 c, int x, int y, int w, int h) {
        SwiftModsMenu.vgrad(c, x, y, w, h, PANEL_TOP, PANEL_BOT);
        SwiftModsMenu.iridescent(c, x, y, w, h);
        SwiftModsMenu.roundCut(c, x, y, w, h, 10, DIM);
        c.method_25294(x + 10, y + 1, x + w - 10, y + 2,
                (Theme.accent() & 0xFFFFFF) | 0x66000000);
    }

    /** Carve rounded corners by painting the backdrop color over them. */
    private static void roundCut(class_332 c, int x, int y, int w, int h, int r, int ambient) {
        SwiftModsMenu.roundCut2(c, x, y, w, h, r, ambient, ambient);
    }

    private static void roundCut2(class_332 c, int x, int y, int w, int h, int r, int ambTop, int ambBot) {
        for (int j = 0; j < r; ++j) {
            for (int i = 0; i < r; ++i) {
                if (i * i + j * j < r * r) {
                    continue;
                }
                c.method_25294(x + i, y + j, x + i + 1, y + j + 1, ambTop);
                c.method_25294(x + w - 1 - i, y + j, x + w - i, y + j + 1, ambTop);
                c.method_25294(x + i, y + h - 1 - j, x + i + 1, y + h - j, ambBot);
                c.method_25294(x + w - 1 - i, y + h - 1 - j, x + w - i, y + h - j, ambBot);
            }
        }
    }

    /** Proper filled rounded rect — drawn directly, no ambient repaint needed. */
    private static void rr(class_332 c, int x, int y, int w, int h, int r, int col) {
        if (w <= 0 || h <= 0) {
            return;
        }
        r = Math.min(r, Math.min(w, h) / 2);
        if (r <= 0) {
            c.method_25294(x, y, x + w, y + h, col);
            return;
        }
        c.method_25294(x + r, y, x + w - r, y + h, col);
        c.method_25294(x, y + r, x + r, y + h - r, col);
        c.method_25294(x + w - r, y + r, x + w, y + h - r, col);
        for (int j = 0; j < r; ++j) {
            int dy = r - j;
            int dx = (int)Math.sqrt((double)(r * r - dy * dy));
            c.method_25294(x + r - dx, y + j, x + r, y + j + 1, col);
            c.method_25294(x + w - r, y + j, x + w - r + dx, y + j + 1, col);
            int by = y + h - 1 - j;
            c.method_25294(x + r - dx, by, x + r, by + 1, col);
            c.method_25294(x + w - r, by, x + w - r + dx, by + 1, col);
        }
    }

    /** Vertical-gradient rounded rect. */
    private static void rrGrad(class_332 c, int x, int y, int w, int h, int r, int top, int bot) {
        if (w <= 0 || h <= 0) {
            return;
        }
        r = Math.min(r, Math.min(w, h) / 2);
        for (int j = 0; j < h; ++j) {
            float t = (float)j / Math.max(1, h - 1);
            int col = SwiftModsMenu.mix(top, bot, t);
            int inset = 0;
            if (j < r) {
                int dy = r - j;
                inset = r - (int)Math.sqrt((double)(r * r - dy * dy));
            } else if (j >= h - r) {
                int dy = r - (h - 1 - j);
                inset = r - (int)Math.sqrt((double)(r * r - dy * dy));
            }
            c.method_25294(x + inset, y + j, x + w - inset, y + j + 1, col);
        }
    }

    /** GLFW keycode -> short badge label. null = unbound. */
    private static String keyName(int k) {
        if (k >= 65 && k <= 90) return String.valueOf((char)k);
        if (k >= 48 && k <= 57) return String.valueOf((char)k);
        if (k >= 290 && k <= 301) return "F" + (k - 289);
        if (k >= 320 && k <= 329) return "NUM" + (k - 320);
        switch (k) {
            case 32: return "SPACE";
            case 257: return "ENTER";
            case 258: return "TAB";
            case 259: return "BACKSPACE";
            case 261: return "DEL";
            case 262: return "RIGHT";
            case 263: return "LEFT";
            case 264: return "DOWN";
            case 265: return "UP";
            case 268: return "HOME";
            case 269: return "END";
            case 280: return "CAPS";
            case 340: return "LSHIFT";
            case 341: return "LCTRL";
            case 342: return "LALT";
            case 344: return "RSHIFT";
            case 345: return "RCTRL";
            case 346: return "RALT";
            default: return null;
        }
    }

    private static boolean isFav(Module m) {
        try {
            return ClientConfig.getBool("TyxenFavs", m.name(), false);
        } catch (Exception e) {
            return false;
        }
    }

    private static void setFav(Module m, boolean fav) {
        try {
            ClientConfig.setBool("TyxenFavs", m.name(), fav);
            ClientConfig.save();
        } catch (Exception e) {
        }
    }

    private void toast(String s) {
        this.toastText = s;
        this.toastUntil = System.currentTimeMillis() + 2500L;
    }

    private String playerName() {
        try {
            Object s = ((class_310)this.field_22787).method_1548();
            if (s != null) return ((class_320)s).method_1676();
        } catch (Exception e) {
        }
        return null;
    }

    /** Panel gradient color at an absolute y — corner ambient for cards. */
    private int panelGradAt(int yy) {
        float t = (float)(yy - this.py()) / Math.max(1, this.panelH());
        if (t < 0.0f) {
            t = 0.0f;
        }
        if (t > 1.0f) {
            t = 1.0f;
        }
        return SwiftModsMenu.mix(PANEL_TOP, PANEL_BOT, t);
    }

    private int cardW() {
        return (this.mainW() - 20 - 16) / 3;
    }

    private int gridRows() {
        return (this.visible().size() + 3 - 1) / 3;
    }

    private int maxScroll() {
        int content = this.gridRows() * 110 - 8;
        return Math.max(0, content - (this.gridBottom() - this.gridTop()));
    }

    private int cardX(int col) {
        return this.mainX() + 10 + col * (this.cardW() + 8);
    }

    private int cardY(int row) {
        return this.gridTop() - this.scroll + row * 110;
    }

    public boolean method_25402(class_11909 click, boolean doubleClick) {
        int sw;
        int t;
        if (click.method_74245() != 0 && click.method_74245() != 1) {
            return false;
        }
          boolean left = click.method_74245() == 0;
          int mx = this.unX((int)click.comp_4798());
          int my = this.unY((int)click.comp_4799());
          int x = this.px();
        int y = this.py();
        int w = this.panelW();
        int h = this.panelH();
        if (left && mx >= x + w - 26 && mx < x + w - 10 && my >= y + 7 && my < y + 23) {
            this.method_25419();
            return true;
        }
        int n = t = left ? this.tabAt(mx, my) : -1;
        if (t >= 0) {
            this.tab = t == 0 ? TAB_MODS : (t == 1 ? TAB_SETTINGS : TAB_WAYPOINTS);
            this.scroll = 0;
            return true;
        }
        if (left && this.tab.equals(TAB_SETTINGS) && (sw = this.swatchAt(mx, my)) >= 0) {
            Theme.setAccent(Theme.ACCENTS[sw]);
            return true;
        }
        if (this.tab.equals(TAB_MODS)) {
            int ci;
            int ry;
            int i;
            List<String> presets = ClientConfig.presets();
            int pcount = Math.min(4, presets.size());
            for (i = 0; i < pcount; ++i) {
                ry = this.presetY(i);
                if (mx < x + 10 || mx >= x + 148 - 10 || my < ry || my >= ry + 16) continue;
                if (click.method_74245() == 1) {
                    ClientConfig.deletePreset(presets.get(i));
                } else if (left && ClientConfig.loadPreset(presets.get(i))) {
                    for (Module m : ModuleManager.all()) {
                        m.setEnabled(ClientConfig.enabled(m.name(), false));
                    }
                }
                return true;
            }
            if (left && this.inSavePreset(mx, my)) {
                ClientConfig.savePreset(ClientConfig.nextPresetName());
                return true;
            }
            if (left) {
                for (i = 0; i < FILTERS.length; ++i) {
                    ry = this.filterY(i);
                    if (mx < x + 10 || mx >= x + 148 - 10 || my < ry || my >= ry + this.filterH()) continue;
                    this.filter = i;
                    this.scroll = 0;
                    return true;
                }
            }
            int n2 = ci = left ? this.chipAt(mx, my) : -1;
            if (ci >= 0) {
                this.filter = ci;
                this.scroll = 0;
                return true;
            }
              List<Module> mods = this.visible();
              int cols = 3;
              int cw = this.cardW();
            for (int i2 = 0; i2 < mods.size(); ++i2) {
                boolean inOptions;
                int cx = this.cardX(i2 % cols);
                int cy = this.cardY(i2 / cols);
                if (cy + 102 < this.gridTop() || cy > this.gridBottom() || mx < cx || mx >= cx + cw || my < cy || my >= cy + 102) continue;
                boolean bl = inOptions = my >= cy + 44 && my < cy + 62;
                if (left && mx >= cx + cw - 26 && mx < cx + cw - 8 && my >= cy + 3 && my < cy + 17) {
                    Module sm = mods.get(i2);
                    SwiftModsMenu.setFav(sm, !SwiftModsMenu.isFav(sm));
                    this.toast(sm.name() + (SwiftModsMenu.isFav(sm) ? " favorited" : " unfavorited"));
                    return true;
                }
                if (click.method_74245() == 1 || inOptions) {
                    this.field_22787.method_1507((class_437)new SwiftModuleSettings(mods.get(i2), this));
                } else {
                    Module tm = mods.get(i2);
                    tm.toggle();
                    this.toast(tm.name() + (tm.enabled() ? " enabled" : " disabled"));
                }
                return true;
            }
            if (left && this.inOpenFolder(mx, my)) {
                try {
                    class_156.method_668().method_60932(FabricLoader.getInstance().getConfigDir());
                }
                catch (Exception exception) {
                    // empty catch block
                }
                return true;
            }
            if (left && this.inHudLayout(mx, my)) {
                this.field_22787.method_1507((class_437)new SwiftHudEditor(this));
                return true;
            }
        }
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private int tabAt(int mx, int my) {
        int y = this.py();
        int base = this.px() + 148 + 12;
        String[] tabs = new String[]{TAB_MODS, TAB_SETTINGS, TAB_WAYPOINTS};
        for (int i = 0; i < tabs.length; ++i) {
            int tw = this.field_22793.method_27525((class_5348)SwiftText.of(tabs[i])) + 16;
            if (mx >= base && mx < base + tw && my >= y + 7 && my < y + 23) {
                return i;
            }
            base += tw + 4;
        }
        return -1;
    }

    public boolean method_25400(class_11905 input) {
        if (!this.tab.equals(TAB_MODS) || !input.method_74227()) {
            return false;
        }
        String s = input.method_74226();
        if (s.length() != 1) {
            return false;
        }
        char ch = s.charAt(0);
        if (ch < ' ' || ch > '~' || this.query.length() >= 24) {
            return true;
        }
        this.query = this.query + Character.toLowerCase(ch);
        return true;
    }

    public boolean method_25404(class_11908 input) {
        if (this.tab.equals(TAB_MODS) && input.comp_4795() == 259 && !this.query.isEmpty()) {
            this.query = this.query.substring(0, this.query.length() - 1);
            return true;
        }
        return super.method_25404(input);
    }

    private int chipAt(int mx, int my) {
        int y = this.py() + 30 + 8;
        int x = this.mainX() + 10;
        for (int i = 0; i < FILTERS.length; ++i) {
            int cw = this.field_22793.method_27525((class_5348)SwiftText.of(FILTERS[i])) + 14;
            if (mx >= x && mx < x + cw && my >= y && my < y + 16) {
                return i;
            }
            x += cw + 6;
        }
        return -1;
    }

    private int swatchAt(int mx, int my) {
        int sx = this.mainX() + 16;
        int sy = this.py() + 30 + 52;
        if (my < sy || my >= sy + 16) {
            return -1;
        }
        int i = (mx - sx) / 24;
        return i >= 0 && i < Theme.ACCENTS.length && mx < sx + i * 24 + 16 ? i : -1;
    }

    private boolean inSavePreset(int mx, int my) {
        int x = this.px();
        int sy = this.saveY();
        return mx >= x + 10 && mx < x + 148 - 10 && my >= sy && my < sy + 16;
    }

    private boolean inOpenFolder(int mx, int my) {
        int x = this.px();
        int y = this.py();
        int h = this.panelH();
        return mx >= x + 10 && mx < x + 148 - 10 && my >= y + h - 72 && my < y + h - 48;
    }

    private boolean inHudLayout(int mx, int my) {
        int x = this.px();
        int y = this.py();
        int h = this.panelH();
        return mx >= x + 10 && mx < x + 148 - 10 && my >= y + h - 44 && my < y + h - 20;
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!this.tab.equals(TAB_MODS)) {
            return false;
        }
        int x = this.px();
        int y = this.py();
        int w = this.panelW();
        int h = this.panelH();
        if (mouseX < (double)x || mouseX >= (double)(x + w) || mouseY < (double)y || mouseY >= (double)(y + h)) {
            return false;
        }
        int before = this.scroll;
        this.scroll = (int)Math.max(0.0, Math.min((double)this.maxScroll(), (double)this.scroll - verticalAmount * 26.0));
        return this.scroll != before;
    }

    public void method_25394(class_332 c, int mouseX, int mouseY, float delta) {
        this.anim = SwiftModsMenu.ease(Math.min(1.0f, (float)(System.nanoTime() - this.openedAt) / 1.5E8f));
        try {
            c.method_71278();
        }
        catch (IllegalStateException illegalStateException) {
            // empty catch block
        }
          c.method_25294(0, 0, this.field_22789, this.field_22790, DIM);
          int x = this.px();
          int y = this.py();
          int w = this.panelW();
          int h = this.panelH();
          this.panelBg(c, x, y, w, h);
          int ox = mouseX;
          int oy = mouseY;
          mouseX = this.unX(mouseX);
          mouseY = this.unY(mouseY);
          float ps = this.uiScale();
          float pcfx = this.pcx();
          float pcfy = this.pcy();
          org.joml.Matrix3x2fStack pmat = c.method_51448();
          pmat.pushMatrix();
          pmat.translate(pcfx, pcfy);
          pmat.scale(ps, ps);
          pmat.translate(-pcfx, -pcfy);
          try {
          this.drawHeader(c, mouseX, mouseY);
        if (this.tab.equals(TAB_MODS)) {
            this.drawSidebar(c, mouseX, mouseY);
            this.drawMods(c, mouseX, mouseY);
        } else if (this.tab.equals(TAB_SETTINGS)) {
            this.drawSettings(c, mouseX, mouseY);
        } else {
            this.drawWaypoints(c);
        }
          this.drawFooter(c);
          this.drawToast(c);
          } finally {
          pmat.popMatrix();
          }
          super.method_25394(c, ox, oy, delta);
    }

    private void drawHeader(class_332 c, int mx, int my) {
        int x = this.px();
        int y = this.py();
        int w = this.panelW();
        Ui.crescent(c, x + 22, y + 15, 9, -723718);
            c.method_27535(this.field_22793, SwiftText.bold("TYXEN X SWIFT"), x + 36, y + 11, -855308);
            String pn = this.playerName();
            if (pn != null && !pn.isEmpty()) {
                if (pn.length() > 16) pn = pn.substring(0, 16);
                c.method_27535(this.field_22793, SwiftText.of(pn), x + 36, y + 21, -7697506);
            }
        String[] tabs = new String[]{TAB_MODS, TAB_SETTINGS, TAB_WAYPOINTS};
        int base = x + 148 + 12;
          for (int i = 0; i < tabs.length; ++i) {
              boolean hover;
              int tw = this.field_22793.method_27525((class_5348)SwiftText.of(tabs[i])) + 16;
                boolean sel = this.tab.equals(tabs[i]);
                boolean bl = hover = mx >= base && mx < base + tw && my >= y + 7 && my < y + 23;
                if (sel) {
                    SwiftModsMenu.rr(c, base, y + 5, tw, 20, 9, 0x661E9BF0);
                    SwiftModsMenu.rr(c, base + 1, y + 6, tw - 2, 18, 8, 0xFF1E9BF0);
                }
                c.method_27534(this.field_22793, sel ? SwiftText.bold(tabs[i]) : SwiftText.of(tabs[i]), base + tw / 2, y + 11, sel ? -1 : -7697506);
                if (!sel && hover) {
                    c.method_25294(base + 2, y + 23, base + tw - 2, y + 24, 0x44FFFFFF);
                }
              base += tw + 4;
          }
        boolean hX = mx >= x + w - 26 && mx < x + w - 10 && my >= y + 7 && my < y + 23;
        Ui.sprite(c, hX ? Sprites.TILE16_HOVER : Sprites.TILE16, x + w - 26, y + 7, 16, 16);
        c.method_27534(this.field_22793, SwiftText.of("x"), x + w - 18, y + 11, hX ? -1 : -7697506);
        c.method_25294(x, y + 30 - 1, x + w, y + 30, -13027015);
    }

    private void drawSidebar(class_332 c, int mx, int my) {
        int x = this.px();
        int y = this.py();
        int h = this.panelH();
          c.method_25294(x, y + 30, x + 148 - 1, y + h - 22, SIDE_BG);
        c.method_25294(x + 148 - 1, y + 30, x + 148, y + h - 22, -13027015);
          c.method_27535(this.field_22793, SwiftText.of("CONFIGS"), x + 12, y + 32, -10592396);
          List<String> presets = ClientConfig.presets();
          int count = Math.min(4, presets.size());
          for (int i = 0; i < count; ++i) {
              boolean hover;
              int ry = this.presetY(i);
              boolean bl = hover = mx >= x + 10 && mx < x + 148 - 10 && my >= ry && my < ry + 16;
              if (hover) {
                  SwiftModsMenu.rr(c, x + 8, ry, 132, 16, 7, -15461349);
              }
              c.method_25294(x + 8, ry, x + 10, ry + 16, hover ? Theme.accent() : -13421244);
              c.method_27535(this.field_22793, SwiftText.of(presets.get(i)), x + 16, ry + 4, hover ? -1 : -7697506);
          }
          if (presets.isEmpty()) {
              c.method_27535(this.field_22793, SwiftText.of("No presets yet"), x + 16, this.presetY(0) + 4, -7697776);
          }
          int sy = this.saveY();
          boolean hovSave = this.inSavePreset(mx, my);
          Ui.sprite(c, hovSave ? Sprites.BTN_BLUE_HOVER : Sprites.BTN_BLUE, x + 10, sy, 128, 16);
          c.method_27534(this.field_22793, SwiftText.of("+ SAVE CURRENT"), x + 74, sy + 4, -1);
          c.method_25294(x + 12, this.divY(), x + 148 - 12, this.divY() + 1, -13027015);
          c.method_27535(this.field_22793, SwiftText.of("CATEGORIES"), x + 12, this.catY(), -10592396);
          int fh = this.filterH();
          for (int i = 0; i < FILTERS.length; ++i) {
              boolean hover;
              int ry = this.filterY(i);
                boolean sel = this.filter == i;
                boolean bl = hover = mx >= x + 10 && mx < x + 148 - 10 && my >= ry && my < ry + fh;
                if (sel) {
                    SwiftModsMenu.rr(c, x + 8, ry, 132, fh, 8, 0xFF1E9BF0);
                } else if (hover) {
                    SwiftModsMenu.rr(c, x + 8, ry, 132, fh, 8, -871099365);
                }
                c.method_27535(this.field_22793, SwiftText.of(FILTERS[i]), x + 16, ry + (fh - 8) / 2, sel ? -1 : -7697506);
          }
          boolean hov = this.inOpenFolder(mx, my);
          Ui.sprite(c, hov ? Sprites.BTN_HOVER : Sprites.BTN, x + 10, this.openFolderY(), 128, 24);
          c.method_27534(this.field_22793, SwiftText.of("OPEN FOLDER"), x + 74, this.openFolderY() + 8, -4605239);
          boolean hov2 = this.inHudLayout(mx, my);
          Ui.sprite(c, hov2 ? Sprites.BTN_BLUE_HOVER : Sprites.BTN_BLUE, x + 10, this.hudLayoutY(), 128, 24);
          c.method_27534(this.field_22793, SwiftText.of("EDIT HUD LAYOUT"), x + 74, this.hudLayoutY() + 8, -1);
    }

    private void drawMods(class_332 c, int mx, int my) {
        int cw;
        int y = this.py() + 30 + 8;
        int x = this.mainX() + 10;
        for (int i = 0; i < FILTERS.length; ++i) {
            boolean sel;
            int cw2 = this.field_22793.method_27525((class_5348)SwiftText.of(FILTERS[i])) + 14;
            boolean bl = sel = this.filter == i;
            if (sel) {
                Ui.sprite(c, Sprites.chipAccent(Theme.accIndex()), x, y, cw2, 16);
            } else {
                Ui.sprite(c, Sprites.CHIP_DARK, x, y, cw2, 16);
            }
            c.method_27534(this.field_22793, SwiftText.of(FILTERS[i]), x + cw2 / 2, y + 4, sel ? -15856108 : -4605239);
            x += cw2 + 6;
        }
        int sx1 = this.mainX() + this.mainW() - 8;
        int sx0 = sx1 - 150;
          if (sx0 > x + 4) {
              Ui.sprite(c, Sprites.CHIP_DARK, sx0, y, sx1 - sx0, 16);
              Ui.ring(c, sx0 + 10, y + 8, 3, 1, -10592396);
              c.method_25294(sx0 + 12, y + 10, sx0 + 15, y + 13, -10592396);
              String shown = this.query.isEmpty() ? "Search" : this.query;
              c.method_27535(this.field_22793, SwiftText.of(shown), sx0 + 19, y + 4, this.query.isEmpty() ? -10592396 : -855305);
              if (System.currentTimeMillis() / 500L % 2L == 0L) {
                  cw = this.field_22793.method_27525((class_5348)SwiftText.of(shown));
                  c.method_25294(sx0 + 20 + cw, y + 4, sx0 + 21 + cw, y + 12, -4605239);
              }
          }
          List<Module> mods = this.visible();
          this.hoverCard = -1;
          cw = this.cardW();
          int cols = 3;
          int top = this.gridTop();
          int bot = this.gridBottom();
        c.method_44379(this.mainX(), top, this.mainW() - 8, bot - top);
        for (int i = 0; i < mods.size(); ++i) {
            boolean hover;
            Module m = mods.get(i);
              int cx = this.cardX(i % 3);
              int cy = this.cardY(i / 3);
            if (cy + 102 < top || cy > bot) continue;
            boolean bl = hover = mx >= cx && mx < cx + cw && my >= cy && my < cy + 102 && cy >= top - 2 && cy + 102 <= bot + 2;
              if (hover) {
                  this.hoverCard = i;
              }
              this.drawCard(c, m, cx, cy, cw, hover);
          }
          c.method_44380();
        int max = this.maxScroll();
        if (max > 0) {
            float view = bot - top;
              int barH = Math.max(24, (int)(view * (view / ((float)mods.size() / 3.0f * 110.0f))));
            int barY = top + (int)((view - (float)barH) * ((float)this.scroll / (float)max));
            int sx = this.mainX() + this.mainW() - 7;
            c.method_25294(sx, top, sx + 3, bot, -1726276568);
            c.method_25294(sx, barY, sx + 3, barY + barH, Theme.accent());
        }
    }

      private void drawCard(class_332 c, Module m, int x, int y, int w, boolean hover) {
          // TYX liquid-glass card: true rounded body, inner glow border,
          // glossy top highlight, white icon chip.
          SwiftModsMenu.rr(c, x, y, w, 102, 8, hover ? 0x99FFFFFF : 0x55FFFFFF);
          SwiftModsMenu.rrGrad(c, x + 1, y + 1, w - 2, 100, 7, CARD_TOP, CARD_BOT);
          c.method_25294(x + 10, y + 2, x + w - 10, y + 3, 0x40FFFFFF);
          boolean on = m.enabled();
          int mc = ModuleIcons.color(m);
            int ix = x + w / 2 - 8;
            int iy = y + 8;
            SwiftModsMenu.rr(c, ix - 3, iy - 3, 22, 22, 6, 0x33FFFFFF);
            SwiftModsMenu.rr(c, ix - 2, iy - 2, 20, 20, 5, 0x40FFFFFF);
        c.method_25293(class_10799.field_56883, ModuleIcons.id(m), x + w / 2 - 8, y + 8, 0.0f, 0.0f, 16, 16, 24, 24, 24, 24, ModuleIcons.color(m));
          String kb = SwiftModsMenu.keyName(m.key());
          if (kb != null) {
              c.method_27535(this.field_22793, SwiftText.of(kb), x + 8, y + 5, CARD_SUB);
          }
          boolean fav = SwiftModsMenu.isFav(m);
          int scx = x + w - 16;
          int scy = y + 10;
          int scol = fav ? 0xFFFFD54A : (hover ? 0xFFADB6C6 : 0xFF7C8698);
          c.method_25294(scx - 5, scy, scx + 6, scy + 1, scol);
          c.method_25294(scx, scy - 5, scx + 1, scy + 6, scol);
          c.method_25294(scx - 2, scy - 2, scx + 3, scy + 3, scol);
          c.method_27534(this.field_22793, SwiftText.bold(m.name()), x + w / 2, y + 28, CARD_INK);
          int oy = y + 44;
          SwiftModsMenu.rr(c, x + 8, oy, w - 16, 18, 8, 0x33FFFFFF);
          SwiftModsMenu.rr(c, x + 9, oy + 1, w - 18, 16, 7, 0x59FFFFFF);
          c.method_27534(this.field_22793, SwiftText.of("OPTIONS"), x + w / 2 - 6, oy + 5, CARD_SUB);
            c.method_25293(class_10799.field_56883, TILE_GEAR, x + w - 8 - 12, oy + 4, 0.0f, 0.0f, 10, 10, 10, 10, 10, 10, CARD_SUB);
            int ty = y + 102 - 32;
            if (on) {
                SwiftModsMenu.rr(c, x + 5, ty - 2, w - 10, 22, 10, 0x2E3CE07A);
                SwiftModsMenu.rr(c, x + 8, ty, w - 16, 18, 8, 0x803CE07A);
                SwiftModsMenu.rrGrad(c, x + 9, ty + 1, w - 18, 16, 7, 0xFF37E393, 0xFF0FA968);
                c.method_27534(this.field_22793, SwiftText.of("ENABLED"), x + w / 2, ty + 5, -1);
            } else {
                SwiftModsMenu.rr(c, x + 8, ty, w - 16, 18, 8, 0x668B7BD4);
                SwiftModsMenu.rr(c, x + 9, ty + 1, w - 18, 16, 7, 0x665A4A8C);
                c.method_27534(this.field_22793, SwiftText.of("DISABLED"), x + w / 2, ty + 5, 0xFFC9BEEF);
            }
    }

    private void drawToast(class_332 c) {
        if (this.toastText == null || System.currentTimeMillis() > this.toastUntil) return;
        int w = this.panelW();
        int tw = this.field_22793.method_27525((class_5348)SwiftText.of(this.toastText)) + 24;
        int tx = this.px() + w - tw - 12;
        int ty = this.py() + this.panelH() - 46;
        SwiftModsMenu.rr(c, tx, ty, tw, 18, 8, 0xCC101A2C);
        SwiftModsMenu.rr(c, tx + 1, ty + 1, tw - 2, 16, 7, 0xCC1E2A40);
        c.method_27534(this.field_22793, SwiftText.of(this.toastText), tx + tw / 2, ty + 5, -1);
    }

    private void drawSettings(class_332 c, int mx, int my) {
        int x = this.mainX();
        int y = this.py() + 30 + 14;
        c.method_27535(this.field_22793, SwiftText.of("THEME"), x + 16, y, -855305);
        c.method_27535(this.field_22793, SwiftText.of("Pick your accent color. It updates the whole client instantly."), x + 16, y + 13, -7697776);
        int sx = x + 16;
        int sy = y + 38;
        for (int i = 0; i < Theme.ACCENTS.length; ++i) {
            int bx = sx + i * 24;
            boolean hov = mx >= bx && mx < bx + 16 && my >= sy && my < sy + 16;
            c.method_25294(bx, sy, bx + 16, sy + 16, Theme.ACCENTS[i]);
            if (Theme.ACCENTS[i] != Theme.accent() && !hov) continue;
            Ui.outline(c, bx - 1, sy - 1, 18, 18, -1);
        }
        c.method_27535(this.field_22793, SwiftText.of("PREVIEW"), x + 16, sy + 28, -855305);
        Ui.toggle(c, x + 16, sy + 44, 52, 24, true);
        c.method_27535(this.field_22793, SwiftText.of("Enabled modules render in this color."), x + 76, sy + 51, -7697776);
    }

    private void drawWaypoints(class_332 c) {
        int x = this.mainX();
        int mw = this.mainW();
        int y = this.py() + 30 + (this.panelH() - 30 - 22) / 2 - 8;
        c.method_27534(this.field_22793, SwiftText.of("Waypoints are coming soon."), x + mw / 2, y, -855305);
        c.method_27534(this.field_22793, SwiftText.of("They will live right here in a future update."), x + mw / 2, y + 13, -7697776);
    }

    private void drawFooter(class_332 c) {
        Object s;
        int x = this.px();
        int y = this.py() + this.panelH() - 22 + 6;
        int w = this.panelW();
        if (this.tab.equals(TAB_MODS) && this.hoverCard >= 0 && this.hoverCard < this.visible().size()) {
            Module m = this.visible().get(this.hoverCard);
            s = m.name() + " \u2014 " + m.description();
        } else {
                s = "Tyxen X Swift Client  \u2022  left-click toggles, right-click configures";
        }
        c.method_27535(this.field_22793, SwiftText.of((String)s), x + 12, y, -7697506);
        String r = "Presets: click to apply, right-click to delete";
        c.method_27535(this.field_22793, SwiftText.of(r), x + w - this.field_22793.method_27525((class_5348)SwiftText.of(r)) - 12, y, -10592396);
    }
}

