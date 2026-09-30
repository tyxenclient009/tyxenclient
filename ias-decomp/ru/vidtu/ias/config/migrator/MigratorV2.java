/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  org.jetbrains.annotations.Contract
 *  org.jetbrains.annotations.NotNull
 */
package ru.vidtu.ias.config.migrator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Predicate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.account.MicrosoftAccount;
import ru.vidtu.ias.account.OfflineAccount;
import ru.vidtu.ias.config.IASConfig;
import ru.vidtu.ias.config.IASStorage;
import ru.vidtu.ias.config.TextAlign;
import ru.vidtu.ias.config.migrator.Migrator;
import ru.vidtu.ias.crypt.DummyCrypt;
import ru.vidtu.ias.utils.GSONUtils;

final class MigratorV2
implements Migrator {
    MigratorV2() {
    }

    @Override
    public void load(@NotNull JsonObject json) {
        try {
            TextAlign titleTextAlign;
            int version = GSONUtils.getIntOrThrow(json, "version");
            if (version != 2) {
                throw new IllegalArgumentException("V2 has invalid version marker: " + version);
            }
            boolean titleText = json.has("titleScreenText") ? GSONUtils.getBooleanOrThrow(json, "titleScreenText") : IASConfig.titleText;
            String titleTextX = json.has("titleScreenTextX") ? GSONUtils.getStringOrThrow(json, "titleScreenTextX") : IASConfig.titleTextX;
            String titleTextY = json.has("titleScreenTextY") ? GSONUtils.getStringOrThrow(json, "titleScreenTextY") : IASConfig.titleTextY;
            String rawTitleTextAlign = json.has("titleScreenTextAlignment") ? GSONUtils.getStringOrThrow(json, "titleScreenTextAlignment").toUpperCase(Locale.ROOT) : null;
            try {
                titleTextAlign = rawTitleTextAlign != null ? TextAlign.valueOf(rawTitleTextAlign.toUpperCase(Locale.ROOT)) : IASConfig.titleTextAlign;
            }
            catch (Throwable ignored) {
                titleTextAlign = IASConfig.titleTextAlign;
            }
            boolean titleButton = json.has("titleScreenButton") ? GSONUtils.getBooleanOrThrow(json, "titleScreenButton") : IASConfig.titleButton;
            String titleButtonX = json.has("titleScreenButtonX") ? GSONUtils.getStringOrThrow(json, "titleScreenButtonX") : IASConfig.titleButtonX;
            String titleButtonY = json.has("titleScreenButtonY") ? GSONUtils.getStringOrThrow(json, "titleScreenButtonY") : IASConfig.titleButtonY;
            String serversButtonX = json.has("multiplayerScreenButtonX") ? GSONUtils.getStringOrThrow(json, "multiplayerScreenButtonX") : null;
            String serversButtonY = json.has("multiplayerScreenButtonY") ? GSONUtils.getStringOrThrow(json, "multiplayerScreenButtonY") : null;
            JsonArray rawAccounts = json.has("accounts") ? GSONUtils.getArrayOrThrow(json, "accounts") : new JsonArray(0);
            ArrayList<OfflineAccount> accounts = new ArrayList<OfflineAccount>(rawAccounts.size());
            for (JsonElement entry : rawAccounts) {
                OfflineAccount account;
                JsonObject rawAccount = entry.getAsJsonObject();
                String type = GSONUtils.getStringOrThrow(rawAccount, "type");
                switch (type.toLowerCase(Locale.ROOT)) {
                    case "ias:microsoft": 
                    case "ru.vidtu.ias.account.microsoftaccount": {
                        Account account2;
                        byte[] data;
                        byte[] unencrypted;
                        UUID uuid = UUID.fromString(GSONUtils.getStringOrThrow(rawAccount, "uuid"));
                        String name = GSONUtils.getStringOrThrow(rawAccount, "name");
                        String accessToken = GSONUtils.getStringOrThrow(rawAccount, "accessToken");
                        String refreshToken = GSONUtils.getStringOrThrow(rawAccount, "refreshToken");
                        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream(accessToken.length() + refreshToken.length() + 4);
                             DataOutputStream out = new DataOutputStream(byteOut);){
                            out.writeUTF(accessToken);
                            out.writeUTF(refreshToken);
                            unencrypted = byteOut.toByteArray();
                        }
                        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream(unencrypted.length + 32);
                             DataOutputStream out = new DataOutputStream(byteOut);){
                            DummyCrypt crypt = DummyCrypt.INSTANCE;
                            byte[] encrypted = crypt.encrypt(unencrypted);
                            out.writeUTF(crypt.type());
                            out.write(encrypted);
                            data = byteOut.toByteArray();
                        }
                        OfflineAccount offlineAccount = account2 = new MicrosoftAccount(true, uuid, name, data);
                        break;
                    }
                    case "ias:offline": 
                    case "ru.vidtu.ias.account.offlineaccount": {
                        Account account2;
                        String name = GSONUtils.getStringOrThrow(rawAccount, "name");
                        OfflineAccount offlineAccount = account2 = new OfflineAccount(name, null);
                        break;
                    }
                    default: {
                        Account account2;
                        OfflineAccount offlineAccount = account = (account2 = null);
                    }
                }
                if (account == null) {
                    return;
                }
                accounts.add(account);
            }
            titleButtonX = titleButtonX == null ? null : titleButtonX.replace("w", "%width%").replace("W", "%width%").replace("h", "%height%").replace("H", "%height%");
            titleButtonY = titleButtonY == null ? null : titleButtonY.replace("w", "%width%").replace("W", "%width%").replace("h", "%height%").replace("H", "%height%");
            serversButtonX = serversButtonX == null ? null : serversButtonX.replace("w", "%width%").replace("W", "%width%").replace("h", "%height%").replace("H", "%height%");
            serversButtonY = serversButtonY == null ? null : serversButtonY.replace("w", "%width%").replace("W", "%width%").replace("h", "%height%").replace("H", "%height%");
            IASConfig.titleText = titleText;
            IASConfig.titleTextX = titleTextX;
            IASConfig.titleTextY = titleTextY;
            IASConfig.titleTextAlign = titleTextAlign;
            IASConfig.titleButton = titleButton;
            IASConfig.titleButtonX = titleButtonX;
            IASConfig.titleButtonY = titleButtonY;
            IASConfig.serversButtonX = serversButtonX;
            IASConfig.serversButtonY = serversButtonY;
            IASStorage.ACCOUNTS.addAll(accounts);
            HashSet set = new HashSet(IASStorage.ACCOUNTS.size());
            IASStorage.ACCOUNTS.removeIf(Predicate.not(set::add));
        }
        catch (Throwable t) {
            String redacted = OBFUSCATE_LOGS.matcher(String.valueOf(json)).replaceAll("$1[TOKEN]");
            throw new JsonParseException("Unable to migrate V2 config: " + redacted, t);
        }
    }

    @Contract(pure=true)
    @NotNull
    public String toString() {
        return "MigratorV2{}";
    }
}

