package net.tyxen.hud.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/**
 * TYXEN backend roots — apna khud ka server.
 * config/tyxen-backend.json me badlo jab server host karo:
 *   { "api": "http://localhost:8787", "files": "http://localhost:8787" }
 * Default = local server (tyxen-server/server.cjs).
 */
public final class TyxenBackend {
    private static String api = null;
    private static String files = null;

    private TyxenBackend() {
    }

    private static synchronized void load() {
        if (api != null) {
            return;
        }
        api = "http://api.tyxen.space:11789";
        files = "http://api.tyxen.space:11789";
        try {
            Path p = FabricLoader.getInstance().getConfigDir().resolve("tyxen-backend.json");
            if (Files.exists(p)) {
                JsonObject o = new Gson().fromJson(Files.readString(p), JsonObject.class);
                if (o != null) {
                    if (o.has("api")) api = o.get("api").getAsString();
                    if (o.has("files")) files = o.get("files").getAsString();
                }
            }
        } catch (Exception e) {
        }
    }

    public static String apiBase() {
        TyxenBackend.load();
        return api;
    }

    public static String filesBase() {
        TyxenBackend.load();
        return files;
    }
}
