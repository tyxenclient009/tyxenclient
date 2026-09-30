/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1657
 *  net.minecraft.class_310
 */
package net.fastclient.hud.store;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fastclient.hud.FastClientHUDClient;
import net.fastclient.hud.store.CosmeticRuntimeAdapter;
import net.fastclient.hud.store.MarketplaceClient;
import net.fastclient.hud.store.StoreCatalog;
import net.fastclient.hud.store.StoreCosmetic;
import net.minecraft.class_1657;
import net.minecraft.class_310;

@Environment(value=EnvType.CLIENT)
final class RemoteCosmeticResolver {
    private static final Duration PROFILE_TTL = Duration.ofSeconds(45L);
    private static final int MAX_BATCH_SIZE = 128;
    private final MarketplaceClient marketplaceClient;
    private final StoreCatalog catalog;
    private final CosmeticRuntimeAdapter runtimeAdapter;
    private final ConcurrentHashMap<String, CachedProfile> cache = new ConcurrentHashMap();
    private final Set<String> inFlight = ConcurrentHashMap.newKeySet();

    RemoteCosmeticResolver(MarketplaceClient marketplaceClient, StoreCatalog catalog, CosmeticRuntimeAdapter runtimeAdapter) {
        this.marketplaceClient = marketplaceClient;
        this.catalog = catalog;
        this.runtimeAdapter = runtimeAdapter;
    }

    void refreshVisiblePlayers(class_310 client) {
        if (client.field_1687 == null || client.field_1724 == null) {
            return;
        }
        long now = System.currentTimeMillis();
        String localUsername = client.field_1724.method_7334().name();
        ArrayList<String> staleUsernames = new ArrayList<String>();
        LinkedHashSet<String> visible = new LinkedHashSet<String>();
        for (class_1657 player : client.field_1687.method_18456()) {
            String username = player.method_7334().name();
            if (username == null || username.equalsIgnoreCase(localUsername)) continue;
            String key = RemoteCosmeticResolver.normalize(username);
            visible.add(key);
            CachedProfile cached = this.cache.get(key);
            if (cached != null && cached.expiresAtMillis() > now || !this.inFlight.add(key)) continue;
            staleUsernames.add(username);
            if (staleUsernames.size() < 128) continue;
            break;
        }
        this.clearPlayersNoLongerVisible(visible);
        if (staleUsernames.isEmpty()) {
            return;
        }
        FastClientHUDClient.LOGGER.info("[Store] Fetching public cosmetics for {} visible player(s)", (Object)staleUsernames.size());
        ((CompletableFuture)this.marketplaceClient.publicEquipped(staleUsernames).thenAccept(profiles -> client.method_63588(() -> {
            long expiresAt = System.currentTimeMillis() + PROFILE_TTL.toMillis();
            int equippedProfiles = 0;
            for (MarketplaceClient.PublicCosmeticProfile profile : profiles) {
                String key = RemoteCosmeticResolver.normalize(profile.username());
                this.inFlight.remove(key);
                this.cache.put(key, new CachedProfile(profile.equippedItemIds(), expiresAt));
                this.apply(profile.username(), profile.equippedItemIds());
                if (profile.equippedItemIds().isEmpty()) continue;
                ++equippedProfiles;
            }
            FastClientHUDClient.LOGGER.info("[Store] Public cosmetics updated: profiles={} withCosmetics={} cache={}", new Object[]{profiles.size(), equippedProfiles, this.cache.size()});
        }))).exceptionally(ex -> {
            for (String username : staleUsernames) {
                this.inFlight.remove(RemoteCosmeticResolver.normalize(username));
            }
            FastClientHUDClient.LOGGER.warn("[Store] Failed to fetch public cosmetics: {}", (Object)ex.getMessage());
            return null;
        });
    }

    void clear() {
        if (!this.cache.isEmpty()) {
            FastClientHUDClient.LOGGER.info("[Store] Clearing {} cached public cosmetic profile(s)", (Object)this.cache.size());
        }
        this.cache.clear();
        this.inFlight.clear();
        this.runtimeAdapter.clearRemoteCosmetics();
    }

    private void apply(String username, List<String> equippedIds) {
        ArrayList<StoreCosmetic> equipped = new ArrayList<StoreCosmetic>();
        for (String id : equippedIds) {
            StoreCosmetic cosmetic = this.catalog.find(id);
            if (cosmetic == null) continue;
            equipped.add(cosmetic);
        }
        if (equipped.isEmpty()) {
            this.runtimeAdapter.clearPlayerCosmetics(username);
        } else {
            this.runtimeAdapter.applyPlayerCosmetics(username, equipped);
        }
    }

    private void clearPlayersNoLongerVisible(Set<String> visible) {
        for (String username : this.cache.keySet()) {
            if (visible.contains(username)) continue;
            this.cache.remove(username);
            this.inFlight.remove(username);
            this.runtimeAdapter.clearPlayerCosmetics(username);
        }
    }

    private static String normalize(String username) {
        return username.toLowerCase(Locale.ROOT);
    }

    @Environment(value=EnvType.CLIENT)
    private record CachedProfile(List<String> equippedItemIds, long expiresAtMillis) {
    }
}

