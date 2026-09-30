package gg.tyx.client.gui;

import gg.tyx.client.TyxClient;
import gg.tyx.client.TyxConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

/**
 * Dawn-style mod menu: top rail (MOD MENU + module pills + close), left
 * settings panel (collapsible sections, segmented pills, sliders,
 * switches), right live preview with Mod Preview + Reset buttons.
 * Everything custom-drawn — identical on every Minecraft version, and
 * every control does something real.
 */
public final class TyxMenuScreen extends Screen {
    private final Screen parent;

    private static final int ACC = 0xFF1EA86A;
    private static final int ACC_HI = 0xFF4FD68A;
    private static final int ROW = 0xFF1B2220;
    private static final int LINE = 0xFF26302B;
    private static final int TXT = 0xFFE8EEEA;
    private static final int DIM = 0xFFA8B7AE;
    private static final int FAINT = 0xFF5B6660;

    // ── rows (each remembers its own rect for hit-testing) ───────────────

    private abstract static class Row {
        int rx, ry, rw, rh;

        abstract int height();

        abstract void draw(DrawContext ctx, TextRenderer tr, int x, int y, int w);

        /** Press inside the row. Return true when something changed. */
        boolean press(double mx, double my) {
            return false;
        }

        void drag(double mx) {
        }
    }

    private final class ToggleRow extends Row {
        final String label;
        final java.util.function.BooleanSupplier get;
        final Runnable flip;

        ToggleRow(String label, java.util.function.BooleanSupplier get, Runnable flip) {
            this.label = label;
            this.get = get;
            this.flip = flip;
        }

        int height() {
            return 24;
        }

        void draw(DrawContext ctx, TextRenderer tr, int x, int y, int w) {
            rx = x;
            ry = y;
            rw = w;
            rh = 24;
            ctx.drawTextWithShadow(tr, Text.literal(label), x, y + 7, DIM);
            int sw = 30, sh = 15, sx = x + w - sw, sy = y + 4;
            boolean on = get.getAsBoolean();
            ctx.fill(sx, sy, sx + sw, sy + sh, on ? ACC : 0xFF2A3530);
            int knob = on ? sx + sw - 13 : sx + 2;
            ctx.fill(knob, sy + 2, knob + 11, sy + sh - 2, 0xFFFFFFFF);
        }

        boolean press(double mx, double my) {
            flip.run();
            save();
            return true;
        }
    }

    private final class SegRow extends Row {
        final String label;
        final String[] options;
        final java.util.function.IntSupplier get;
        final java.util.function.IntConsumer set;
        final List<int[]> rects = new ArrayList<>();

        SegRow(String label, String[] options, java.util.function.IntSupplier get,
                java.util.function.IntConsumer set) {
            this.label = label;
            this.options = options;
            this.get = get;
            this.set = set;
        }

        int height() {
            return 26;
        }

        void draw(DrawContext ctx, TextRenderer tr, int x, int y, int w) {
            rx = x;
            ry = y;
            rw = w;
            rh = 26;
            rects.clear();
            ctx.drawTextWithShadow(tr, Text.literal(label), x, y + 6, DIM);
            int cur = get.getAsInt();
            int bx = x + w;
            for (int i = options.length - 1; i >= 0; i--) {
                int bw = tr.getWidth(options[i]) + 16;
                bx -= bw + 4;
                boolean sel = i == cur;
                ctx.fill(bx, y + 1, bx + bw, y + 19, sel ? ACC : ROW);
                drawBorder(ctx, bx, y + 1, bw, 18, sel ? ACC_HI : LINE);
                ctx.drawTextWithShadow(tr, Text.literal(options[i]), bx + 8, y + 6,
                        sel ? 0xFF0B0F0D : DIM);
                rects.add(new int[] { bx, y + 1, bw, 18, i });
            }
        }

        boolean press(double mx, double my) {
            for (int[] r : rects) {
                if (mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3]) {
                    set.accept(r[4]);
                    save();
                    return true;
                }
            }
            return false;
        }
    }

    private final class SliderRow extends Row {
        final String label;
        final int min, max;
        final java.util.function.IntSupplier get;
        final java.util.function.IntConsumer set;
        int tx, ty, tw;

        SliderRow(String label, int min, int max, java.util.function.IntSupplier get,
                java.util.function.IntConsumer set) {
            this.label = label;
            this.min = min;
            this.max = max;
            this.get = get;
            this.set = set;
        }

        int height() {
            return 26;
        }

        void draw(DrawContext ctx, TextRenderer tr, int x, int y, int w) {
            rx = x;
            ry = y;
            rw = w;
            rh = 26;
            int val = get.getAsInt();
            ctx.drawTextWithShadow(tr, Text.literal(label), x, y + 6, DIM);
            String vs = String.valueOf(val);
            int vw = tr.getWidth(vs) + 10;
            tx = x + w - 150;
            tw = 150;
            ty = y + 6;
            ctx.fill(tx, ty + 4, tx + tw, ty + 6, 0xFF2A3530);
            double t = (double) (val - min) / Math.max(1, max - min);
            int kx = tx + (int) (t * tw);
            ctx.fill(tx, ty + 4, kx, ty + 6, ACC);
            ctx.fill(kx - 3, ty, kx + 3, ty + 10, ACC_HI);
            ctx.drawTextWithShadow(tr, Text.literal(vs), tx - vw - 6, y + 6, TXT);
        }

        boolean press(double mx, double my) {
            if (mx >= tx - 4 && mx <= tx + tw + 4 && my >= ty - 4 && my <= ty + 14) {
                dragging = this;
                drag(mx);
                return true;
            }
            return false;
        }

        void drag(double mx) {
            double t = (mx - tx) / (double) Math.max(1, tw);
            t = Math.max(0, Math.min(1, t));
            set.accept(min + (int) Math.round(t * (max - min)));
            save();
        }
    }

    private static final class Section {
        final String title;
        boolean open = true;
        final List<Row> rows = new ArrayList<>();

        Section(String title) {
            this.title = title;
        }
    }

    private static final class Mod {
        final String id;
        final String name;
        final String shortName;
        final String desc;
        final List<Section> sections = new ArrayList<>();

        Mod(String id, String name, String shortName, String desc) {
            this.id = id;
            this.name = name;
            this.shortName = shortName;
            this.desc = desc;
        }
    }

    private final List<Mod> mods = new ArrayList<>();
    private int selected;
    private double railX;
    private double setY;
    private boolean listCollapsed;
    private boolean previewPlain;
    private Row dragging;

    private int pillY, pillH;
    private final List<int[]> pillHit = new ArrayList<>();
    private int closeX, closeY;
    private int mmX, mmW;
    private int setX, setY0, setW, setH;
    private int topY, railY, railH, bodyY, bodyH;
    private int listX, listW, prevX, prevW;
    private int previewBtnY;

    public TyxMenuScreen(Screen parent) {
        super(Text.literal("Tyx Client"));
        this.parent = parent;
        build();
        rows();
    }

    private TyxConfig c() {
        return TyxClient.CONFIG;
    }

    private void save() {
        TyxClient.CONFIG.save();
    }

    // ── module catalogue ─────────────────────────────────────────────────

    private void build() {
        mods.clear();
        add("badge", "Nametag Badge", "Badge", "Tyx icon beside player nametags.");
        add("cape", "Cape", "Cape", "Green T-emblem cloth on your back.");
        add("fullbright", "Fullbright", "Bright", "Maximum brightness, always.");
        add("fps", "FPS HUD", "FPS", "Frames per second, top-left.");
        add("coords", "Coords HUD", "XYZ", "Position under the FPS line.");
        add("keys", "Keystrokes", "Keys", "WASD + mouse with live CPS.");
        add("armor", "Armor HUD", "Armor", "Gear with live durability.");
        add("potion", "Potion HUD", "Fx", "Active effects with timers.");
        add("sprint", "ToggleSprint", "Run", "Sprint without holding anything.");
        add("sneak", "ToggleSneak", "Sneak", "Stay sneaked hands-free.");
        add("gg", "Auto GG", "GG", "Says gg when matches end.");
    }

    private Mod add(String id, String name, String shortName, String desc) {
        Mod m = new Mod(id, name, shortName, desc);
        mods.add(m);
        return m;
    }

    private Section sec(Mod m, String title) {
        Section s = new Section(title);
        m.sections.add(s);
        return s;
    }

    private void rows() {
        for (Mod m : mods) {
            m.sections.clear();
            switch (m.id) {
                case "badge" -> {
                    sec(m, "Display").rows
                            .add(new ToggleRow("Enabled", () -> c().badge, () -> c().badge = !c().badge));
                    sec(m, "Display").rows.add(new ToggleRow("Show in F5 (self)",
                            () -> c().selfNametag, () -> c().selfNametag = !c().selfNametag));
                }
                case "cape" -> sec(m, "Display").rows
                        .add(new ToggleRow("Enabled", () -> c().cape, () -> c().cape = !c().cape));
                case "fullbright" -> sec(m, "Display").rows
                        .add(new ToggleRow("Enabled", () -> c().fullbright, () -> c().fullbright = !c().fullbright));
                case "fps" -> {
                    sec(m, "Display").rows
                            .add(new ToggleRow("Enabled", () -> c().fpsHud, () -> c().fpsHud = !c().fpsHud));
                    String[] colors = { "Green", "White", "Amber" };
                    sec(m, "Style").rows.add(new SegRow("Text Color", colors,
                            () -> idx(colors, cap(c().fpsColor)), i -> c().fpsColor = colors[i].toLowerCase()));
                }
                case "coords" -> sec(m, "Display").rows
                        .add(new ToggleRow("Enabled", () -> c().coordsHud, () -> c().coordsHud = !c().coordsHud));
                case "keys" -> {
                    sec(m, "Display").rows
                            .add(new ToggleRow("Enabled", () -> c().keystrokes, () -> c().keystrokes = !c().keystrokes));
                    sec(m, "Style").rows.add(new ToggleRow("Background",
                            () -> c().keyBackground, () -> c().keyBackground = !c().keyBackground));
                    sec(m, "Style").rows.add(new ToggleRow("Show CPS",
                            () -> c().keyShowCps, () -> c().keyShowCps = !c().keyShowCps));
                }
                case "armor" -> {
                    sec(m, "Display").rows
                            .add(new ToggleRow("Enabled", () -> c().armorHud, () -> c().armorHud = !c().armorHud));
                    sec(m, "Style").rows.add(new SegRow("List Mode", new String[] { "Vertical", "Horizontal" },
                            () -> c().armorMode.equals("horizontal") ? 1 : 0,
                            i -> c().armorMode = i == 1 ? "horizontal" : "vertical"));
                    sec(m, "Style").rows.add(new SegRow("Damage", new String[] { "Value", "Percent" },
                            () -> c().armorValue ? 0 : 1, i -> c().armorValue = i == 0));
                    sec(m, "Style").rows.add(new ToggleRow("Background",
                            () -> c().armorBackground, () -> c().armorBackground = !c().armorBackground));
                    sec(m, "Style").rows
                            .add(new SliderRow("Spacing", 0, 8, () -> c().armorGap, v -> c().armorGap = v));
                }
                case "potion" -> sec(m, "Display").rows
                        .add(new ToggleRow("Enabled", () -> c().potionHud, () -> c().potionHud = !c().potionHud));
                case "sprint" -> sec(m, "Display").rows.add(new ToggleRow("Enabled",
                        () -> c().toggleSprint, () -> c().toggleSprint = !c().toggleSprint));
                case "sneak" -> sec(m, "Display").rows.add(new ToggleRow("Enabled",
                        () -> c().toggleSneak, () -> c().toggleSneak = !c().toggleSneak));
                case "gg" -> {
                    sec(m, "Display").rows
                            .add(new ToggleRow("Enabled", () -> c().autoGG, () -> c().autoGG = !c().autoGG));
                    sec(m, "Timing").rows.add(
                            new SliderRow("Delay (ticks)", 10, 100, () -> c().ggDelay, v -> c().ggDelay = v));
                }
            }
        }
    }

    private static int idx(String[] options, String want) {
        for (int i = 0; i < options.length; i++) {
            if (options[i].equalsIgnoreCase(want)) return i;
        }
        return 0;
    }

    private static String cap(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private void resetMod(Mod m) {
        TyxConfig c = c();
        switch (m.id) {
            case "badge" -> { c.badge = true; c.selfNametag = true; }
            case "cape" -> c.cape = true;
            case "fullbright" -> c.fullbright = false;
            case "fps" -> { c.fpsHud = true; c.fpsColor = "green"; }
            case "coords" -> c.coordsHud = false;
            case "keys" -> { c.keystrokes = true; c.keyBackground = true; c.keyShowCps = true; }
            case "armor" -> {
                c.armorHud = true;
                c.armorMode = "vertical";
                c.armorValue = true;
                c.armorBackground = true;
                c.armorGap = 2;
            }
            case "potion" -> c.potionHud = true;
            case "sprint" -> c.toggleSprint = false;
            case "sneak" -> c.toggleSneak = false;
            case "gg" -> { c.autoGG = false; c.ggDelay = 30; }
        }
        save();
        rows();
    }

    // ── layout ───────────────────────────────────────────────────────────

    private void layout() {
        int pw = Math.min(760, width - 30);
        int ph = Math.min(430, height - 30);
        int px = (width - pw) / 2;
        int py = (height - ph) / 2;
        topY = py;
        railY = py + 40;
        railH = 26;
        bodyY = railY + railH + 8;
        bodyH = py + ph - bodyY - 34;
        int lx = px + 12;
        listW = listCollapsed ? 0 : 150;
        listX = lx;
        setX = lx + listW + (listCollapsed ? 0 : 10);
        prevW = 190;
        prevX = px + pw - 12 - prevW;
        setW = prevX - 10 - setX;
        pillHit.clear();
        closeX = px + pw - 30;
        closeY = py + 7;
        mmX = px + 12;
        mmW = 110;
        previewBtnY = bodyY;
        setY0 = bodyY;
        setH = bodyH;
        pillY = railY;
        pillH = railH;
    }

    // ── input ────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) return true;
        if (button != 0) return false;
        layout();
        if (mx >= closeX && mx < closeX + 22 && my >= closeY && my < closeY + 22) {
            close();
            return true;
        }
        if (mx >= mmX && mx < mmX + mmW && my >= topY + 6 && my < topY + 30) {
            listCollapsed = !listCollapsed;
            return true;
        }
        for (int i = 0; i < pillHit.size(); i++) {
            int[] r = pillHit.get(i);
            if (mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3]) {
                selected = Math.max(0, Math.min(i, mods.size() - 1));
                setY = 0;
                return true;
            }
        }
        Mod m = mods.get(Math.min(selected, mods.size() - 1));
        int ry = (int) (bodyY + 44 - setY);
        for (Section s : m.sections) {
            if (mx >= setX && mx < setX + 100 && my >= ry && my < ry + 20) {
                s.open = !s.open;
                return true;
            }
            ry += 20;
            if (!s.open) continue;
            for (Row r : s.rows) {
                if (mx >= setX && mx < setX + setW && my >= ry && my < ry + r.height()) {
                    if (r.press(mx, my)) return true;
                }
                ry += r.height() + 2;
            }
        }
        int bw = 86;
        if (mx >= prevX + prevW - bw * 2 - 6 && mx < prevX + prevW && my >= previewBtnY
                && my < previewBtnY + 20) {
            if (mx < prevX + prevW - bw - 6 + bw) {
                previewPlain = !previewPlain;
            } else {
                resetMod(m);
            }
            return true;
        }
        if (!listCollapsed) {
            int ly = bodyY + 30;
            for (int i = 0; i < mods.size(); i++) {
                if (mx >= listX && mx < listX + listW && my >= ly && my < ly + 22) {
                    selected = i;
                    setY = 0;
                    return true;
                }
                ly += 24;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (super.mouseDragged(mx, my, button, dx, dy)) return true;
        if (dragging != null && button == 0) {
            dragging.drag(mx);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = null;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        if (super.mouseScrolled(mx, my, h, v)) return true;
        layout();
        if (my >= railY && my < railY + railH) {
            railX = Math.max(0, railX - v * 20);
            return true;
        }
        if (my >= bodyY && my < bodyY + bodyH && mx >= setX && mx < setX + setW) {
            setY = Math.max(0, setY - v * 12);
            return true;
        }
        return false;
    }

    // ── render ───────────────────────────────────────────────────────────

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        var tr = textRenderer;
        layout();
        if (selected >= mods.size()) selected = 0;
        int pw = Math.min(760, width - 30);
        int ph = Math.min(430, height - 30);
        int px = (width - pw) / 2;
        int py = (height - ph) / 2;

        ctx.fill(0, 0, width, height, 0xC00E1210);
        // Top bar.
        ctx.fill(mmX, topY + 6, mmX + mmW, topY + 30, ACC);
        ctx.drawCenteredTextWithShadow(tr, Text.literal("MOD MENU"), mmX + mmW / 2, topY + 13, 0xFF0B0F0D);
        ctx.drawTextWithShadow(tr, Text.literal("✕"), closeX + 7, closeY + 6, ACC_HI);

        // Rail pills (wheel-scrollable).
        ctx.enableScissor(px + mmW + 16, railY, closeX - 8, railY + railH);
        int rx = (int) (px + mmW + 16 - railX);
        for (int i = 0; i < mods.size(); i++) {
            Mod mod = mods.get(i);
            int tw = tr.getWidth(mod.shortName) + 20;
            pillHit.add(new int[] { rx, railY, tw, railH });
            boolean sel = i == selected;
            ctx.fill(rx, railY, rx + tw, railY + railH, sel ? ACC : ROW);
            drawBorder(ctx, rx, railY, tw, railH, sel ? ACC_HI : LINE);
            ctx.drawTextWithShadow(tr, Text.literal(mod.shortName), rx + 10, railY + 8,
                    sel ? 0xFF0B0F0D : DIM);
            rx += tw + 6;
        }
        ctx.disableScissor();

        Mod m = mods.get(Math.min(selected, mods.size() - 1));

        // Left module list.
        if (!listCollapsed) {
            int ly = bodyY + 30;
            for (int i = 0; i < mods.size(); i++) {
                Mod o = mods.get(i);
                boolean sel = i == selected;
                boolean hov = mouseX >= listX && mouseX < listX + listW && mouseY >= ly
                        && mouseY < ly + 22;
                if (sel) ctx.fill(listX, ly, listX + listW, ly + 22, ACC);
                else if (hov) ctx.fill(listX, ly, listX + listW, ly + 22, ROW);
                boolean on = modOn(o.id);
                ctx.fill(listX + 8, ly + 8, listX + 12, ly + 12, on ? (sel ? 0xFF0B0F0D : ACC_HI) : FAINT);
                ctx.drawTextWithShadow(tr, Text.literal(o.name), listX + 18, ly + 6,
                        sel ? 0xFF0B0F0D : TXT);
                ly += 24;
            }
        }

        // Settings header + rows (scrollable).
        ctx.drawTextWithShadow(tr, Text.literal("ⓘ  " + m.desc), setX, bodyY + 8, DIM);
        ctx.enableScissor(setX, bodyY + 26, setX + setW, bodyY + bodyH);
        int ry = (int) (bodyY + 44 - setY);
        for (Section s : m.sections) {
            ctx.drawTextWithShadow(tr, Text.literal((s.open ? "▾ " : "▸ ") + s.title),
                    setX, ry + 5, TXT);
            ry += 20;
            if (!s.open) continue;
            for (Row r : s.rows) {
                r.draw(ctx, tr, setX, ry, setW);
                ry += r.height() + 2;
            }
        }
        ctx.disableScissor();

        // Right preview.
        int pvx = prevX, pvy = bodyY, pvw = prevW, pvh = bodyH;
        ctx.fill(pvx, pvy, pvx + pvw, pvy + pvh, previewPlain ? 0xFF101414 : ROW);
        drawBorder(ctx, pvx, pvy, pvw, pvh, LINE);
        drawPreview(ctx, m, pvx + 8, pvy + 8, pvw - 16, pvh - 16);
        int bw = 86;
        ctx.fill(pvx + pvw - bw * 2 - 6, previewBtnY, pvx + pvw - bw - 6 + bw, previewBtnY + 20,
                previewPlain ? ACC : ROW);
        drawBorder(ctx, pvx + pvw - bw * 2 - 6, previewBtnY, bw, 20, previewPlain ? ACC_HI : LINE);
        ctx.drawTextWithShadow(tr, Text.literal("Mod Preview"),
                pvx + pvw - bw * 2 - 6 + (bw - tr.getWidth("Mod Preview")) / 2, previewBtnY + 6,
                previewPlain ? 0xFF0B0F0D : DIM);
        ctx.fill(pvx + pvw - bw, previewBtnY, pvx + pvw, previewBtnY + 20, ROW);
        drawBorder(ctx, pvx + pvw - bw, previewBtnY, bw, 20, LINE);
        ctx.drawTextWithShadow(tr, Text.literal("Reset"),
                pvx + pvw - bw + (bw - tr.getWidth("Reset")) / 2, previewBtnY + 6, DIM);

        // Footer.
        ctx.drawTextWithShadow(tr, Text.literal("Tyx Client 1.6.0 · Right Shift reopens · C zooms"),
                px + 12, py + ph - 20, FAINT);
        super.render(ctx, mouseX, mouseY, delta);
    }

    private boolean modOn(String id) {
        TyxConfig c = c();
        return switch (id) {
            case "badge" -> c.badge;
            case "cape" -> c.cape;
            case "fullbright" -> c.fullbright;
            case "fps" -> c.fpsHud;
            case "coords" -> c.coordsHud;
            case "keys" -> c.keystrokes;
            case "armor" -> c.armorHud;
            case "potion" -> c.potionHud;
            case "sprint" -> c.toggleSprint;
            case "sneak" -> c.toggleSneak;
            case "gg" -> c.autoGG;
            default -> false;
        };
    }

    // ── live previews (sample data, same painters as the HUD) ────────────

    private void drawPreview(DrawContext ctx, Mod m, int x, int y, int w, int h) {
        var tr = textRenderer;
        switch (m.id) {
            case "armor" -> {
                ItemStack[] gear = {
                    new ItemStack(Items.DIAMOND_HELMET),
                    new ItemStack(Items.DIAMOND_CHESTPLATE),
                    new ItemStack(Items.DIAMOND_LEGGINGS),
                    new ItemStack(Items.DIAMOND_BOOTS),
                    new ItemStack(Items.DIAMOND_SWORD),
                };
                int[] dmg = { 120, 300, 210, 150, 900 };
                boolean horiz = c().armorMode.equals("horizontal");
                boolean value = c().armorValue;
                int ax = x + 6, ay = y + 6;
                for (int i = 0; i < gear.length; i++) {
                    gear[i].setDamage(Math.min(dmg[i], gear[i].getMaxDamage() - 1));
                    int left = gear[i].getMaxDamage() - gear[i].getDamage();
                    int pct = left * 100 / gear[i].getMaxDamage();
                    String t = value ? String.valueOf(left) : pct + "%";
                    if (horiz) {
                        ctx.drawItem(gear[i], ax, ay);
                        ctx.drawTextWithShadow(tr, Text.literal(t), ax + 2, ay + 18, TXT);
                        ax += 34;
                    } else {
                        if (c().armorBackground) ctx.fill(ax - 2, ay - 2, ax + 60, ay + 20, 0x80000000);
                        ctx.drawItem(gear[i], ax, ay);
                        ctx.drawTextWithShadow(tr, Text.literal(t), ax + 20, ay + 5, TXT);
                        ay += 22 + c().armorGap;
                    }
                }
            }
            case "keys" -> {
                int s = 24, gap = 3, u = s + gap;
                int bx = x + 8, by = y + 8;
                keyBox(ctx, bx + s + gap, by, s, s, false, "W");
                keyBox(ctx, bx, by + u, s, s, true, "A");
                keyBox(ctx, bx + u, by + u, s, s, false, "S");
                keyBox(ctx, bx + 2 * u, by + u, s, s, false, "D");
                int cps = 4 + (int) ((System.currentTimeMillis() / 600) % 5);
                mouseBox(ctx, bx, by + 2 * u, (3 * s + gap) / 2, 24, true, "LMB",
                        c().keyShowCps ? cps : -1);
                mouseBox(ctx, bx + (3 * s + gap + 1) / 2, by + 2 * u, (3 * s + gap + 1) / 2, 24,
                        false, "RMB", c().keyShowCps ? cps - 1 : -1);
            }
            case "fps" -> {
                String fps = "144 FPS";
                int col = switch (c().fpsColor) {
                    case "white" -> 0xFFFFFF;
                    case "amber" -> 0xFFBF00;
                    default -> ACC_HI;
                };
                ctx.drawTextWithShadow(tr, Text.literal(fps), x + 6, y + 6, col);
            }
            case "coords" -> ctx.drawTextWithShadow(tr, Text.literal("100 / 64 / -200"), x + 6, y + 6, TXT);
            case "potion" -> {
                ctx.drawTextWithShadow(tr, Text.literal("Speed II  03:12"), x + 6, y + 6, 0x7DD3FC);
                ctx.drawTextWithShadow(tr, Text.literal("Strength I  01:05"), x + 6, y + 20, 0x7DD3FC);
            }
            case "badge" -> {
                ctx.fill(x + 6, y + 6, x + 14, y + 14, ACC);
                ctx.drawTextWithShadow(tr, Text.literal("T  Steve"), x + 18, y + 6, TXT);
            }
            case "cape" -> {
                ctx.fill(x + 6, y + 6, x + 26, y + 46, ACC);
                ctx.fill(x + 11, y + 14, x + 21, y + 20, 0xFF0B0F0D);
                ctx.fill(x + 14, y + 20, x + 18, y + 34, 0xFF0B0F0D);
            }
            case "fullbright" -> {
                ctx.fillGradient(x + 6, y + 6, x + w - 6, y + 26, 0xFFFFFF, 0xFFB0B0B0);
                ctx.drawTextWithShadow(tr, Text.literal("Gamma x16"), x + 6, y + 30, TXT);
            }
            default -> ctx.drawTextWithShadow(tr,
                    Text.literal(modOn(m.id) ? "Currently ON" : "Currently OFF"), x + 6, y + 6, DIM);
        }
    }

    private void keyBox(DrawContext ctx, int x, int y, int w, int h, boolean down, String label) {
        if (c().keyBackground) ctx.fill(x, y, x + w, y + h, down ? ACC : 0xCC0B0F0D);
        var tr = textRenderer;
        int tw = tr.getWidth(label);
        ctx.drawTextWithShadow(tr, Text.literal(label), x + (w - tw) / 2, y + (h - 9) / 2, TXT);
    }

    private void mouseBox(DrawContext ctx, int x, int y, int w, int h, boolean down, String label, int cps) {
        keyBox(ctx, x, y, w, h, down, label);
        if (cps >= 0) {
            var tr = textRenderer;
            String s = String.valueOf(cps);
            ctx.drawTextWithShadow(tr, Text.literal(s), x + (w - tr.getWidth(s)) / 2, y + 13, ACC_HI);
        }
    }

    private static void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y, x + 1, y + h, color);
        ctx.fill(x + w - 1, y, x + w, y + h, color);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }
}
