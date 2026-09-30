/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.fastclient.hud.modules.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fastclient.hud.appearance.PlayerAppearanceService;
import net.fastclient.hud.modules.Category;
import net.fastclient.hud.modules.Module;

@Environment(value=EnvType.CLIENT)
public final class FastClientSkinsModule
extends Module {
    private static FastClientSkinsModule instance;

    public FastClientSkinsModule() {
        super("FastClientSkins", "FastClient Skins & Capes", "Show FastClient skins and capes on FastClient players", Category.RENDER);
        instance = this;
        this.setEnabled(true);
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }

    @Override
    protected void onEnable() {
        PlayerAppearanceService.getInstance().setEnabled(true);
    }

    @Override
    protected void onDisable() {
        PlayerAppearanceService.getInstance().setEnabled(false);
    }
}

