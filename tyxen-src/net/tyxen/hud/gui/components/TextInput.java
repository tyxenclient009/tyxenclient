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
 */
package net.tyxen.hud.gui.components;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.gui.TyxenUI;
import net.tyxen.hud.gui.components.UIComponent;
import net.tyxen.hud.render.Theme;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_310;
import net.minecraft.class_332;

@Environment(value=EnvType.CLIENT)
public class TextInput
extends UIComponent {
    private static final int MAX_LENGTH = 160;
    private final String label;
    private final Consumer<String> onChange;
    private String value;
    private boolean focused;

    public TextInput(int x, int y, int width, int height, String label, String value, Consumer<String> onChange) {
        super(x, y, width, height);
        this.label = label;
        this.value = value == null ? "" : value;
        this.onChange = onChange;
    }

    @Override
    public void render(class_332 graphics, int mouseX, int mouseY, float delta) {
        this.hovered = this.isHovered(mouseX, mouseY);
        class_310 mc = class_310.method_1551();
        String displayLabel = Theme.formatSettingName(this.label);
        this.drawUiText(graphics, mc, displayLabel, this.x + 12, this.centeredTextY(mc, this.y, this.height), this.hovered || this.focused ? -723724 : -7303024);
        int inputWidth = Math.max(150, Math.min(260, this.width / 2));
        int inputX = this.x + this.width - inputWidth - 12;
        int inputY = this.y + 2;
        int inputHeight = this.height - 4;
        TyxenUI.roundedRect(graphics, inputX, inputY, inputWidth, inputHeight, 4, this.focused ? -266722777 : -435153640);
        TyxenUI.outline(graphics, inputX, inputY, inputWidth, inputHeight, this.focused ? -14498466 : 1143616571);
        Object visibleValue = this.fitFromEnd(mc, this.value, inputWidth - 18);
        if (this.focused && System.currentTimeMillis() / 500L % 2L == 0L) {
            visibleValue = (String)visibleValue + "|";
        }
        this.drawUiText(graphics, mc, (String)visibleValue, inputX + 8, this.centeredTextY(mc, inputY, inputHeight), this.value.isEmpty() && !this.focused ? -9934744 : -723724);
    }

    @Override
    public boolean mouseClicked(class_11909 event, boolean bl) {
        this.focused = event.method_74245() == 0 && this.isHovered(event.comp_4798(), event.comp_4799());
        return this.focused;
    }

    @Override
    public boolean keyPressed(class_11908 event) {
        if (!this.focused) {
            return false;
        }
        if (event.comp_4795() == 256 || event.comp_4795() == 257 || event.comp_4795() == 335) {
            this.focused = false;
            return true;
        }
        if (event.comp_4795() == 259) {
            if (!this.value.isEmpty()) {
                this.value = this.value.substring(0, this.value.length() - 1);
                this.notifyChange();
            }
            return true;
        }
        return true;
    }

    @Override
    public boolean charTyped(class_11905 event) {
        if (!this.focused) {
            return false;
        }
        int codepoint = event.comp_4793();
        if (codepoint >= 32 && !Character.isISOControl(codepoint) && this.value.length() < 160) {
            this.value = this.value + Character.toString(codepoint);
            this.notifyChange();
        }
        return true;
    }

    private String fitFromEnd(class_310 mc, String text, int maxWidth) {
        String result = text;
        while (!result.isEmpty() && this.uiTextWidth(mc, result) > maxWidth) {
            result = result.substring(1);
        }
        return result;
    }

    private void notifyChange() {
        if (this.onChange != null) {
            this.onChange.accept(this.value);
        }
    }

    public boolean isFocused() {
        return this.focused;
    }
}

