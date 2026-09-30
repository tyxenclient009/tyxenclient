package net.tyxen.hud.gui.screens;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_320;
import net.minecraft.class_332;
import net.minecraft.class_407;
import net.minecraft.class_437;

/**
 * Tyxen cape shop — store jaisa look, sirf capes.
 * Discord members ko free, EQUIP dabao = peeth pe cape.
 */
@Environment(value = EnvType.CLIENT)
public class CapeScreen extends class_437 {
    private static final class CapeEntry {
        String id = "";
        String name = "";
        boolean equipped = false;
    }

    private final class_437 previous;
    private String status = "Loading...";
    private final List<CapeEntry> capes = new ArrayList<CapeEntry>();
    private int cx;
    private int cy;
    private int pw = 520;
    private int ph = 340;

    public CapeScreen(class_437 previousScreen) {
        super((class_2561)class_2561.method_43470((String)"Tyxen Capes"));
        this.previous = previousScreen;
    }

    @Override
    protected void method_25426() {
        this.cx = this.field_22789 / 2;
        this.cy = this.field_22790 / 2;
        this.refresh();
    }

    private static String base() {
        try {
            return net.tyxen.hud.network.TyxenBackend.apiBase();
        } catch (Exception e) {
            return "http://localhost:8787";
        }
    }

    private String username() {
        try {
            class_310 client = class_310.method_1551();
            Object s = client.method_1548();
            if (s != null) {
                return ((class_320)s).method_1676();
            }
        } catch (Exception e) {
        }
        return null;
    }

    private void refresh() {
        final String user = this.username();
        if (user == null) {
            this.status = "Loading...";
            return;
        }
        new Thread(() -> {
            try {
                HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3L)).build();
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(CapeScreen.base() + "/api/tyxen/capes?username=" + user))
                        .timeout(Duration.ofSeconds(5L)).GET().build();
                String body = http.send(req, HttpResponse.BodyHandlers.ofString()).body();
                List<CapeEntry> list = new ArrayList<CapeEntry>();
                int idx = 0;
                while ((idx = body.indexOf("\"id\":\"", idx)) >= 0) {
                    int s1 = idx + 6;
                    int e1 = body.indexOf("\"", s1);
                    int n0 = body.indexOf("\"name\":\"", e1) + 8;
                    int n1 = body.indexOf("\"", n0);
                    CapeEntry ce = new CapeEntry();
                    ce.id = body.substring(s1, e1);
                    ce.name = body.substring(n0, n1);
                    int eq = body.indexOf("\"equipped\":true", e1);
                    int next = body.indexOf("\"id\":\"", e1);
                    ce.equipped = eq >= 0 && (next < 0 || eq < next);
                    list.add(ce);
                    idx = e1;
                }
                this.capes.clear();
                this.capes.addAll(list);
                this.status = list.isEmpty() ? "Join our Discord to unlock capes." : list.size() + " cape(s) — EQUIP to wear.";
            } catch (Exception e) {
                this.status = "Server offline — start tyxen-server first.";
            }
        }).start();
    }

    private void toggle(String id, boolean equip) {
        final String user = this.username();
        if (user == null) {
            return;
        }
        if (id == null) {
            try {
                class_407.method_60866((class_437)this, (String)"https://dsc.gg/tyxenclient", (boolean)true);
            } catch (Exception e) {
            }
            return;
        }
        final String path = equip ? "/api/tyxen/cape/equip" : "/api/tyxen/cape/unequip";
        final String eid = id;
        this.status = equip ? "Equipping..." : "Removing...";
        new Thread(() -> {
            try {
                String json = "{\"username\":\"" + user + "\",\"id\":\"" + eid + "\"}";
                HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3L)).build();
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(CapeScreen.base() + path))
                        .timeout(Duration.ofSeconds(8L))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json)).build();
                String body = http.send(req, HttpResponse.BodyHandlers.ofString()).body();
                if (body.contains("\"ok\":true")) {
                    for (CapeEntry ce : this.capes) {
                        ce.equipped = equip && ce.id.equals(eid);
                    }
                    this.status = equip ? "Equipped! Rejoin to see it." : "Cape removed.";
                } else {
                    this.status = "Server said no. Try again.";
                }
            } catch (Exception e) {
                this.status = "Server offline.";
            }
        }).start();
    }

    @Override
    public void method_25394(class_332 c, int mouseX, int mouseY, float delta) {
        c.method_25294(0, 0, this.field_22789, this.field_22790, 0xA0000000);
        int x = this.cx - this.pw / 2;
        int y = this.cy - this.ph / 2;
        c.method_25294(x, y, x + this.pw, y + this.ph, 0xF0101828);
        c.method_25294(x, y, x + this.pw, y + 2, 0xFF1E9BF0);
        c.method_25294(x, y + this.ph - 2, x + this.pw, y + this.ph, 0xFF1E9BF0);
        c.method_25294(x, y, x + 2, y + this.ph, 0xFF1E9BF0);
        c.method_25294(x + this.pw - 2, y, x + this.pw, y + this.ph, 0xFF1E9BF0);
        c.method_27534(this.field_22793, (class_2561)class_2561.method_43470((String)"TYXEN CAPES"), this.cx, y + 12, -1);
        c.method_27534(this.field_22793, (class_2561)class_2561.method_43470((String)this.status), this.cx, y + 30, -7697506);
        c.method_27534(this.field_22793, (class_2561)class_2561.method_43470((String)"x"), x + this.pw - 16, y + 8, -7697506);
        int cols = 3;
        int cw = 150;
        int chh = 150;
        int gap = 12;
        int gx = this.cx - (cols * cw + (cols - 1) * gap) / 2;
        int gy = y + 56;
        if (this.capes.isEmpty()) {
            c.method_27534(this.field_22793, (class_2561)class_2561.method_43470((String)"No capes yet."), this.cx, gy + 40, -7697506);
            c.method_25294(this.cx - 80, gy + 64, this.cx + 80, gy + 92, 0xFF1E9BF0);
            c.method_27534(this.field_22793, (class_2561)class_2561.method_43470((String)"JOIN DISCORD"), this.cx, gy + 73, -1);
        } else {
            int i = 0;
            for (CapeEntry ce : new ArrayList<CapeEntry>(this.capes)) {
                int col = i % cols;
                int row = i / cols;
                int bx = gx + col * (cw + gap);
                int by = gy + row * (chh + gap);
                if (by + chh > y + this.ph - 16) {
                    break;
                }
                int border = ce.equipped ? 0xFF37E393 : 0xFF8B7BD4;
                c.method_25294(bx, by, bx + cw, by + chh, 0xE8070D16);
                c.method_25294(bx, by, bx + cw, by + 3, border);
                c.method_25294(bx, by + chh - 3, bx + cw, by + chh, border);
                c.method_25294(bx, by, bx + 3, by + chh, border);
                c.method_25294(bx + cw - 3, by, bx + cw, by + chh, border);
                c.method_27534(this.field_22793, (class_2561)class_2561.method_43470((String)"T"), bx + cw / 2, by + 26, ce.equipped ? 0xFF37E393 : -4605239);
                String nm = ce.name.length() > 18 ? ce.name.substring(0, 18) : ce.name;
                c.method_27534(this.field_22793, (class_2561)class_2561.method_43470((String)nm), bx + cw / 2, by + 66, -1);
                c.method_27534(this.field_22793, (class_2561)class_2561.method_43470((String)"FREE"), bx + cw / 2, by + 82, 0xFFC9BEEF);
                c.method_25294(bx + 20, by + 104, bx + cw - 20, by + 132, 0xFF1E9BF0);
                c.method_27534(this.field_22793, (class_2561)class_2561.method_43470((String)(ce.equipped ? "UNEQUIP" : "EQUIP")), bx + cw / 2, by + 112, -1);
                if (ce.equipped) {
                    c.method_27534(this.field_22793, (class_2561)class_2561.method_43470((String)"WORN"), bx + cw / 2, by + 8, 0xFF37E393);
                }
                ++i;
            }
        }
        super.method_25394(c, mouseX, mouseY, delta);
    }

    @Override
    public boolean method_25402(class_11909 click, boolean doubleClick) {
        if (click.method_74245() != 0) {
            return super.method_25402(click, doubleClick);
        }
        int x = this.cx - this.pw / 2;
        int y = this.cy - this.ph / 2;
        int mx = (int)click.comp_4798();
        int my = (int)click.comp_4799();
        if (mx >= x + this.pw - 24 && mx < x + this.pw - 4 && my >= y + 4 && my < y + 24) {
            class_310.method_1551().method_1507(this.previous);
            return true;
        }
        int cols = 3;
        int cw = 150;
        int chh = 150;
        int gap = 12;
        int gx = this.cx - (cols * cw + (cols - 1) * gap) / 2;
        int gy = y + 56;
        if (this.capes.isEmpty()) {
            if (mx >= this.cx - 80 && mx < this.cx + 80 && my >= gy + 64 && my < gy + 92) {
                this.toggle(null, false);
                return true;
            }
            return super.method_25402(click, doubleClick);
        }
        int i = 0;
        for (CapeEntry ce : new ArrayList<CapeEntry>(this.capes)) {
            int col = i % cols;
            int row = i / cols;
            int bx = gx + col * (cw + gap);
            int by = gy + row * (chh + gap);
            if (by + chh > y + this.ph - 16) {
                break;
            }
            if (mx >= bx + 20 && mx < bx + cw - 20 && my >= by + 104 && my < by + 132) {
                this.toggle(ce.id, !ce.equipped);
                return true;
            }
            ++i;
        }
        return super.method_25402(click, doubleClick);
    }
}
