package gg.tyx.client;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

/** Canonical (1.21.9+) keybind factory — categories are typed now. */
public final class Keybinds {
    private Keybinds() {
    }

    public static KeyBinding create(String id, InputUtil.Type type, int code) {
        return new KeyBinding(id, type, code, KeyBinding.Category.MISC);
    }
}
