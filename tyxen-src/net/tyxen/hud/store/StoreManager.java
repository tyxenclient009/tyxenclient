/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.class_310
 */
package net.tyxen.hud.store;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.tyxen.hud.TyxenHUDClient;
import net.tyxen.hud.store.CosmeticCategory;
import net.tyxen.hud.store.CosmeticRuntimeAdapter;
import net.tyxen.hud.store.MarketplaceClient;
import net.tyxen.hud.store.RemoteCosmeticResolver;
import net.tyxen.hud.store.StoreCatalog;
import net.tyxen.hud.store.StoreCosmetic;
import net.tyxen.hud.store.StoreState;
import net.minecraft.class_310;

@Environment(value=EnvType.CLIENT)
public class StoreManager {
    public static final int MAX_CHAT_TAGS = 3;
    private static final int DEFAULT_DAILY_COIN_CLAIM = 100;
    private static final long REMOTE_COSMETIC_REFRESH_INTERVAL_MS = 5000L;
    private static final int LOCAL_COSMETIC_REFRESH_TICKS = 80;
    private static final String CHAT_TAG_SLOT = "nametag";
    private static StoreManager instance;
    private final StoreCatalog catalog;
    private final StoreState state;
    private final CosmeticRuntimeAdapter runtimeAdapter;
    private final MarketplaceClient marketplaceClient;
    private final RemoteCosmeticResolver remoteCosmeticResolver;
    private final Path configPath;
    private final Gson gson;
    private volatile boolean catalogLoaded;
    private volatile boolean bootstrapAttempted;
    private volatile int dailyCoinClaimAmount = 100;
    private long nextRemoteCosmeticRefreshMillis;
    private int localCosmeticRefreshTicks;

    public StoreManager() {
        instance = this;
        this.catalog = new StoreCatalog();
        this.state = new StoreState();
        this.runtimeAdapter = new CosmeticRuntimeAdapter(){

            @Override
            public void applyCosmetics(List<StoreCosmetic> equippedCosmetics) {
            }

            @Override
            public void clearAllCosmetics() {
            }
        };
        this.marketplaceClient = MarketplaceClient.getInstance();
        this.remoteCosmeticResolver = new RemoteCosmeticResolver(this.marketplaceClient, this.catalog, this.runtimeAdapter);
        this.configPath = FabricLoader.getInstance().getConfigDir().resolve("tyxen-store.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.populateFallbackCatalog();
        this.loadState();
        this.applyRuntime();
        TyxenHUDClient.LOGGER.info("[StoreManager] Initialized store cache: catalog={} owned={} equipped={} api={}", new Object[]{this.catalog.all().size(), this.state.getOwnedItemIds().size(), this.state.getEquippedItemIds().size(), this.marketplaceClient.getBaseUrl()});
    }

    public static StoreManager getInstance() {
        if (instance == null) {
            instance = new StoreManager();
        }
        return instance;
    }

    public CosmeticRuntimeAdapter getRuntimeAdapter() {
        return this.runtimeAdapter;
    }

    public MarketplaceClient getMarketplaceClient() {
        return this.marketplaceClient;
    }

    public java.util.List<String> equippedFor(String username) {
        try {
            return this.remoteCosmeticResolver.equippedFor(username);
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    public List<StoreCosmetic> getCatalog() {
        return this.catalog.all();
    }

    public boolean isCatalogLoaded() {
        return this.catalogLoaded;
    }

    public StoreCosmetic getCosmetic(String id) {
        return this.catalog.find(id);
    }

    public List<StoreCosmetic> getOwnedCosmetics() {
        return this.catalog.owned(this.state);
    }

    public List<StoreCosmetic> getEquippedCosmetics() {
        return this.catalog.equipped(this.state);
    }

    public List<StoreCosmetic> getEquippedChatTags() {
        ArrayList<StoreCosmetic> tags = new ArrayList<StoreCosmetic>();
        for (String id : this.state.getEquippedItemIds()) {
            StoreCosmetic cosmetic = this.getCosmetic(id);
            if (!StoreManager.isChatTag(cosmetic)) continue;
            tags.add(cosmetic);
            if (tags.size() < 3) continue;
            break;
        }
        return List.copyOf(tags);
    }

    public List<StoreCosmetic> getCosmeticsByCategory(CosmeticCategory category) {
        return this.catalog.byCategory(category);
    }

    public List<StoreCosmetic> searchCosmetics(String query, CosmeticCategory category, boolean ownedOnly) {
        return this.catalog.search(query, category, ownedOnly, this.state);
    }

    public StoreState getState() {
        return this.state;
    }

    public int getDailyCoinClaimAmount() {
        return this.dailyCoinClaimAmount;
    }

    public java.util.concurrent.CompletableFuture<int[]> onlineCount() {
        return this.marketplaceClient.onlineCount();
    }

    public void bootstrapFromBackend() {
        if (this.bootstrapAttempted) {
            return;
        }
        this.bootstrapAttempted = true;
        String username = this.getClientUsername();
        if (username == null) {
            TyxenHUDClient.LOGGER.warn("[StoreManager] No username, skipping bootstrap");
            return;
        }
        TyxenHUDClient.LOGGER.info("[StoreManager] Bootstrapping marketplace state for {}", (Object)username);
        ((CompletableFuture)this.marketplaceClient.bootstrap(username).thenAccept(result -> this.runOnClientThread(() -> {
            try {
                if (result.catalog() != null && !result.catalog().isEmpty()) {
                    this.catalog.replaceAll(result.catalog());
                    this.ensureLocalCategoryFillers();
                    TyxenHUDClient.LOGGER.info("[StoreManager] Catalog replaced from backend: {} items", (Object)result.catalog().size());
                }
                this.catalogLoaded = true;
                this.dailyCoinClaimAmount = result.dailyCoinClaimAmount();
                MarketplaceClient.UserState userState = result.userState();
                this.state.setCoinBalance(userState.coinBalance());
                this.state.setLastCoinClaimEpochDay(userState.lastCoinClaimEpochDay());
                this.syncOwnedAndEquipped(userState.ownedItemIds(), userState.equippedItemIds());
                this.saveState();
                this.applyRuntime();
                TyxenHUDClient.LOGGER.info("[StoreManager] Bootstrap complete \u2014 {} items, {} coins", (Object)result.catalog().size(), (Object)userState.coinBalance());
            }
            catch (Exception e) {
                TyxenHUDClient.LOGGER.warn("[StoreManager] Bootstrap apply failed: {}", (Object)e.getMessage());
            }
        }))).exceptionally(ex -> {
            this.runOnClientThread(() -> {
                TyxenHUDClient.LOGGER.warn("[StoreManager] Bootstrap failed, using local state: {}", (Object)((Throwable)ex).getMessage());
                this.catalogLoaded = true;
            });
            return null;
        });
    }

    public boolean purchase(String id) {
        StoreCosmetic cosmetic = this.getCosmetic(id);
        if (cosmetic == null) {
            TyxenHUDClient.LOGGER.warn("[StoreManager] Purchase ignored for unknown item {}", (Object)id);
            return false;
        }
        if (this.state.owns(id)) {
            TyxenHUDClient.LOGGER.info("[StoreManager] Purchase skipped for already-owned item {}", (Object)id);
            return false;
        }
        String username = this.getClientUsername();
        if (username == null) {
            TyxenHUDClient.LOGGER.info("[StoreManager] Offline purchase fallback for item {}", (Object)id);
            return this.purchaseLocal(id, cosmetic);
        }
        if (!this.state.spendCoins(cosmetic.getPrice())) {
            TyxenHUDClient.LOGGER.info("[StoreManager] Purchase rejected locally for {}: price={} balance={}", new Object[]{id, cosmetic.getPrice(), this.state.getCoinBalance()});
            return false;
        }
        this.state.addOwned(id);
        this.saveState();
        TyxenHUDClient.LOGGER.info("[StoreManager] Purchase queued: user={} item={} price={}", new Object[]{username, id, cosmetic.getPrice()});
        ((CompletableFuture)this.marketplaceClient.purchase(username, id).thenAccept(result -> this.runOnClientThread(() -> {
            if (result.userState() != null) {
                this.syncUserState(result.userState());
                this.saveState();
                this.applyRuntime();
                TyxenHUDClient.LOGGER.info("[StoreManager] Purchase reconciled: item={} balance={} owned={}", new Object[]{id, this.state.getCoinBalance(), this.state.getOwnedItemIds().size()});
            }
        }))).exceptionally(ex -> {
            this.runOnClientThread(() -> TyxenHUDClient.LOGGER.warn("[StoreManager] Backend purchase failed: {}", (Object)((Throwable)ex).getMessage()));
            return null;
        });
        return true;
    }

    public void equip(String id) {
        StoreCosmetic cosmetic = this.getCosmetic(id);
        if (cosmetic == null || !this.state.owns(id)) {
            TyxenHUDClient.LOGGER.info("[StoreManager] Equip ignored for {}: known={} owned={}", new Object[]{id, cosmetic != null, this.state.owns(id)});
            return;
        }
        List<StoreCosmetic> equipped = this.getEquippedCosmetics();
        for (StoreCosmetic equippedCosmetic : equipped) {
            if (equippedCosmetic.getId().equals(id)) {
                TyxenHUDClient.LOGGER.info("[StoreManager] Equip skipped for already-equipped item {}", (Object)id);
                this.applyRuntime();
                return;
            }
            if (!cosmetic.conflictsWith(equippedCosmetic)) continue;
            this.state.unequip(equippedCosmetic.getId());
        }
        this.trimChatTagsForNextEquip(cosmetic);
        this.state.equip(id);
        this.saveState();
        this.applyRuntime();
        String username = this.getClientUsername();
        TyxenHUDClient.LOGGER.info("[StoreManager] Equipped locally: user={} item={} slot={} equipped={}", new Object[]{username == null ? "offline" : username, id, cosmetic.getSlot(), this.state.getEquippedItemIds().size()});
        if (username != null) {
            ((CompletableFuture)this.marketplaceClient.equip(username, id).thenAccept(result -> this.runOnClientThread(() -> {
                if (result.userState() != null) {
                    this.syncEquipped(result.userState().equippedItemIds());
                    this.saveState();
                    this.applyRuntime();
                    TyxenHUDClient.LOGGER.info("[StoreManager] Equip reconciled: item={} equipped={}", (Object)id, (Object)this.state.getEquippedItemIds().size());
                }
            }))).exceptionally(ex -> {
                this.runOnClientThread(() -> TyxenHUDClient.LOGGER.warn("[StoreManager] Backend equip failed: {}", (Object)((Throwable)ex).getMessage()));
                return null;
            });
        }
    }

    public void unequip(String id) {
        this.state.unequip(id);
        this.saveState();
        this.applyRuntime();
        String username = this.getClientUsername();
        TyxenHUDClient.LOGGER.info("[StoreManager] Unequipped locally: user={} item={}", (Object)(username == null ? "offline" : username), (Object)id);
        if (username != null) {
            ((CompletableFuture)this.marketplaceClient.unequip(username, id).thenAccept(result -> this.runOnClientThread(() -> {
                if (result.userState() != null) {
                    this.syncEquipped(result.userState().equippedItemIds());
                    this.saveState();
                    this.applyRuntime();
                    TyxenHUDClient.LOGGER.info("[StoreManager] Unequip reconciled: item={} equipped={}", (Object)id, (Object)this.state.getEquippedItemIds().size());
                }
            }))).exceptionally(ex -> {
                this.runOnClientThread(() -> TyxenHUDClient.LOGGER.warn("[StoreManager] Backend unequip failed: {}", (Object)((Throwable)ex).getMessage()));
                return null;
            });
        }
    }

    public void removeAll() {
        if (this.state.getEquippedItemIds().isEmpty()) {
            return;
        }
        int removed = this.state.getEquippedItemIds().size();
        this.state.removeAllEquipped();
        this.saveState();
        this.applyRuntime();
        String username = this.getClientUsername();
        TyxenHUDClient.LOGGER.info("[StoreManager] Removed all equipped locally: user={} removed={}", (Object)(username == null ? "offline" : username), (Object)removed);
        if (username != null) {
            ((CompletableFuture)this.marketplaceClient.removeAll(username).thenAccept(result -> this.runOnClientThread(() -> {
                if (result.userState() != null) {
                    this.syncEquipped(result.userState().equippedItemIds());
                    this.saveState();
                    this.applyRuntime();
                    TyxenHUDClient.LOGGER.info("[StoreManager] Remove-all reconciled: equipped={}", (Object)this.state.getEquippedItemIds().size());
                }
            }))).exceptionally(ex -> {
                this.runOnClientThread(() -> TyxenHUDClient.LOGGER.warn("[StoreManager] Backend removeAll failed: {}", (Object)((Throwable)ex).getMessage()));
                return null;
            });
        }
    }

    public boolean claimDailyCoins() {
        if (!this.state.canClaimDailyCoins()) {
            return false;
        }
        this.state.addCoins(this.dailyCoinClaimAmount);
        this.state.markDailyCoinsClaimed();
        this.saveState();
        String username = this.getClientUsername();
        TyxenHUDClient.LOGGER.info("[StoreManager] Daily claim queued: user={} amount={}", (Object)(username == null ? "offline" : username), (Object)this.dailyCoinClaimAmount);
        if (username != null) {
            ((CompletableFuture)this.marketplaceClient.claimDaily(username).thenAccept(result -> this.runOnClientThread(() -> {
                if (result.userState() != null) {
                    this.state.setCoinBalance(result.userState().coinBalance());
                    this.state.setLastCoinClaimEpochDay(result.userState().lastCoinClaimEpochDay());
                    this.saveState();
                    TyxenHUDClient.LOGGER.info("[StoreManager] Daily claim reconciled: balance={}", (Object)this.state.getCoinBalance());
                }
            }))).exceptionally(ex -> {
                this.runOnClientThread(() -> TyxenHUDClient.LOGGER.warn("[StoreManager] Backend claim failed: {}", (Object)((Throwable)ex).getMessage()));
                return null;
            });
        }
        return true;
    }

    public void applyRuntime() {
        List<StoreCosmetic> equipped = this.getEquippedCosmetics();
        if (equipped.isEmpty()) {
            this.runtimeAdapter.clearAllCosmetics();
        } else {
            this.runtimeAdapter.applyCosmetics(equipped);
        }
        this.scheduleLocalCosmeticRefresh();
    }

    public void saveState() {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("coinBalance", (Number)this.state.getCoinBalance());
            root.addProperty("lastCategory", this.state.getLastSelectedCategory());
            root.addProperty("scrollOffset", (Number)this.state.getScrollOffset());
            root.addProperty("lastCoinClaimEpochDay", (Number)this.state.getLastCoinClaimEpochDay());
            JsonArray owned = new JsonArray();
            for (String id : this.state.getOwnedItemIds()) {
                owned.add(id);
            }
            root.add("owned", (JsonElement)owned);
            JsonArray equipped = new JsonArray();
            for (String id : this.state.getEquippedItemIds()) {
                equipped.add(id);
            }
            root.add("equipped", (JsonElement)equipped);
            Files.createDirectories(this.configPath.getParent(), new FileAttribute[0]);
            Files.writeString(this.configPath, (CharSequence)this.gson.toJson((JsonElement)root), new OpenOption[0]);
        }
        catch (Exception e) {
            TyxenHUDClient.LOGGER.error("[StoreManager] Failed to save state: {}", (Object)e.getMessage());
        }
    }

    private void loadState() {
        if (!Files.exists(this.configPath, new LinkOption[0])) {
            return;
        }
        try {
            String id;
            int i;
            String json = Files.readString(this.configPath);
            JsonObject root = (JsonObject)this.gson.fromJson(json, JsonObject.class);
            if (root.has("coinBalance")) {
                this.state.setCoinBalance(root.get("coinBalance").getAsInt());
            }
            if (root.has("lastCategory")) {
                this.state.setLastSelectedCategory(root.get("lastCategory").getAsString());
            }
            if (root.has("scrollOffset")) {
                this.state.setScrollOffset(root.get("scrollOffset").getAsDouble());
            }
            if (root.has("lastCoinClaimEpochDay")) {
                this.state.setLastCoinClaimEpochDay(root.get("lastCoinClaimEpochDay").getAsLong());
            }
            if (root.has("owned")) {
                JsonArray owned = root.getAsJsonArray("owned");
                for (i = 0; i < owned.size(); ++i) {
                    id = owned.get(i).getAsString();
                    if (this.getCosmetic(id) == null) continue;
                    this.state.addOwned(id);
                }
            }
            if (root.has("equipped")) {
                JsonArray equipped = root.getAsJsonArray("equipped");
                for (i = 0; i < equipped.size(); ++i) {
                    id = equipped.get(i).getAsString();
                    if (this.getCosmetic(id) == null || !this.state.owns(id)) continue;
                    this.equipLoaded(id);
                }
            }
            TyxenHUDClient.LOGGER.info("[StoreManager] Store state loaded");
        }
        catch (Exception e) {
            TyxenHUDClient.LOGGER.error("[StoreManager] Failed to load state: {}", (Object)e.getMessage());
        }
    }

    private void equipLoaded(String id) {
        StoreCosmetic cosmetic = this.getCosmetic(id);
        if (cosmetic == null) {
            return;
        }
        List<StoreCosmetic> equipped = this.getEquippedCosmetics();
        for (StoreCosmetic equippedCosmetic : equipped) {
            if (!cosmetic.conflictsWith(equippedCosmetic)) continue;
            this.state.unequip(equippedCosmetic.getId());
        }
        this.trimChatTagsForNextEquip(cosmetic);
        this.state.equip(id);
    }

    private void syncUserState(MarketplaceClient.UserState userState) {
        this.state.setCoinBalance(userState.coinBalance());
        this.state.setLastCoinClaimEpochDay(userState.lastCoinClaimEpochDay());
        this.syncOwnedAndEquipped(userState.ownedItemIds(), userState.equippedItemIds());
    }

    private void syncOwnedAndEquipped(List<String> ownedIds, List<String> equippedIds) {
        ArrayList<String> owned = new ArrayList<String>();
        for (String id : ownedIds) {
            if (this.getCosmetic(id) == null) continue;
            owned.add(id);
        }
        this.state.replaceOwned(owned);
        ArrayList<String> equipped = new ArrayList<String>();
        for (String id : equippedIds) {
            if (this.getCosmetic(id) == null || !this.state.owns(id)) continue;
            equipped.add(id);
        }
        this.syncEquipped(equipped);
    }

    private void syncEquipped(List<String> equippedIds) {
        this.state.replaceEquipped(List.of());
        for (String id : equippedIds) {
            if (this.getCosmetic(id) == null || !this.state.owns(id)) continue;
            this.equipLoaded(id);
        }
    }

    private void trimChatTagsForNextEquip(StoreCosmetic nextCosmetic) {
        StoreCosmetic equipped;
        if (!StoreManager.isChatTag(nextCosmetic)) {
            return;
        }
        int activeTags = 0;
        ArrayList<String> equippedIds = new ArrayList<String>(this.state.getEquippedItemIds());
        for (String equippedId : equippedIds) {
            equipped = this.getCosmetic(equippedId);
            if (!StoreManager.isChatTag(equipped) || equippedId.equals(nextCosmetic.getId())) continue;
            ++activeTags;
        }
        for (String equippedId : equippedIds) {
            if (activeTags < 3) break;
            equipped = this.getCosmetic(equippedId);
            if (!StoreManager.isChatTag(equipped) || equippedId.equals(nextCosmetic.getId())) continue;
            this.state.unequip(equippedId);
            --activeTags;
        }
    }

    private static boolean isChatTag(StoreCosmetic cosmetic) {
        return cosmetic != null && CHAT_TAG_SLOT.equals(cosmetic.getSlot());
    }

    private boolean purchaseLocal(String id, StoreCosmetic cosmetic) {
        if (!this.state.spendCoins(cosmetic.getPrice())) {
            return false;
        }
        this.state.addOwned(id);
        this.saveState();
        return true;
    }

    private String getClientUsername() {
        class_310 client = class_310.method_1551();
        if (client.method_1548() != null && client.method_1548().method_1676() != null) {
            return client.method_1548().method_1676();
        }
        return null;
    }

    public void onJoin() {
        this.bootstrapAttempted = false;
        this.catalogLoaded = false;
        this.nextRemoteCosmeticRefreshMillis = 0L;
        this.scheduleLocalCosmeticRefresh();
        TyxenHUDClient.LOGGER.info("[StoreManager] Join detected, queued store bootstrap/runtime refresh");
    }

    public void onClientTick(class_310 client) {
        if (client.field_1724 == null) {
            return;
        }
        if (!this.bootstrapAttempted) {
            TyxenHUDClient.LOGGER.info("[StoreManager] Player ready, starting marketplace bootstrap");
            this.bootstrapFromBackend();
            this.applyRuntime();
        }
        this.refreshLocalCosmeticsIfQueued();
        this.refreshVisiblePlayerCosmetics(client);
    }

    private void refreshVisiblePlayerCosmetics(class_310 client) {
        if (!this.catalogLoaded) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now < this.nextRemoteCosmeticRefreshMillis) {
            return;
        }
        this.nextRemoteCosmeticRefreshMillis = now + 5000L;
        this.remoteCosmeticResolver.refreshVisiblePlayers(client);
    }

    public void onDisconnect() {
        this.bootstrapAttempted = false;
        this.catalogLoaded = false;
        this.nextRemoteCosmeticRefreshMillis = 0L;
        this.remoteCosmeticResolver.clear();
        TyxenHUDClient.LOGGER.info("[StoreManager] Disconnected, cleared remote cosmetics and kept local state");
    }

    private void runOnClientThread(Runnable task) {
        class_310.method_1551().method_63588(task);
    }

    private void scheduleLocalCosmeticRefresh() {
        this.localCosmeticRefreshTicks = Math.max(this.localCosmeticRefreshTicks, 80);
    }

    private void refreshLocalCosmeticsIfQueued() {
        if (this.localCosmeticRefreshTicks <= 0) {
            return;
        }
        --this.localCosmeticRefreshTicks;
        this.runtimeAdapter.refreshLocalCosmetics();
    }

    private void addCosmetic(String id, String displayName, CosmeticCategory category, StoreCosmetic.Rarity rarity, int price, String description, String assetName, String payload, boolean supportsMultipleEquip) {
        this.catalog.add(new StoreCosmetic(id, displayName, category, rarity, price, description, assetName, payload, supportsMultipleEquip));
    }

    private void addCosmeticIfMissing(String id, String displayName, CosmeticCategory category, StoreCosmetic.Rarity rarity, int price, String description, String assetName, String payload, boolean supportsMultipleEquip) {
        if (this.catalog.find(id) == null) {
            this.addCosmetic(id, displayName, category, rarity, price, description, assetName, payload, supportsMultipleEquip);
        }
    }

    private void ensureLocalCategoryFillers() {
    }

    private void populateFallbackCatalog() {
    }

    public void refreshFromBackend() {
        this.bootstrapAttempted = false;
        this.bootstrapFromBackend();
    }
}

