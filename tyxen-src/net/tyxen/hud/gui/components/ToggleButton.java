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
package net.tyxen.hud.gui.components;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.gui.TyxenUI;
import net.tyxen.hud.gui.components.UIComponent;
import net.tyxen.hud.render.AnimationUtils;
import net.tyxen.hud.render.Theme;
import net.minecraft.class_11909;
import net.minecraft.class_310;
import net.minecraft.class_332;

@Environment(value=EnvType.CLIENT)
public class ToggleButton
extends UIComponent {
    private boolean enabled;
    private final Consumer<Boolean> onChange;
    private final String label;
    private float toggleProgress;
    private final AnimationUtils.Spring toggleSpring = new AnimationUtils.Spring(0.0f);
    private float hoverProgress;
    private long lastUpdate = System.currentTimeMillis();

    public ToggleButton(int x, int y, int width, int height, String label, boolean initialState, Consumer<Boolean> onChange) {
        super(x, y, width, height);
        this.label = label;
        this.enabled = initialState;
        this.onChange = onChange;
        this.toggleProgress = initialState ? 1.0f : 0.0f;
        this.toggleSpring.reset(initialState ? 1.0f : 0.0f);
    }

    @Override
    public void render(class_332 graphics, int mouseX, int mouseY, float delta) {
        this.hovered = this.isHovered(mouseX, mouseY);
        long now = System.currentTimeMillis();
        float dt = (float)(now - this.lastUpdate) / 1000.0f;
        this.lastUpdate = now;
        this.toggleSpring.animateTo(this.enabled ? 1.0f : 0.0f);
        this.toggleProgress = this.toggleSpring.update(dt);
        this.hoverProgress = AnimationUtils.smoothDelta(this.hoverProgress, this.hovered ? 1.0f : 0.0f, 0.4f, dt * 60.0f);
        class_310 mc = class_310.method_1551();
        if (this.hoverProgress > 0.01f) {
            int bgAlpha = (int)(20.0f * this.hoverProgress);
            TyxenUI.roundedRect(graphics, this.x, this.y, this.width, this.height, 4, TyxenUI.withAlpha(-266722777, bgAlpha));
        }
        String displayLabel = Theme.formatSettingName(this.label);
        int labelColor = TyxenUI.blend(-7303024, -723724, this.hoverProgress);
        this.drawUiText(graphics, mc, displayLabel, this.x + 12, this.centeredTextY(mc, this.y, this.height), labelColor);
        int toggleWidth = 36;
        int toggleHeight = 18;
        int toggleX = this.x + this.width - toggleWidth - 12;
        int toggleY = this.y + (this.height - toggleHeight) / 2;
        int trackColor = TyxenUI.blend(-435153640, -15511009, this.toggleProgress);
        TyxenUI.roundedRect(graphics, toggleX, toggleY, toggleWidth, toggleHeight, 9, trackColor);
        int handlePadding = 3;
        int handleWidth = 12;
        int handleX = toggleX + handlePadding + (int)((float)(toggleWidth - handleWidth - handlePadding * 2) * this.toggleProgress);
        int handleY = toggleY + handlePadding;
        int handleHeight = toggleHeight - handlePadding * 2;
        int handleColor = this.enabled ? -723724 : -7303024;
        TyxenUI.roundedRect(graphics, handleX, handleY, handleWidth, handleHeight, 6, handleColor);
        String status = this.enabled ? "ON" : "OFF";
        int statusColor = this.enabled ? -15243738 : -9934744;
        int statusX = toggleX - this.uiTextWidth(mc, status) - 8;
        this.drawUiText(graphics, mc, status, statusX, this.centeredTextY(mc, this.y, this.height), statusColor);
    }

    @Override
    public boolean mouseClicked(class_11909 event, boolean bl) {
        if (super.mouseClicked(event, bl) && event.method_74245() == 0) {
            boolean bl2 = this.enabled = !this.enabled;
            if (this.onChange != null) {
                this.onChange.accept(this.enabled);
            }
            return true;
        }
        return false;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

