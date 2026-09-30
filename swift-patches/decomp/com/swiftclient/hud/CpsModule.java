/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package com.swiftclient.hud;

import com.swiftclient.config.ClientConfig;
import com.swiftclient.hud.HudUtil;
import com.swiftclient.modules.Category;
import com.swiftclient.modules.Module;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class CpsModule
extends Module {
    private final Deque<Long> left = new ArrayDeque<Long>();
    private final Deque<Long> right = new ArrayDeque<Long>();
    private boolean wasL;
    private boolean wasR;

    public CpsModule() {
        super("CPS", Category.HUD, "Clicks per second counter.", 0);
    }

    @Override
    public int defaultY() {
        return 32;
    }

    @Override
    public void tick(class_310 c) {
        if (c.field_1724 == null) {
            return;
        }
        boolean l = c.field_1690.field_1886.method_1434();
        boolean r = c.field_1690.field_1904.method_1434();
        long now = System.currentTimeMillis();
        if (l && !this.wasL) {
            this.left.addLast(now);
        }
        if (r && !this.wasR) {
            this.right.addLast(now);
        }
        this.wasL = l;
        this.wasR = r;
        while (!this.left.isEmpty() && now - this.left.peekFirst() > 1000L) {
            this.left.pollFirst();
        }
        while (!this.right.isEmpty() && now - this.right.peekFirst() > 1000L) {
            this.right.pollFirst();
        }
    }

    @Override
    public void render(class_332 c, float d) {
        boolean showR = ClientConfig.getBool(this.name(), "right", true);
        String s = showR ? this.left.size() + " | " + this.right.size() + " CPS" : this.left.size() + " CPS";
        HudUtil.label(c, class_310.method_1551().field_1772, s, ClientConfig.getX(this.name(), this.defaultX()), ClientConfig.getY(this.name(), this.defaultY()), this.name());
    }

    @Override
    public boolean hasBackground() {
        return true;
    }
}

