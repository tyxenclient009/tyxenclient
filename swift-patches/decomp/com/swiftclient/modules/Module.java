/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package com.swiftclient.modules;

import com.swiftclient.config.ClientConfig;
import com.swiftclient.modules.Category;
import net.minecraft.class_310;
import net.minecraft.class_332;

public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private int key;
    private boolean enabled;

    protected Module(String name, Category category, String description, int key) {
        this(name, category, description, key, false);
    }

    protected Module(String name, Category category, String description, int key, boolean enabledByDefault) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.key = key;
        this.enabled = ClientConfig.enabled(name, enabledByDefault);
    }

    public final String name() {
        return this.name;
    }

    public final Category category() {
        return this.category;
    }

    public final String description() {
        return this.description;
    }

    public final boolean enabled() {
        return this.enabled;
    }

    public final int key() {
        return this.key;
    }

    public int defaultX() {
        return 8;
    }

    public int defaultY() {
        return 8;
    }

    public boolean hasPosition() {
        return this.category() == Category.HUD;
    }

    public boolean hasBackground() {
        return false;
    }

    public int hudWidth(class_310 mc) {
        return 80;
    }

    public int hudHeight(class_310 mc) {
        return 13;
    }

    public final void toggle() {
        this.setEnabled(!this.enabled);
    }

    public final void setEnabled(boolean value) {
        if (this.enabled == value) {
            return;
        }
        this.enabled = value;
        ClientConfig.setEnabled(this.name, value);
        if (value) {
            this.onEnable();
        } else {
            this.onDisable();
        }
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void tick(class_310 client) {
    }

    public void render(class_332 ctx, float tickDelta) {
    }

    public void init() {
    }

    public void shutdown() {
    }
}

