package com.swiftclient.player;

import com.swiftclient.modules.Category;
import com.swiftclient.modules.Module;

/**
 * TYX addition — show your own nametag above your head in F5 third-person,
 * with the T badge beside it (see NametagBadgeMixin). Vanilla always hides
 * the camera entity's own label; this flips that gate for the local player
 * only. First-person stays clean.
 */
public final class SelfNametagModule extends Module {
    public SelfNametagModule() {
        super("Self Nametag", Category.PLAYER, "Show your own nametag in F5.", 0, true);
    }
}
