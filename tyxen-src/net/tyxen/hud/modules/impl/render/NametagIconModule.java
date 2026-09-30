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
import net.tyxen.hud.modules.Category;
import net.tyxen.hud.modules.Module;
import net.tyxen.hud.modules.settings.BooleanSetting;

@Environment(value=EnvType.CLIENT)
public class NametagIconModule
extends Module {
    private static NametagIconModule instance;
    private final BooleanSetting showAboveHead = this.register(new BooleanSetting("show_above_head", "Show icon above players' heads", true));
    private final BooleanSetting showInTabList = this.register(new BooleanSetting("show_in_tab_list", "Show icon in the TAB player list", true));

    public NametagIconModule() {
        super("NametagIcon", "Show Tyxen logo next to Tyxen players", Category.RENDER);
        instance = this;
        this.setEnabled(true);
    }

    public static boolean shouldShowAboveHead() {
        return instance != null && instance.isEnabled() && NametagIconModule.instance.showAboveHead.isEnabled();
    }

    public static boolean shouldShowInTabList() {
        return instance != null && instance.isEnabled() && NametagIconModule.instance.showInTabList.isEnabled();
    }
}

