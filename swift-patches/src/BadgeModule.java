package com.swiftclient.render;

import com.swiftclient.modules.Category;
import com.swiftclient.modules.Module;

/**
 * TYX addition — emblem badge left of every other player's nametag,
 * like the classic Tyx client. Toggled from the MODS menu, on by default.
 */
public final class BadgeModule extends Module {
    public BadgeModule() {
        super("Nametag Badge", Category.RENDER, "Tyx emblem beside player nametags.", 0, true);
    }
}
