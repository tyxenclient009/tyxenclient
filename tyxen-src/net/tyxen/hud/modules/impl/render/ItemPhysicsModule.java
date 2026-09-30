package net.tyxen.hud.modules.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.modules.Category;
import net.tyxen.hud.modules.Module;

@Environment(value = EnvType.CLIENT)
public class ItemPhysicsModule extends Module {
    private static ItemPhysicsModule instance;

    public ItemPhysicsModule() {
        super("ItemPhysics", "Dropped items lie flat and drift gently", Category.RENDER);
        instance = this;
        this.setEnabled(true);
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }
}
