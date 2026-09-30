package com.swiftclient.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TYX addition — server geolocation for the multiplayer list flag badges.
 * Resolves a server address to country + ISP via ip-api.com (free tier),
 * strictly off the render thread, throttled to stay inside the rate limit.
 * Results are cached for the session; unknown/failed hosts simply show no
 * badge (never an error, never a stall).
 */
public final class ServerGeo {
    public record Info(String code, String country, String isp) {
    }

    private static final Map<String, Info> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> PENDING = new ConcurrentHashMap<>();
    private static volatile long nextAt = 0L;

    private ServerGeo() {
    }

    /** Cached info, or null (lookup kicked off in the background). */
    public static Info get(String address) {
        String host = hostOf(address);
        if (host.isEmpty()) {
            return null;
        }
        Info hit = CACHE.get(host);
        if (hit != null) {
            return hit;
        }
        query(host);
        return null;
    }

    static String hostOf(String address) {
        if (address == null) {
            return "";
        }
        String h = address.trim();
        int colon = h.lastIndexOf(':');
        int bracket = h.lastIndexOf(']');
        if (colon > bracket) {
            h = h.substring(0, colon);
        }
        if (h.startsWith("[") && h.endsWith("]") && h.length() > 2) {
            h = h.substring(1, h.length() - 1);
        }
        return h.toLowerCase(Locale.ROOT);
    }

    private static void query(String host) {
        if (PENDING.putIfAbsent(host, Boolean.TRUE) != null) {
            return;
        }
        long wait = nextAt - System.currentTimeMillis();
        Thread t = new Thread(() -> {
            try {
                if (wait > 0) {
                    Thread.sleep(wait);
                }
                HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
                HttpRequest req = HttpRequest.newBuilder(
                        URI.create("http://ip-api.com/json/" + host + "?fields=status,country,countryCode,isp,org,query"))
                        .timeout(Duration.ofSeconds(8)).GET().build();
                String body = client.send(req, HttpResponse.BodyHandlers.ofString()).body();
                Info info = parse(body);
                if (info != null) {
                    CACHE.put(host, info);
                }
            } catch (Exception ignored) {
            } finally {
                PENDING.remove(host);
                nextAt = System.currentTimeMillis() + 1500L;
            }
        }, "swiftclient-geo");
        t.setDaemon(true);
        t.start();
    }

    static Info parse(String json) {
        if (json == null || !json.contains("\"status\":\"success\"")) {
            return null;
        }
        String code = str(json, "countryCode");
        String country = str(json, "country");
        String isp = str(json, "isp");
        if (isp.isEmpty()) {
            isp = str(json, "org");
        }
        if (code.isEmpty()) {
            return null;
        }
        code = code.toUpperCase(Locale.ROOT);
        if (country.isEmpty()) {
            country = code;
        }
        if (isp.isEmpty()) {
            isp = "Unknown ISP";
        }
        return new Info(code, country, isp);
    }

    private static String str(String json, String key) {
        String k = "\"" + key + "\":\"";
        int i = json.indexOf(k);
        if (i < 0) {
            return "";
        }
        i += k.length();
        int j = json.indexOf('"', i);
        if (j < 0) {
            return "";
        }
        return json.substring(i, j).replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
