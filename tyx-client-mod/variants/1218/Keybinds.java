package gg.tyx.client;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

/** VARIANT 1218 (MC 1.21.1–1.21.8) — categories are still plain strings. */
public final class Keybinds {
    private Keybinds() {
    }

    public static KeyBinding create(String id, InputUtil.Type type, int code) {
        return new KeyBinding(id, type, code, "category.tyxclient");
    }
}
