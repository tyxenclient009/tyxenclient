package net.tyxen.hud.modules.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.modules.Category;
import net.tyxen.hud.modules.Module;

@Environment(value = EnvType.CLIENT)
public class WingsModule extends Module {
    private static WingsModule instance;

    public WingsModule() {
        super("Wings", "Show equipped wings on your back", Category.RENDER);
        instance = this;
        this.setEnabled(true);
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }
}
