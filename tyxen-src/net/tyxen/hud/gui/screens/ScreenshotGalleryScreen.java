package net.tyxen.hud.gui.screens;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_1060;
import net.minecraft.class_10799;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.tyxen.hud.launcher.LauncherRenderer;

/**
 * Essential-style screenshot gallery: every PNG in the screenshots folder
 * as a thumbnail grid (paginated), click any shot for a fullscreen viewer
 * with prev/next, delete (with confirm), open-folder and back.
 *
 * The top-bar camera button opens this instead of silently snapping —
 * F2 still takes screenshots the vanilla way, and TAKE does it from here.
 */
@Environment(value = EnvType.CLIENT)
public class ScreenshotGalleryScreen extends class_437 {
    private static final int PAGE_SIZE = 6;
    private static final int COLS = 3;
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT);
    private static int texCounter = 0;

    private static final class Shot {
        File file;
        String name = "";
        String date = "";
        int w;
        int h;
        class_2960 texId;
        class_1043 tex;
    }

    private final class_437 previous;
    private final List<Shot> shots = new ArrayList<Shot>();
    private String status = "";
    private int cx;
    private int cy;
    private int pw = 640;
    private int ph = 440;
    private int page;
    /** -1 = grid, otherwise index into shots = fullscreen viewer. */
    private int viewer = -1;
    /** File awaiting delete-confirm (second click deletes). */
    private File deleteArm;
    private long deleteArmAt;
    /** Auto rescan a moment after TAKE (vanilla saves async). */
    private long takeStamp;

    public ScreenshotGalleryScreen(class_437 previousScreen) {
        super((class_2561)class_2561.method_43470((String)"Screenshots"));
        this.previous = previousScreen;
    }

    @Override
    protected void method_25426() {
        this.cx = this.field_22789 / 2;
        this.cy = this.field_22790 / 2;
        // Fit small windows: shrink the panel, never below usable.
        this.pw = Math.max(480, Math.min(640, this.field_22789 - 40));
        this.ph = Math.max(360, Math.min(440, this.field_22790 - 40));
        this.scan();
    }

    private File shotsDir() {
        try {
            class_310 mc = class_310.method_1551();
            if (mc != null && mc.field_1697 != null) {
                return new File(mc.field_1697, "screenshots");
            }
        } catch (Throwable ignore) {
        }
        return new File("screenshots");
    }

    private void scan() {
        this.clearThumbs();
        this.shots.clear();
        this.viewer = -1;
        this.deleteArm = null;
        try {
            File dir = this.shotsDir();
            File[] files = dir.exists() ? dir.listFiles() : null;
            if (files != null) {
                for (File f : files) {
                    String n = f.getName().toLowerCase(Locale.ROOT);
                    if (f.isFile() && (n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg"))) {
                        Shot s = new Shot();
                        s.file = f;
                        s.name = f.getName();
                        s.date = DATE_FMT.format(new Date(f.lastModified()));
                        this.shots.add(s);
                    }
                }
                this.shots.sort((a, b) -> Long.compare(b.file.lastModified(), a.file.lastModified()));
            }
            int pages = this.pages();
            if (this.page >= pages) {
                this.page = Math.max(0, pages - 1);
            }
            this.status = this.shots.isEmpty() ? "No screenshots yet — press F2 or TAKE." : "";
        } catch (Throwable t) {
            this.status = "Could not read screenshots folder.";
        }
    }

    private int pages() {
        return Math.max(1, (this.shots.size() + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    /** Upload the GPU texture for a shot on demand (render thread). */
    private void ensureTex(Shot s) {
        if (s.texId != null) {
            return;
        }
        try {
            class_1011 img = class_1011.method_4309(new FileInputStream(s.file));
            s.w = img.method_4307();
            s.h = img.method_4323();
            class_1043 tex = new class_1043(() -> "tyxen-shot", img);
            tex.method_4524();
            String path = ("shots/s" + (texCounter++) + ".png").toLowerCase(Locale.ROOT);
            class_2960 id = class_2960.method_60655((String)"tyxen", (String)path);
            class_310.method_1551().method_1531().method_4616(id, (class_1044)tex);
            s.tex = tex;
            s.texId = id;
        } catch (Throwable ignore) {
        }
    }

    private void dropTex(Shot s) {
        if (s.texId != null) {
            try {
                class_310.method_1551().method_1531().method_4615(s.texId);
            } catch (Throwable ignore) {
            }
            s.texId = null;
            s.tex = null;
        }
    }

    private void clearThumbs() {
        for (Shot s : this.shots) {
            this.dropTex(s);
        }
    }

    private void closeToPrevious() {
        this.clearThumbs();
        try {
            class_310.method_1551().method_1507(this.previous);
        } catch (Throwable ignore) {
        }
    }

    private static void openFolder(File dir) {
        boolean opened = false;
        try {
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows")) {
                new ProcessBuilder("explorer.exe", dir.getAbsolutePath()).start();
                opened = true;
            }
        } catch (Throwable ignore) {
        }
        if (!opened) {
            try {
                class_156.method_668().method_672(dir);
            } catch (Throwable ignore) {
            }
        }
    }

    private static boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private void drawThumb(class_332 c, Shot s, int x, int y, int w, int h) {
        c.method_25294(x, y, x + w, y + h, 0xE8070D16);
        if (s.texId != null && s.w > 0 && s.h > 0) {
            float scale = Math.min((float) w / (float) s.w, (float) h / (float) s.h);
            int dw = Math.max(1, Math.round((float) s.w * scale));
            int dh = Math.max(1, Math.round((float) s.h * scale));
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            c.method_25291(class_10799.field_56883, s.texId, dx, dy, 0.0f, 0.0f, dw, dh, s.w, s.h, -1);
        } else {
            c.method_27534(this.field_22793, (class_2561)class_2561.method_43470((String)"?"), x + w / 2, y + h / 2 - 4, -7697506);
        }
    }

    private void button(class_332 c, int x, int y, int w, int h, String label, boolean danger) {
        c.method_25294(x, y, x + w, y + h, danger ? 0xFF5A2A2A : 0xFF1E9BF0);
        c.method_27534(this.field_22793, (class_2561)class_2561.method_43470((String)label), x + w / 2, y + h / 2 - 4, -1);
    }

    @Override
    public void method_25394(class_332 c, int mouseX, int mouseY, float delta) {
        // TAKE saves async — rescan once its file should exist.
        if (this.takeStamp > 0 && System.currentTimeMillis() - this.takeStamp > 3000) {
            this.takeStamp = 0;
            this.scan();
        }
        c.method_25294(0, 0, this.field_22789, this.field_22790, 0xA0000000);
        int x = this.cx - this.pw / 2;
        int y = this.cy - this.ph / 2;
        c.method_25294(x, y, x + this.pw, y + this.ph, 0xF0101828);
        c.method_25294(x, y, x + this.pw, y + 2, 0xFF1E9BF0);
        c.method_25294(x, y + this.ph - 2, x + this.pw, y + this.ph, 0xFF1E9BF0);
        c.method_25294(x, y, x + 2, y + this.ph, 0xFF1E9BF0);
        c.method_25294(x + this.pw - 2, y, x + this.pw, y + this.ph, 0xFF1E9BF0);
        String title = this.viewer >= 0 ? "SCREENSHOT " + (this.viewer + 1) + " / " + this.shots.size()
                : "SCREENSHOTS (" + this.shots.size() + ")";
        c.method_27534(this.field_22793, (class_2561)class_2561.method_43470((String)title), this.cx, y + 12, -1);
        if (!this.status.isEmpty()) {
            c.method_27534(this.field_22793, (class_2561)class_2561.method_43470((String)this.status), this.cx, y + 30, -7697506);
        }
        // Header buttons: TAKE / FOLDER / REFRESH / X.
        int bh = 20;
        int by = y + 8;
        int bx = x + this.pw - 16;
        this.button(c, bx - 20, by, 20, bh, "x", false);
        bx -= 20 + 6;
        this.button(c, bx - 70, by, 70, bh, "REFRESH", false);
        bx -= 70 + 6;
        this.button(c, bx - 70, by, 70, bh, "FOLDER", false);
        bx -= 70 + 6;
        this.button(c, bx - 70, by, 70, bh, "TAKE", false);
        if (this.viewer >= 0 && this.viewer < this.shots.size()) {
            this.renderViewer(c, this.shots.get(this.viewer), x, y);
        } else {
            this.renderGrid(c, x, y, mouseX, mouseY);
        }
        super.method_25394(c, mouseX, mouseY, delta);
    }

    private void renderGrid(class_332 c, int x, int y, int mouseX, int mouseY) {
        if (this.shots.isEmpty()) {
            c.method_27534(this.field_22793, (class_2561)class_2561.method_43470((String)"Press F2 in game — shots land here."), this.cx, y + this.ph / 2 - 10, -7697506);
            c.method_27534(this.field_22793, (class_2561)class_2561.method_43470((String)"Or hit TAKE above for one right now."), this.cx, y + this.ph / 2 + 6, -7697506);
            return;
        }
        int cols = COLS;
        int gap = 12;
        int tw = (this.pw - 48 - (cols - 1) * gap) / cols;
        int th = tw * 9 / 16;
        int labelH = 24;
        int cellH = th + labelH;
        int gx = x + 24;
        int gy = y + 64;
        int start = this.page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, this.shots.size());
        for (int i = start; i < end; i++) {
            Shot s = this.shots.get(i);
            this.ensureTex(s);
            int k = i - start;
            int col = k % cols;
            int row = k / cols;
            int bx = gx + col * (tw + gap);
            int by = gy + row * (cellH + gap);
            if (by + cellH > y + this.ph - 34) {
                break;
            }
            boolean hov = inside(mouseX, mouseY, bx, by, tw, cellH);
            this.drawThumb(c, s, bx, by, tw, th);
            c.method_25294(bx, by, bx + tw, by + 1, hov ? 0xFF1E9BF0 : 0xFF3A3F55);
            c.method_25294(bx, by + th - 1, bx + tw, by + th, hov ? 0xFF1E9BF0 : 0xFF3A3F55);
            c.method_25294(bx, by, bx + 1, by + th, hov ? 0xFF1E9BF0 : 0xFF3A3F55);
            c.method_25294(bx + tw - 1, by, bx + tw, by + th, hov ? 0xFF1E9BF0 : 0xFF3A3F55);
            String nm = s.name.length() > 22 ? s.name.substring(0, 22) : s.name;
            c.method_27534(this.field_22793, (class_2561)class_2561.method_43470((String)nm), bx + tw / 2, by + th + 3, -1);
            c.method_27534(this.field_22793, (class_2561)class_2561.method_43470((String)s.date), bx + tw / 2, by + th + 13, -7697506);
        }
        // Footer pager.
        String pg = (this.page + 1) + " / " + this.pages();
        c.method_27534(this.field_22793, (class_2561)class_2561.method_43470((String)pg), this.cx, y + this.ph - 24, -7697506);
        if (this.page > 0) {
            this.button(c, this.cx - 70, y + this.ph - 30, 44, 18, "<", false);
        }
        if (this.page + 1 < this.pages()) {
            this.button(c, this.cx + 26, y + this.ph - 30, 44, 18, ">", false);
        }
    }

    private void renderViewer(class_332 c, Shot s, int x, int y) {
        this.ensureTex(s);
        int areaX = x + 24;
        int areaY = y + 64;
        int areaW = this.pw - 48;
        int areaH = this.ph - 64 - 66;
        if (s.texId != null && s.w > 0 && s.h > 0) {
            float scale = Math.min((float) areaW / (float) s.w, (float) areaH / (float) s.h);
            int dw = Math.max(1, Math.round((float) s.w * scale));
            int dh = Math.max(1, Math.round((float) s.h * scale));
            int dx = areaX + (areaW - dw) / 2;
            int dy = areaY + (areaH - dh) / 2;
            c.method_25294(dx - 1, dy - 1, dx + dw + 1, dy + dh + 1, 0xFF1E9BF0);
            c.method_25291(class_10799.field_56883, s.texId, dx, dy, 0.0f, 0.0f, dw, dh, s.w, s.h, -1);
        } else {
            c.method_27534(this.field_22793, (class_2561)class_2561.method_43470((String)"Could not load image."), this.cx, areaY + areaH / 2, -7697506);
        }
        c.method_27534(this.field_22793, (class_2561)class_2561.method_43470((String)(s.name + "  ·  " + s.date)), this.cx, y + this.ph - 56, -1);
        // Bottom bar: < > DELETE FOLDER BACK.
        int bh = 20;
        int by = y + this.ph - 32;
        this.button(c, this.cx - 190, by, 40, bh, "<", false);
        this.button(c, this.cx - 144, by, 40, bh, ">", false);
        boolean armed = s.file.equals(this.deleteArm) && System.currentTimeMillis() - this.deleteArmAt < 5000;
        this.button(c, this.cx - 98, by, armed ? 110 : 70, bh, armed ? "CONFIRM?" : "DELETE", true);
        int fx = this.cx + (armed ? 18 : -22);
        this.button(c, fx, by, 70, bh, "FOLDER", false);
        this.button(c, fx + 76, by, 60, bh, "BACK", false);
    }

    @Override
    public boolean method_25402(class_11909 click, boolean doubleClick) {
        if (click.method_74245() != 0) {
            return super.method_25402(click, doubleClick);
        }
        int mx = (int) click.comp_4798();
        int my = (int) click.comp_4799();
        int x = this.cx - this.pw / 2;
        int y = this.cy - this.ph / 2;
        int bh = 20;
        int by = y + 8;
        int bx = x + this.pw - 16;
        // X close.
        if (inside(mx, my, bx - 20, by, 20, bh)) {
            this.closeToPrevious();
            return true;
        }
        bx -= 20 + 6;
        // REFRESH.
        if (inside(mx, my, bx - 70, by, 70, bh)) {
            this.scan();
            this.status = this.shots.isEmpty() ? "No screenshots yet — press F2 or TAKE." : "Refreshed.";
            return true;
        }
        bx -= 70 + 6;
        // FOLDER.
        if (inside(mx, my, bx - 70, by, 70, bh)) {
            openFolder(this.shotsDir());
            return true;
        }
        bx -= 70 + 6;
        // TAKE.
        if (inside(mx, my, bx - 70, by, 70, bh)) {
            try {
                LauncherRenderer.takeScreenshot();
                this.status = "Saving…";
                this.takeStamp = System.currentTimeMillis();
            } catch (Throwable ignore) {
            }
            return true;
        }
        if (this.viewer >= 0 && this.viewer < this.shots.size()) {
            return this.clickViewer(mx, my, x, y);
        }
        return this.clickGrid(mx, my, x, y);
    }

    private boolean clickGrid(int mx, int my, int x, int y) {
        if (this.shots.isEmpty()) {
            return true;
        }
        // Pager.
        if (this.page > 0 && inside(mx, my, this.cx - 70, y + this.ph - 30, 44, 18)) {
            this.page--;
            return true;
        }
        if (this.page + 1 < this.pages() && inside(mx, my, this.cx + 26, y + this.ph - 30, 44, 18)) {
            this.page++;
            return true;
        }
        int cols = COLS;
        int gap = 12;
        int tw = (this.pw - 48 - (cols - 1) * gap) / cols;
        int th = tw * 9 / 16;
        int labelH = 24;
        int cellH = th + labelH;
        int gx = x + 24;
        int gy = y + 64;
        int start = this.page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, this.shots.size());
        for (int i = start; i < end; i++) {
            int k = i - start;
            int col = k % cols;
            int row = k / cols;
            int bx = gx + col * (tw + gap);
            int by = gy + row * (cellH + gap);
            if (by + cellH > y + this.ph - 34) {
                break;
            }
            if (inside(mx, my, bx, by, tw, cellH)) {
                this.viewer = i;
                this.deleteArm = null;
                return true;
            }
        }
        return true;
    }

    private boolean clickViewer(int mx, int my, int x, int y) {
        Shot s = this.shots.get(this.viewer);
        int bh = 20;
        int by = y + this.ph - 32;
        if (inside(mx, my, this.cx - 190, by, 40, bh)) {
            this.dropTex(s);
            this.viewer = (this.viewer - 1 + this.shots.size()) % this.shots.size();
            this.deleteArm = null;
            return true;
        }
        if (inside(mx, my, this.cx - 144, by, 40, bh)) {
            this.dropTex(s);
            this.viewer = (this.viewer + 1) % this.shots.size();
            this.deleteArm = null;
            return true;
        }
        boolean armed = s.file.equals(this.deleteArm) && System.currentTimeMillis() - this.deleteArmAt < 5000;
        int delW = armed ? 110 : 70;
        if (inside(mx, my, this.cx - 98, by, delW, bh)) {
            if (armed) {
                try {
                    this.dropTex(s);
                    if (s.file.delete()) {
                        this.status = "Deleted " + s.name;
                    } else {
                        this.status = "Could not delete file.";
                    }
                } catch (Throwable ignore) {
                    this.status = "Could not delete file.";
                }
                this.scan();
            } else {
                this.deleteArm = s.file;
                this.deleteArmAt = System.currentTimeMillis();
                this.status = "Click CONFIRM? to delete forever.";
            }
            return true;
        }
        int fx = this.cx + (armed ? 18 : -22);
        if (inside(mx, my, fx, by, 70, bh)) {
            openFolder(this.shotsDir());
            return true;
        }
        if (inside(mx, my, fx + 76, by, 60, bh)) {
            this.dropTex(s);
            this.viewer = -1;
            this.deleteArm = null;
            return true;
        }
        return true;
    }

    @Override
    public boolean method_25404(class_11908 event) {
        int keyCode = event.comp_4795();
        // Left / right browse in viewer, Esc steps back (viewer → grid → previous).
        if (this.viewer >= 0 && (keyCode == 262 || keyCode == 263)) {
            Shot s = this.shots.get(this.viewer);
            this.dropTex(s);
            if (keyCode == 262) {
                this.viewer = (this.viewer + 1) % this.shots.size();
            } else {
                this.viewer = (this.viewer - 1 + this.shots.size()) % this.shots.size();
            }
            this.deleteArm = null;
            return true;
        }
        if (keyCode == 256) {
            if (this.viewer >= 0) {
                Shot s = this.shots.get(this.viewer);
                this.dropTex(s);
                this.viewer = -1;
                this.deleteArm = null;
                return true;
            }
            this.closeToPrevious();
            return true;
        }
        return super.method_25404(event);
    }
}
