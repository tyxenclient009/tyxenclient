/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1041
 *  net.minecraft.class_3675
 */
package net.tyxen.hud.modules.impl.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.modules.Category;
import net.tyxen.hud.modules.Module;

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
        boolean isKeyPressed = ToggleSneak.mc.field_1690.field_1832.method_1434();
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

