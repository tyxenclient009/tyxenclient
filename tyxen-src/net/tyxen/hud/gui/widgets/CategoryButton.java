/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_11909
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package net.tyxen.hud.gui.widgets;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.gui.TyxenUI;
import net.tyxen.hud.modules.Category;
import net.tyxen.hud.render.AnimationUtils;
import net.minecraft.class_11909;
import net.minecraft.class_310;
import net.minecraft.class_332;

@Environment(value=EnvType.CLIENT)
public class CategoryButton {
    private final Category category;
    private int x;
    private int y;
    private final int width;
    private final int height;
    private boolean selected;
    private final Consumer<Category> onClick;
    private float hoverProgress;
    private float selectProgress;
    private long lastUpdate = System.currentTimeMillis();

    public CategoryButton(Category category, int x, int y, int width, int height, Consumer<Category> onClick) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.onClick = onClick;
    }

    public void render(class_332 graphics, int mouseX, int mouseY) {
        class_310 mc = class_310.method_1551();
        boolean hovered = mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
        long now = System.currentTimeMillis();
        float delta = (float)(now - this.lastUpdate) / 1000.0f;
        this.lastUpdate = now;
        this.hoverProgress = AnimationUtils.smoothDelta(this.hoverProgress, hovered ? 1.0f : 0.0f, 0.3f, delta * 60.0f);
        this.selectProgress = AnimationUtils.smoothDelta(this.selectProgress, this.selected ? 1.0f : 0.0f, 0.25f, delta * 60.0f);
        int bgColor = this.selected ? -14498466 : TyxenUI.blend(-435153640, -266722777, this.hoverProgress);
        graphics.method_25294(this.x, this.y, this.x + this.width, this.y + this.height, bgColor);
        String icon = this.category.getIcon();
        int iconX = this.x + 8;
        int iconY = this.y + (this.height - 8) / 2;
        int iconColor = this.selected ? -723724 : -7303024;
        graphics.method_51433(mc.field_1772, icon, iconX, iconY, iconColor, false);
        String name = this.category.getDisplayName();
        int textX = this.x + 24;
        int textY = this.y + (this.height - 8) / 2;
        int textColor = this.selected ? -723724 : TyxenUI.blend(-7303024, -723724, this.hoverProgress);
        graphics.method_51433(mc.field_1772, name, textX, textY, textColor, this.selected);
    }

    public boolean mouseClicked(class_11909 event, boolean bl) {
        double mouseX = event.comp_4798();
        double mouseY = event.comp_4799();
        if (event.method_74245() == 0 && mouseX >= (double)this.x && mouseX <= (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY <= (double)(this.y + this.height)) {
            if (this.onClick != null) {
                this.onClick.accept(this.category);
            }
            return true;
        }
        return false;
    }

    public Category getCategory() {
        return this.category;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }
}

