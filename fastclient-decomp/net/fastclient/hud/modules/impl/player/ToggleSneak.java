/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1041
 *  net.minecraft.class_3675
 */
package net.fastclient.hud.modules.impl.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fastclient.hud.modules.Category;
import net.fastclient.hud.modules.Module;
import net.minecraft.class_1041;
import net.minecraft.class_3675;

@Environment(value=EnvType.CLIENT)
public class ToggleSneak
extends Module {
    private boolean sneakToggled = false;
    private boolean wasKeyPressed = false;

    public ToggleSneak() {
        super("ToggleSneak", "Press sneak once to toggle sneaking on/off - great for building!", Category.PLAYER);
    }

    @Override
    protected void onEnable() {
        this.sneakToggled = false;
        this.wasKeyPressed = false;
    }

    @Override
    public void onTick() {
        if (!this.isInGame() || ToggleSneak.mc.field_1724 == null) {
            return;
        }
        int sneakKey = ToggleSneak.mc.field_1690.field_1832.method_1429().method_1444();
        boolean isKeyPressed = class_3675.method_15987((class_1041)mc.method_22683(), (int)sneakKey);
        if (isKeyPressed && !this.wasKeyPressed) {
            this.sneakToggled = !this.sneakToggled;
        }
        this.wasKeyPressed = isKeyPressed;
        if (this.sneakToggled) {
            ToggleSneak.mc.field_1690.field_1832.method_23481(true);
        }
    }

    @Override
    protected void onDisable() {
        this.sneakToggled = false;
        if (ToggleSneak.mc.field_1690 != null) {
            ToggleSneak.mc.field_1690.field_1832.method_23481(false);
        }
    }

    public boolean isSneakToggled() {
        return this.sneakToggled;
    }
}

