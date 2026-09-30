package gg.tyx.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Tiny persistent settings store (config/tyxclient.properties).
 * Everything the Right Shift menu toggles lives here.
 */
public final class TyxConfig {
    public boolean badge = true;
    /** Show your own nametag + Tyx badge above your head in F5 third-person. */
    public boolean selfNametag = true;
    public boolean fullbright = false;
    public boolean fpsHud = true;
    public boolean coordsHud = false;
    public boolean keystrokes = true;
    public boolean armorHud = true;
    public boolean potionHud = true;
    public boolean toggleSprint = false;
    public boolean toggleSneak = false;
    public boolean autoGG = false;
    public boolean cape = true;
    // Dawn-style per-mod settings (all wired to real rendering).
    public String fpsColor = "green"; // green | white | amber
    public boolean keyBackground = true;
    public boolean keyShowCps = true;
    public String armorMode = "vertical"; // vertical | horizontal
    public boolean armorValue = true; // true = remaining value, false = percent
    public boolean armorBackground = true;
    public int armorGap = 2; // px between armor rows, 0–8
    public int ggDelay = 30; // ticks to wait before saying gg, 10–100

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("tyxclient.properties");
    }

    private static boolean get(Properties p, String key, boolean def) {
        return Boolean.parseBoolean(p.getProperty(key, Boolean.toString(def)));
    }

    public static TyxConfig load() {
        TyxConfig c = new TyxConfig();
        try {
            Path f = file();
            if (Files.exists(f)) {
                Properties p = new Properties();
                try (var in = Files.newInputStream(f)) {
                    p.load(in);
                }
                c.badge = get(p, "badge", true);
                c.selfNametag = get(p, "selfNametag", true);
                c.fullbright = get(p, "fullbright", false);
                c.fpsHud = get(p, "fpsHud", true);
                c.coordsHud = get(p, "coordsHud", false);
                c.keystrokes = get(p, "keystrokes", true);
                c.armorHud = get(p, "armorHud", true);
                c.potionHud = get(p, "potionHud", true);
                c.toggleSprint = get(p, "toggleSprint", false);
                c.toggleSneak = get(p, "toggleSneak", false);
                c.autoGG = get(p, "autoGG", false);
                c.cape = get(p, "cape", true);
                c.fpsColor = p.getProperty("fpsColor", "green");
                c.keyBackground = get(p, "keyBackground", true);
                c.keyShowCps = get(p, "keyShowCps", true);
                c.armorMode = p.getProperty("armorMode", "vertical");
                c.armorValue = get(p, "armorValue", true);
                c.armorBackground = get(p, "armorBackground", true);
                try {
                    c.armorGap = Math.max(0, Math.min(8, Integer.parseInt(p.getProperty("armorGap", "2"))));
                } catch (NumberFormatException e) {
                    c.armorGap = 2;
                }
                try {
                    c.ggDelay = Math.max(10, Math.min(100, Integer.parseInt(p.getProperty("ggDelay", "30"))));
                } catch (NumberFormatException e) {
                    c.ggDelay = 30;
                }
            }
        } catch (IOException ignored) {
            // Corrupt config = defaults, never crash the game.
        }
        return c;
    }

    public void save() {
        try {
            Properties p = new Properties();
            p.setProperty("badge", Boolean.toString(badge));
            p.setProperty("selfNametag", Boolean.toString(selfNametag));
            p.setProperty("fullbright", Boolean.toString(fullbright));
            p.setProperty("fpsHud", Boolean.toString(fpsHud));
            p.setProperty("coordsHud", Boolean.toString(coordsHud));
            p.setProperty("keystrokes", Boolean.toString(keystrokes));
            p.setProperty("armorHud", Boolean.toString(armorHud));
            p.setProperty("potionHud", Boolean.toString(potionHud));
            p.setProperty("toggleSprint", Boolean.toString(toggleSprint));
            p.setProperty("toggleSneak", Boolean.toString(toggleSneak));
            p.setProperty("autoGG", Boolean.toString(autoGG));
            p.setProperty("cape", Boolean.toString(cape));
            p.setProperty("fpsColor", fpsColor);
            p.setProperty("keyBackground", Boolean.toString(keyBackground));
            p.setProperty("keyShowCps", Boolean.toString(keyShowCps));
            p.setProperty("armorMode", armorMode);
            p.setProperty("armorValue", Boolean.toString(armorValue));
            p.setProperty("armorBackground", Boolean.toString(armorBackground));
            p.setProperty("armorGap", Integer.toString(armorGap));
            p.setProperty("ggDelay", Integer.toString(ggDelay));
            Path f = file();
            Files.createDirectories(f.getParent());
            try (var out = Files.newOutputStream(f)) {
                p.store(out, "Tyx Client");
            }
        } catch (IOException ignored) {
            // Best effort.
        }
    }
}
