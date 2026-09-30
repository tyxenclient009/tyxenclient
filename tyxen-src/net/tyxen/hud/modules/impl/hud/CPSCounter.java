/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_332
 *  org.lwjgl.glfw.GLFW
 */
package net.tyxen.hud.modules.impl.hud;

import java.util.LinkedList;
import java.util.Queue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.gui.TyxenUI;
import net.tyxen.hud.modules.Category;
import net.tyxen.hud.modules.Module;
import net.minecraft.class_332;
import org.lwjgl.glfw.GLFW;

@Environment(value=EnvType.CLIENT)
public class CPSCounter
extends Module {
    private final Queue<Long> leftClicks = new LinkedList<Long>();
    private final Queue<Long> rightClicks = new LinkedList<Long>();
    private boolean wasLeftPressed = false;
    private boolean wasRightPressed = false;

    public CPSCounter() {
        super("CPSCounter", "Clicks per second counter", Category.HUD);
    }

    @Override
    public void onTick() {
        boolean rightPressed;
        if (!this.isInGame()) {
            return;
        }
        long now = System.currentTimeMillis();
        while (!this.leftClicks.isEmpty() && now - this.leftClicks.peek() > 1000L) {
            this.leftClicks.poll();
        }
        while (!this.rightClicks.isEmpty() && now - this.rightClicks.peek() > 1000L) {
            this.rightClicks.poll();
        }
        long windowHandle = mc.method_22683().method_4490();
        boolean leftPressed = GLFW.glfwGetMouseButton((long)windowHandle, (int)0) == 1;
        boolean bl = rightPressed = GLFW.glfwGetMouseButton((long)windowHandle, (int)1) == 1;
        if (leftPressed && !this.wasLeftPressed) {
            this.leftClicks.add(now);
        }
        if (rightPressed && !this.wasRightPressed) {
            this.rightClicks.add(now);
        }
        this.wasLeftPressed = leftPressed;
        this.wasRightPressed = rightPressed;
    }

    @Override
    public void onRender(class_332 graphics, float tickDelta) {
        if (!this.isInGame()) {
            return;
        }
        int x = this.getHudX();
        int y = this.getHudY();
        float scale = this.getHudScale();
        graphics.method_51448().pushMatrix();
        graphics.method_51448().translate((float)x, (float)y);
        graphics.method_51448().scale(scale, scale);
        graphics.method_51448().translate((float)(-x), (float)(-y));
        String text = String.format("CPS: %d | %d", this.leftClicks.size(), this.rightClicks.size());
        TyxenUI.hudText(graphics, CPSCounter.mc.field_1772, text, x, y, -1, true);
        graphics.method_51448().popMatrix();
    }

    @Override
    public int getHudWidth() {
        String text = String.format("CPS: %d | %d", this.leftClicks.size(), this.rightClicks.size());
        return CPSCounter.mc.field_1772.method_1727(text) + 10;
    }

    @Override
    public int getHudHeight() {
        return 17;
    }
}

