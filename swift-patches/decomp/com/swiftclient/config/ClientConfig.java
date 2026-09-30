/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.fabricmc.loader.api.FabricLoader
 */
package com.swiftclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.swiftclient.config.Theme;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;

public final class ClientConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("swiftclient.json");
    private static Data data = new Data();
    private static final Path PROFILES = FabricLoader.getInstance().getConfigDir().resolve("swiftclient_profiles");
    private static List<String> presetCache;

    private ClientConfig() {
    }

    public static void load() {
        try {
            if (Files.exists(FILE, new LinkOption[0])) {
                data = (Data)GSON.fromJson(Files.readString(FILE), Data.class);
            }
            if (data == null) {
                data = new Data();
            }
            if (ClientConfig.data.modules == null) {
                ClientConfig.data.modules = new HashMap<String, Boolean>();
            }
            if (ClientConfig.data.options == null) {
                ClientConfig.data.options = new HashMap<String, Map<String, String>>();
            }
        }
        catch (Exception e) {
            System.err.println("[Swift Client] Warning: corrupted config; regenerating defaults: " + e.getMessage());
            data = new Data();
            ClientConfig.save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent(), new FileAttribute[0]);
            Files.writeString(FILE, (CharSequence)GSON.toJson((Object)data), new OpenOption[0]);
        }
        catch (IOException e) {
            System.err.println("[Swift Client] Could not save config: " + e.getMessage());
        }
    }

    public static boolean enabled(String name, boolean def) {
        return ClientConfig.data.modules.getOrDefault(name, def);
    }

    public static void setEnabled(String name, boolean value) {
        ClientConfig.data.modules.put(name, value);
        ClientConfig.save();
    }

    public static int getTheme() {
        return ClientConfig.data.accent;
    }

    public static void setTheme(int c) {
        ClientConfig.data.accent = c;
        ClientConfig.save();
    }

    public static int getX(String name, int def) {
        return ClientConfig.data.x.getOrDefault(name, def);
    }

    public static int getY(String name, int def) {
        return ClientConfig.data.y.getOrDefault(name, def);
    }

    public static void setPos(String name, int x, int y) {
        ClientConfig.data.x.put(name, x);
        ClientConfig.data.y.put(name, y);
        ClientConfig.save();
    }

    public static void setPosSilent(String name, int x, int y) {
        ClientConfig.data.x.put(name, x);
        ClientConfig.data.y.put(name, y);
    }

    public static boolean getBool(String module, String key, boolean def) {
        Map<String, String> m = ClientConfig.data.options.get(module);
        if (m == null || !m.containsKey(key)) {
            return def;
        }
        return Boolean.parseBoolean(m.get(key));
    }

    public static void setBool(String module, String key, boolean value) {
        ClientConfig.data.options.computeIfAbsent(module, k -> new HashMap()).put(key, Boolean.toString(value));
        ClientConfig.save();
    }

    public static int getInt(String module, String key, int def) {
        Map<String, String> m = ClientConfig.data.options.get(module);
        if (m == null || !m.containsKey(key)) {
            return def;
        }
        try {
            return Integer.parseInt(m.get(key));
        }
        catch (NumberFormatException e) {
            return def;
        }
    }

    public static void setInt(String module, String key, int value) {
        ClientConfig.data.options.computeIfAbsent(module, k -> new HashMap()).put(key, Integer.toString(value));
        ClientConfig.save();
    }

    public static void setIntSilent(String module, String key, int value) {
        ClientConfig.data.options.computeIfAbsent(module, k -> new HashMap()).put(key, Integer.toString(value));
    }

    public static List<String> accounts() {
        return ClientConfig.data.accounts;
    }

    public static void addAccount(String name) {
        ClientConfig.data.accounts.remove(name);
        ClientConfig.data.accounts.add(name);
        ClientConfig.save();
    }

    public static void removeAccount(String name) {
        ClientConfig.data.accounts.remove(name);
        ClientConfig.save();
    }

    public static List<String> presets() {
        if (presetCache != null) {
            return presetCache;
        }
        try {
            if (!Files.isDirectory(PROFILES, new LinkOption[0])) {
                presetCache = List.of();
                return presetCache;
            }
            try (Stream<Path> stream = Files.list(PROFILES);){
                presetCache = stream.filter(p -> p.getFileName().toString().endsWith(".json")).map(p -> p.getFileName().toString().replace(".json", "")).sorted().toList();
            }
        }
        catch (Exception e) {
            presetCache = List.of();
        }
        return presetCache;
    }

    public static void savePreset(String name) {
        try {
            Files.createDirectories(PROFILES, new FileAttribute[0]);
            Files.writeString(PROFILES.resolve(name + ".json"), (CharSequence)GSON.toJson((Object)data), new OpenOption[0]);
            presetCache = null;
        }
        catch (IOException e) {
            System.err.println("[Swift Client] Could not save preset: " + e.getMessage());
        }
    }

    public static boolean loadPreset(String name) {
        try {
            Path f = PROFILES.resolve(name + ".json");
            if (!Files.exists(f, new LinkOption[0])) {
                return false;
            }
            Data loaded = (Data)GSON.fromJson(Files.readString(f), Data.class);
            if (loaded == null) {
                return false;
            }
            if (loaded.modules == null) {
                loaded.modules = new HashMap<String, Boolean>();
            }
            if (loaded.options == null) {
                loaded.options = new HashMap<String, Map<String, String>>();
            }
            if (loaded.accounts == null) {
                loaded.accounts = new ArrayList<String>();
            }
            data = loaded;
            Theme.load(ClientConfig.data.accent);
            ClientConfig.save();
            presetCache = null;
            return true;
        }
        catch (Exception e) {
            System.err.println("[Swift Client] Could not load preset: " + e.getMessage());
            return false;
        }
    }

    public static void deletePreset(String name) {
        try {
            Files.deleteIfExists(PROFILES.resolve(name + ".json"));
            presetCache = null;
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public static String nextPresetName() {
        List<String> existing = ClientConfig.presets();
        int n = 1;
        while (existing.contains("Config " + n)) {
            ++n;
        }
        return "Config " + n;
    }

    private static final class Data {
        int accent = -12872002;
        Map<String, Boolean> modules = new HashMap<String, Boolean>();
        Map<String, Integer> x = new HashMap<String, Integer>();
        Map<String, Integer> y = new HashMap<String, Integer>();
        Map<String, Map<String, String>> options = new HashMap<String, Map<String, String>>();
        List<String> accounts = new ArrayList<String>();

        private Data() {
        }
    }
}

