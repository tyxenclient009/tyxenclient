/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_5348
 */
package net.tyxen.hud.gui.components;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.gui.TyxenFonts;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_5348;

@Environment(value=EnvType.CLIENT)
public abstract class UIComponent {
    protected static final int CONTENT_INSET = 12;
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected boolean visible = true;
    protected boolean hovered = false;
    protected BooleanSupplier visibilityCheck = () -> true;

    public UIComponent(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void render(class_332 var1, int var2, int var3, float var4);

    public boolean mouseClicked(class_11909 event, boolean bl) {
        return this.isHovered(event.comp_4798(), event.comp_4799());
    }

    public boolean mouseReleased(class_11909 event) {
        return false;
    }

    public boolean mouseDragged(class_11909 event, double deltaX, double deltaY) {
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizAmount, double vertAmount) {
        return false;
    }

    public boolean keyPressed(class_11908 event) {
        return false;
    }

    public boolean charTyped(class_11905 event) {
        return false;
    }

    protected boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= (double)this.x && mouseX <= (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY <= (double)(this.y + this.height);
    }

    protected void drawUiText(class_332 graphics, class_310 mc, String text, int textX, int textY, int color) {
        float scale = TyxenFonts.bodyScale();
        graphics.method_51448().pushMatrix();
        graphics.method_51448().translate((float)textX, (float)textY);
        graphics.method_51448().scale(scale, scale);
        graphics.method_51448().translate((float)(-textX), (float)(-textY));
        graphics.method_51439(mc.field_1772, TyxenFonts.body(text), textX, textY, color, false);
        graphics.method_51448().popMatrix();
    }

    protected int uiTextWidth(class_310 mc, String text) {
        return Math.round((float)mc.field_1772.method_27525((class_5348)TyxenFonts.body(text)) * TyxenFonts.bodyScale());
    }

    protected int centeredTextY(class_310 mc, int boxY, int boxHeight) {
        Objects.requireNonNull(mc.field_1772);
        int lineHeight = Math.round(9.0f * TyxenFonts.bodyScale());
        return boxY + (boxHeight - lineHeight) / 2 + 1;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean isVisible() {
        return this.visible && this.visibilityCheck.getAsBoolean();
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setVisibilityCheck(BooleanSupplier check) {
        this.visibilityCheck = check;
    }
}

