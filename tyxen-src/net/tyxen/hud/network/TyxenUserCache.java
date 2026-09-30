/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.tyxen.hud.network;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.TyxenHUDClient;

@Environment(value=EnvType.CLIENT)
public final class TyxenUserCache {
    private static String manifestUrl() {
        return TyxenBackend.filesBase() + "/tyxen/active-users.txt";
    }

    private static String md5Url() {
        return TyxenBackend.filesBase() + "/tyxen/players.md5";
    }

    private static String pingUrl() {
        return TyxenBackend.apiBase() + "/api/tyxen/ping";
    }
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5L);
    private static final long REFRESH_INTERVAL_BASE_SEC = 60L;
    private static final long REFRESH_JITTER_SEC = 10L;
    // Heartbeat: server badges only live pings (5-min TTL), so re-ping
    // well inside the TTL while in-game — otherwise our own badge would
    // flicker off for everyone every few minutes on long sessions.
    private static final long HEARTBEAT_INTERVAL_SEC = 120L;
    private static volatile TyxenUserCache instance;
    private volatile Set<String> activeUsers = Collections.emptySet();
    private String lastMd5;
    private volatile String localUsername;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3L)).build();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Tyxen-UserCache");
        t.setDaemon(true);
        return t;
    });

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static TyxenUserCache getInstance() {
        if (instance != null) return instance;
        Class<TyxenUserCache> clazz = TyxenUserCache.class;
        synchronized (TyxenUserCache.class) {
            if (instance != null) return instance;
            instance = new TyxenUserCache();
            // ** MonitorExit[var0] (shouldn't be in output)
            return instance;
        }
    }

    private TyxenUserCache() {
        TyxenHUDClient.LOGGER.info("[TyxenUserCache] Initialised \u2014 fetching immediately");
        this.scheduler.schedule(this::refreshAndReschedule, 0L, TimeUnit.SECONDS);
        this.scheduler.scheduleAtFixedRate(() -> {
            String local = this.localUsername;
            if (local != null) {
                this.pingServer(local);
            }
        }, HEARTBEAT_INTERVAL_SEC, HEARTBEAT_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    public boolean isTyxenUser(String username) {
        return username != null && this.activeUsers.contains(username.toLowerCase(Locale.ROOT));
    }

    public void pingServer(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        this.localUsername = username;
        TyxenHUDClient.LOGGER.info("[TyxenUserCache] Pinging server for user: {}", (Object)username);
        this.scheduler.execute(() -> {
            try {
                String body = "{\"username\":\"" + username + "\"}";
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(TyxenUserCache.pingUrl())).timeout(REQUEST_TIMEOUT).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
                HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                TyxenHUDClient.LOGGER.info("[TyxenUserCache] Ping response: {}", (Object)response.statusCode());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    this.addActiveUser(username);
                }
            }
            catch (Exception e) {
                TyxenHUDClient.LOGGER.warn("[TyxenUserCache] Ping failed: {}", (Object)e.getMessage());
            }
        });
    }

    public void onDisconnect() {
        TyxenHUDClient.LOGGER.info("[TyxenUserCache] Disconnected \u2014 clearing {} cached users", (Object)this.activeUsers.size());
        this.activeUsers = Collections.emptySet();
        this.lastMd5 = null;
    }

    private void refreshAndReschedule() {
        this.refresh();
        long nextInterval = 60L + (long)(Math.random() * 10.0 * 2.0) - 10L;
        TyxenHUDClient.LOGGER.info("[TyxenUserCache] Next refresh in {}s", (Object)nextInterval);
        this.scheduler.schedule(this::refreshAndReschedule, nextInterval, TimeUnit.SECONDS);
    }

    private void refresh() {
        block13: {
            try {
                HttpRequest md5Request = HttpRequest.newBuilder().uri(URI.create(TyxenUserCache.md5Url())).timeout(REQUEST_TIMEOUT).GET().build();
                HttpResponse<String> md5Response = this.httpClient.send(md5Request, HttpResponse.BodyHandlers.ofString());
                if (md5Response.statusCode() != 200) {
                    TyxenHUDClient.LOGGER.warn("[TyxenUserCache] MD5 fetch returned {}", (Object)md5Response.statusCode());
                    return;
                }
                String remoteMd5 = md5Response.body().trim();
                if (remoteMd5.equals(this.lastMd5)) {
                    TyxenHUDClient.LOGGER.info("[TyxenUserCache] MD5 unchanged ({}) \u2014 skipping manifest fetch", (Object)remoteMd5);
                    return;
                }
                TyxenHUDClient.LOGGER.info("[TyxenUserCache] MD5 changed ({} -> {}) \u2014 fetching manifest", (Object)this.lastMd5, (Object)remoteMd5);
                HttpRequest manifestRequest = HttpRequest.newBuilder().uri(URI.create(TyxenUserCache.manifestUrl())).timeout(REQUEST_TIMEOUT).GET().build();
                HttpResponse<String> response = this.httpClient.send(manifestRequest, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    HashSet<String> users = new HashSet<String>();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream)new ByteArrayInputStream(response.body().getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));){
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.isBlank()) continue;
                            users.add(line.trim().toLowerCase(Locale.ROOT));
                        }
                    }
                    this.lastMd5 = remoteMd5;
                    this.activeUsers = users;
                    TyxenHUDClient.LOGGER.info("[TyxenUserCache] Loaded {} active users", (Object)users.size());
                    String local = this.localUsername;
                    if (local != null && !users.contains(local.toLowerCase(Locale.ROOT))) {
                        TyxenHUDClient.LOGGER.info("[TyxenUserCache] {} not in active users \u2014 re-pinging", (Object)local);
                        this.pingServer(local);
                    }
                    break block13;
                }
                TyxenHUDClient.LOGGER.warn("[TyxenUserCache] Unexpected status {} from manifest", (Object)response.statusCode());
            }
            catch (Exception e) {
                TyxenHUDClient.LOGGER.warn("[TyxenUserCache] Refresh failed: {}", (Object)e.getMessage());
            }
        }
    }

    private synchronized void addActiveUser(String username) {
        HashSet<String> users = new HashSet<String>(this.activeUsers);
        users.add(username.toLowerCase(Locale.ROOT));
        this.activeUsers = Collections.unmodifiableSet(users);
    }
}

