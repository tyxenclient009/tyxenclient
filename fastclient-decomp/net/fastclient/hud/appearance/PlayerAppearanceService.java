/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.class_10538
 *  net.minecraft.class_12079$class_12081
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_7920
 *  net.minecraft.class_8685
 */
package net.fastclient.hud.appearance;

import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fastclient.hud.appearance.PlayerAppearanceCache;
import net.fastclient.hud.network.FastClientUserCache;
import net.minecraft.class_10538;
import net.minecraft.class_12079;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_7920;
import net.minecraft.class_8685;

@Environment(value=EnvType.CLIENT)
public final class PlayerAppearanceService {
    private static final PlayerAppearanceService INSTANCE = new PlayerAppearanceService();
    private final PlayerAppearanceCache<class_12079.class_12081> cache;

    public static PlayerAppearanceService getInstance() {
        return INSTANCE;
    }

    private PlayerAppearanceService() {
        Path cacheDirectory = FabricLoader.getInstance().getGameDir().resolve("fastclient-appearance-cache");
        this.cache = new PlayerAppearanceCache(cacheDirectory, username -> FastClientUserCache.getInstance().isFastClientUser(username), this::registerTexture);
    }

    public class_8685 resolve(class_8685 mojang, String exactUsername) {
        class_12079.class_12081 elytra;
        PlayerAppearanceCache.Appearance<class_12079.class_12081> appearance = this.cache.resolve(exactUsername);
        if (appearance.skin() == null && appearance.cape() == null) {
            return mojang;
        }
        class_12079.class_12081 skin = appearance.skin() != null ? appearance.skin() : mojang.comp_1626();
        class_12079.class_12081 cape = appearance.cape() != null ? appearance.cape() : mojang.comp_1627();
        class_12079.class_12081 class_120812 = elytra = appearance.cape() != null ? appearance.cape() : mojang.comp_1628();
        class_7920 model = appearance.skin() == null ? mojang.comp_1629() : (appearance.slim() ? class_7920.field_41122 : class_7920.field_41123);
        return new class_8685(skin, cape, elytra, model, mojang.comp_1630());
    }

    public void setEnabled(boolean enabled) {
        this.cache.setEnabled(enabled);
    }

    private CompletableFuture<class_12079.class_12081> registerTexture(PlayerAppearanceCache.TextureKind kind, String cacheKey, Path file, String sourceUrl) {
        class_310 minecraft = class_310.method_1551();
        class_2960 id = class_2960.method_60655((String)"fastclient-hud", (String)("appearance/" + kind.name().toLowerCase(Locale.ROOT) + "/" + cacheKey));
        class_10538 downloader = new class_10538(minecraft.method_1487(), minecraft.method_1531(), (Executor)minecraft);
        return downloader.method_65861(id, file, sourceUrl, kind == PlayerAppearanceCache.TextureKind.SKIN);
    }
}

