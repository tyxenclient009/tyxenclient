/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  org.jetbrains.annotations.Contract
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package ru.vidtu.ias.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.config.ServerMode;
import ru.vidtu.ias.config.TextAlign;
import ru.vidtu.ias.config.migrator.Migrator;
import ru.vidtu.ias.utils.GSONUtils;
import ru.vidtu.ias.utils.IUtils;

public final class IASConfig {
    @NotNull
    private static final Gson GSON = new GsonBuilder().excludeFieldsWithModifiers(new int[]{128, 16}).create();
    @NotNull
    public static final Logger LOGGER = LoggerFactory.getLogger((String)"IAS/IASConfig");
    public static boolean titleText = true;
    @Nullable
    public static String titleTextX = null;
    @Nullable
    public static String titleTextY = null;
    @Nullable
    public static TextAlign titleTextAlign = TextAlign.LEFT;
    public static boolean titleButton = true;
    @Nullable
    public static String titleButtonX = null;
    @Nullable
    public static String titleButtonY = null;
    public static boolean serversText = true;
    @Nullable
    public static String serversTextX = null;
    @Nullable
    public static String serversTextY = null;
    @Nullable
    public static TextAlign serversTextAlign = TextAlign.LEFT;
    public static boolean serversButton = true;
    @Nullable
    public static String serversButtonX = null;
    @Nullable
    public static String serversButtonY = null;
    public static boolean allowNoCrypt = false;
    public static boolean nickWarns = true;
    public static boolean unexpectedPigs = true;
    public static boolean barNick = false;
    public static boolean closeOnLogin = false;
    @Nullable
    public static ServerMode server = ServerMode.AVAILABLE;
    public static boolean passwordEchoing = true;

    @Contract(pure=true)
    private IASConfig() {
    }

    public static void load(@NotNull Path path) {
        try {
            LOGGER.debug("IAS: Loading config for {}...", (Object)path);
            Path file = path.resolve("ias.json");
            if (!Files.isRegularFile(file, new LinkOption[0])) {
                LOGGER.debug("IAS: Config not found. Saving...");
                IASConfig.save(path);
                return;
            }
            String value = Files.readString(file);
            JsonObject json = (JsonObject)GSON.fromJson(value, JsonObject.class);
            int version = json.has("version") ? GSONUtils.getIntOrThrow(json, "version") : 1;
            LOGGER.trace("IAS: Loaded config version is {}.", (Object)version);
            Migrator migrator = Migrator.fromVersion(version);
            if (migrator != null) {
                LOGGER.info("IAS: Migrating old config version {} via {}.", (Object)version, (Object)migrator);
                migrator.load(json);
                LOGGER.info("IAS: Migrated old config.");
                IASConfig.save(path);
                return;
            }
            GSON.fromJson((JsonElement)json, IASConfig.class);
            LOGGER.debug("IAS: Config loaded.");
        }
        catch (Throwable t) {
            throw new RuntimeException("Unable to load IAS config.", t);
        }
        finally {
            titleTextAlign = Objects.requireNonNullElse(titleTextAlign, TextAlign.LEFT);
            serversTextAlign = Objects.requireNonNullElse(serversTextAlign, TextAlign.LEFT);
            server = Objects.requireNonNullElse(server, ServerMode.AVAILABLE);
        }
    }

    public static void save(@NotNull Path path) {
        try {
            LOGGER.debug("IAS: Saving config into {}...", (Object)path);
            titleTextAlign = Objects.requireNonNullElse(titleTextAlign, TextAlign.LEFT);
            serversTextAlign = Objects.requireNonNullElse(serversTextAlign, TextAlign.LEFT);
            server = Objects.requireNonNullElse(server, ServerMode.AVAILABLE);
            Path file = path.resolve("ias.json");
            JsonObject json = (JsonObject)GSON.toJsonTree((Object)new IASConfig());
            json.addProperty("version", (Number)3);
            String value = GSON.toJson((JsonElement)json);
            Files.createDirectories(file.getParent(), new FileAttribute[0]);
            Files.writeString(file, (CharSequence)value, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.SYNC, StandardOpenOption.DSYNC);
            LOGGER.debug("IAS: Config saved to {}.", (Object)file);
        }
        catch (Throwable t) {
            throw new RuntimeException("Unable to save IAS config.", t);
        }
    }

    public static boolean useServerAuth() {
        if (server == null) {
            return IUtils.canUseSunServer();
        }
        return switch (server) {
            default -> throw new MatchException(null, null);
            case ServerMode.ALWAYS -> true;
            case ServerMode.NEVER -> false;
            case ServerMode.AVAILABLE -> IUtils.canUseSunServer();
        };
    }
}

