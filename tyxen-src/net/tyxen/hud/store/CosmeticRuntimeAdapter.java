/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.tyxen.hud.store;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.store.StoreCosmetic;

@Environment(value=EnvType.CLIENT)
public interface CosmeticRuntimeAdapter {
    public void applyCosmetics(List<StoreCosmetic> var1);

    public void clearAllCosmetics();

    default public void refreshLocalCosmetics() {
    }

    default public void applyPlayerCosmetics(String username, List<StoreCosmetic> equippedCosmetics) {
    }

    default public void clearPlayerCosmetics(String username) {
    }

    default public void clearRemoteCosmetics() {
    }
}

