/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.tyxen.hud.modules.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.appearance.PlayerAppearanceService;
import net.tyxen.hud.modules.Category;
import net.tyxen.hud.modules.Module;

@Environment(value=EnvType.CLIENT)
public final class TyxenSkinsModule
extends Module {
    private static TyxenSkinsModule instance;

    public TyxenSkinsModule() {
        super("TyxenSkins", "Tyxen Skins & Capes", "Show Tyxen skins and capes on Tyxen players", Category.RENDER);
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

