/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  org.lwjgl.glfw.GLFW
 */
package net.fastclient.hud.gui.screens;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fastclient.hud.FastClientHUDClient;
import net.fastclient.hud.gui.screens.ModuleConfigScreen;
import net.fastclient.hud.modules.Module;
import net.fastclient.hud.render.AnimationUtils;
import net.fastclient.hud.render.Theme;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import org.lwjgl.glfw.GLFW;

@Environment(value=EnvType.CLIENT)
public class HudEditorScreen
extends class_437 {
    private final List<DraggableModule> draggables = new ArrayList<DraggableModule>();
    private DraggableModule dragging = null;
    private DraggableModule selected = null;
    private DraggableModule configTarget = null;
    private int dragOffsetX;
    private int dragOffsetY;
    private DraggableModule resizing = null;
    private int resizeStartX;
    private int resizeStartY;
    private float resizeStartScale;
    private boolean gridEnabled = true;
    private boolean snapToGrid = false;
    private boolean showGuides = true;
    private int gridSize = 10;
    private final Set<Integer> activeGuidesX = new HashSet<Integer>();
    private final Set<Integer> activeGuidesY = new HashSet<Integer>();
    private boolean configPanelOpen = false;
    private AnimationUtils.Animation configPanelAnimation;
    private static final int CONFIG_PANEL_WIDTH = 160;
    private boolean toolbarExpanded = true;
    private AnimationUtils.Animation toolbarAnimation;
    private static final int TOOLBAR_HEIGHT_COLLAPSED = 28;
    private static final int TOOLBAR_HEIGHT_EXPANDED = 66;
    private static final int SNAP_THRESHOLD = 8;
    private static final int NUDGE_AMOUNT = 1;
    private static final int NUDGE_AMOUNT_FAST = 10;
    private static final float SCALE_STEP = 0.1f;
    private static final float MIN_SCALE = 0.5f;
    private static final float MAX_SCALE = 3.0f;

    public HudEditorScreen() {
        super((class_2561)class_2561.method_43470((String)"HUD Editor"));
    }

    private boolean isShiftKeyDown() {
        long handle = class_310.method_1551().method_22683().method_4490();
        return GLFW.glfwGetKey((long)handle, (int)340) == 1 || GLFW.glfwGetKey((long)handle, (int)344) == 1;
    }

    protected void method_25426() {
        this.draggables.clear();
        if (this.toolbarAnimation == null) {
            this.toolbarAnimation = new AnimationUtils.Animation(1.0f, 150L);
        }
        if (this.configPanelAnimation == null) {
            this.configPanelAnimation = new AnimationUtils.Animation(0.0f, 150L);
        }
        for (Module module : FastClientHUDClient.getInstance().getModuleManager().getModules()) {
            if (!module.isEnabled() || !module.isHudVisible()) continue;
            this.draggables.add(new DraggableModule(module, module.getHudX(), module.getHudY()));
        }
    }

    public void method_25420(class_332 graphics, int mouseX, int mouseY, float delta) {
        graphics.method_25294(0, 0, this.field_22789, this.field_22790, 0x50000000);
    }

    public void method_25394(class_332 graphics, int mouseX, int mouseY, float delta) {
        if (this.dragging == null) {
            this.method_25420(graphics, mouseX, mouseY, delta);
            if (this.gridEnabled) {
                this.renderGrid(graphics);
            }
        }
        this.renderCenterGuides(graphics);
        if (this.dragging != null && this.showGuides) {
            this.renderAlignmentGuides(graphics);
        }
        for (DraggableModule dm : this.draggables) {
            this.renderDraggableModule(graphics, dm, mouseX, mouseY);
        }
        if (this.resizing != null) {
            this.renderPositionInfo(graphics, this.resizing);
        }
        if (this.dragging == null) {
            this.renderToolbar(graphics, mouseX, mouseY);
            if (this.configPanelOpen && this.configTarget != null) {
                this.renderConfigPanel(graphics, mouseX, mouseY);
            }
            this.renderFooter(graphics);
        }
        super.method_25394(graphics, mouseX, mouseY, delta);
    }

    private void renderCenterGuides(class_332 graphics) {
        int centerColor = Theme.withAlpha(-38091, 40);
        int centerX = this.field_22789 / 2;
        int centerY = this.field_22790 / 2;
        graphics.method_25294(centerX, 0, centerX + 1, this.field_22790, centerColor);
        graphics.method_25294(0, centerY, this.field_22789, centerY + 1, centerColor);
    }

    private void renderGrid(class_332 graphics) {
        int lineColor = Theme.withAlpha(-13817282, 20);
        for (int x = 0; x <= this.field_22789; x += this.gridSize) {
            graphics.method_25294(x, 0, x + 1, this.field_22790, lineColor);
        }
        for (int y = 0; y <= this.field_22790; y += this.gridSize) {
            graphics.method_25294(0, y, this.field_22789, y + 1, lineColor);
        }
    }

    private void renderAlignmentGuides(class_332 graphics) {
        int guideColor = Theme.withAlpha(-38091, 60);
        for (int x : this.activeGuidesX) {
            graphics.method_25294(x, 0, x + 1, this.field_22790, guideColor);
        }
        for (int y : this.activeGuidesY) {
            graphics.method_25294(0, y, this.field_22789, y + 1, guideColor);
        }
    }

    private void renderDraggableModule(class_332 graphics, DraggableModule dm, int mouseX, int mouseY) {
        int borderWidth;
        int borderColor;
        boolean isHovered = this.isHovered(dm, mouseX, mouseY);
        boolean isSelected = dm == this.selected;
        boolean isDragging = dm == this.dragging;
        boolean isResizing = dm == this.resizing;
        boolean isResizeHandleHovered = this.isResizeHandleHovered(dm, mouseX, mouseY);
        if (isDragging) {
            int bracketLen = 6;
            int bracketColor = Theme.withAlpha(-38091, 200);
            boolean hasOffset = !dm.module.getName().equals("Block Overlay");
            int pad = hasOffset ? (int)(2.0f * dm.scale) : 0;
            int bx = dm.x - pad;
            int by = dm.y - pad;
            int bw = dm.width;
            int bh = dm.height;
            graphics.method_25294(bx - 1, by - 1, bx + bracketLen, by, bracketColor);
            graphics.method_25294(bx - 1, by - 1, bx, by + bracketLen, bracketColor);
            graphics.method_25294(bx + bw - bracketLen, by - 1, bx + bw + 1, by, bracketColor);
            graphics.method_25294(bx + bw, by - 1, bx + bw + 1, by + bracketLen, bracketColor);
            graphics.method_25294(bx - 1, by + bh, bx + bracketLen, by + bh + 1, bracketColor);
            graphics.method_25294(bx - 1, by + bh - bracketLen, bx, by + bh + 1, bracketColor);
            graphics.method_25294(bx + bw - bracketLen, by + bh, bx + bw + 1, by + bh + 1, bracketColor);
            graphics.method_25294(bx + bw, by + bh - bracketLen, bx + bw + 1, by + bh + 1, bracketColor);
            return;
        }
        if (isResizing) {
            borderColor = Theme.withAlpha(-38091, 200);
            borderWidth = 2;
        } else if (isSelected) {
            borderColor = Theme.withAlpha(-38091, 180);
            borderWidth = 2;
        } else if (isHovered) {
            borderColor = Theme.withAlpha(-38091, 150);
            borderWidth = 2;
        } else {
            borderColor = Theme.withAlpha(-38091, 100);
            borderWidth = 2;
        }
        boolean hasOffset = !dm.module.getName().equals("Block Overlay");
        int pad = hasOffset ? (int)(2.0f * dm.scale) : 0;
        int bx = dm.x - pad;
        int by = dm.y - pad;
        int bw = dm.width;
        int bh = dm.height;
        graphics.method_25294(bx, by, bx + bw, by + borderWidth, borderColor);
        graphics.method_25294(bx, by + bh - borderWidth, bx + bw, by + bh, borderColor);
        graphics.method_25294(bx, by, bx + borderWidth, by + bh, borderColor);
        graphics.method_25294(bx + bw - borderWidth, by, bx + bw, by + bh, borderColor);
        if (isHovered || isSelected || isResizing) {
            int handleSize = 12;
            int handleX = bx + bw - handleSize;
            int handleY = by + bh - handleSize;
            int handleColor = isResizeHandleHovered || isResizing ? -22016 : Theme.withAlpha(-38091, 180);
            graphics.method_25294(handleX + 2, handleY + handleSize - 2, handleX + handleSize, handleY + handleSize, handleColor);
            graphics.method_25294(handleX + handleSize - 2, handleY + 2, handleX + handleSize, handleY + handleSize, handleColor);
            graphics.method_25294(handleX + 5, handleY + handleSize - 4, handleX + handleSize - 2, handleY + handleSize - 2, handleColor);
            graphics.method_25294(handleX + handleSize - 4, handleY + 5, handleX + handleSize - 2, handleY + handleSize - 2, handleColor);
            graphics.method_25294(handleX + 8, handleY + handleSize - 6, handleX + handleSize - 4, handleY + handleSize - 4, handleColor);
            graphics.method_25294(handleX + handleSize - 6, handleY + 8, handleX + handleSize - 4, handleY + handleSize - 4, handleColor);
        }
    }

    private void renderToolbar(class_332 graphics, int mouseX, int mouseY) {
        float animVal = this.toolbarAnimation.getValue();
        int toolbarWidth = 320;
        int toolbarX = (this.field_22789 - toolbarWidth) / 2;
        int toolbarY = 6;
        int currentHeight = (int)(28.0f + 38.0f * animVal);
        graphics.method_25294(toolbarX, toolbarY, toolbarX + toolbarWidth, toolbarY + currentHeight, Theme.withAlpha(-1341388268, 160));
        graphics.method_25294(toolbarX, toolbarY, toolbarX + toolbarWidth, toolbarY + 1, Theme.withAlpha(-38091, 120));
        graphics.method_51433(this.field_22793, "HUD Editor", toolbarX + 8, toolbarY + 6, Theme.withAlpha(-1, 180), false);
        String gridInfo = this.gridSize + "px";
        graphics.method_51433(this.field_22793, gridInfo, toolbarX + toolbarWidth - this.field_22793.method_1727(gridInfo) - 30, toolbarY + 6, Theme.withAlpha(-6646352, 150), false);
        String collapseIcon = this.toolbarExpanded ? "\u25b2" : "\u25bc";
        graphics.method_51433(this.field_22793, collapseIcon, toolbarX + toolbarWidth - 16, toolbarY + 6, Theme.withAlpha(-3356452, 150), false);
        if (animVal > 0.5f) {
            int btnX;
            int btnY = toolbarY + 22;
            int btnHeight = 18;
            int btnSpacing = 8;
            int gridStartX = btnX = toolbarX + 12;
            String gridText = this.gridEnabled ? "\u229e Grid" : "\u229f Grid";
            int gridBtnWidth = this.field_22793.method_1727(gridText) + 12;
            int gridBtnColor = this.gridEnabled ? Theme.withAlpha(-38091, 100) : -1608178890;
            graphics.method_25294(btnX, btnY, btnX + gridBtnWidth, btnY + btnHeight, gridBtnColor);
            graphics.method_51433(this.field_22793, gridText, btnX + 6, btnY + 5, this.gridEnabled ? -1 : -3356452, false);
            int snapStartX = btnX += gridBtnWidth + btnSpacing;
            String snapText = this.snapToGrid ? "\u25c9 Snap" : "\u25cb Snap";
            int snapBtnWidth = this.field_22793.method_1727(snapText) + 12;
            int snapBtnColor = this.snapToGrid ? Theme.withAlpha(-38091, 100) : -1608178890;
            graphics.method_25294(btnX, btnY, btnX + snapBtnWidth, btnY + btnHeight, snapBtnColor);
            graphics.method_51433(this.field_22793, snapText, btnX + 6, btnY + 5, this.snapToGrid ? -1 : -3356452, false);
            int guidesStartX = btnX += snapBtnWidth + btnSpacing;
            String guidesText = this.showGuides ? "\u25c8 Guides" : "\u25c7 Guides";
            int guidesBtnWidth = this.field_22793.method_1727(guidesText) + 12;
            int guidesBtnColor = this.showGuides ? Theme.withAlpha(-38091, 100) : -1608178890;
            graphics.method_25294(btnX, btnY, btnX + guidesBtnWidth, btnY + btnHeight, guidesBtnColor);
            graphics.method_51433(this.field_22793, guidesText, btnX + 6, btnY + 5, this.showGuides ? -1 : -3356452, false);
            int infoY = btnY + btnHeight + 4;
            int infoOn = Theme.withAlpha(-6646352, 140);
            int infoOff = Theme.withAlpha(-6646352, 70);
            graphics.method_51433(this.field_22793, "G \u00b7 overlay", gridStartX + 2, infoY, this.gridEnabled ? infoOn : infoOff, false);
            graphics.method_51433(this.field_22793, "S \u00b7 snap", snapStartX + 2, infoY, this.snapToGrid ? infoOn : infoOff, false);
            graphics.method_51433(this.field_22793, "A \u00b7 align", guidesStartX + 2, infoY, this.showGuides ? infoOn : infoOff, false);
        }
    }

    private void renderConfigPanel(class_332 graphics, int mouseX, int mouseY) {
        float panelAnim = this.configPanelAnimation.getValue();
        if (panelAnim < 0.01f) {
            return;
        }
        int panelX = Math.min(this.configTarget.x + this.configTarget.width + 10, this.field_22789 - 160 - 10);
        int panelY = Math.max(10, Math.min(this.configTarget.y, this.field_22790 - 200));
        int panelHeight = 150;
        int currentWidth = (int)(160.0f * panelAnim);
        graphics.method_25294(panelX, panelY, panelX + currentWidth, panelY + panelHeight, Theme.withAlpha(-1341388268, 240));
        graphics.method_25294(panelX, panelY, panelX + currentWidth, panelY + 2, -38091);
        if (panelAnim > 0.5f) {
            String title = this.configTarget.module.getDisplayName();
            graphics.method_51433(this.field_22793, title, panelX + 8, panelY + 10, -1, true);
            graphics.method_51433(this.field_22793, "Quick Actions", panelX + 8, panelY + 26, -6646352, false);
            int actionY = panelY + 45;
            graphics.method_51433(this.field_22793, "\u21ba Reset Position", panelX + 8, actionY, -3356452, false);
            graphics.method_51433(this.field_22793, "\u2194 Center Horizontal", panelX + 8, actionY += 22, -3356452, false);
            graphics.method_51433(this.field_22793, "\u2195 Center Vertical", panelX + 8, actionY += 22, -3356452, false);
            graphics.method_51433(this.field_22793, "\u2699 Full Settings...", panelX + 8, actionY += 28, -38091, false);
        }
    }

    private void renderPositionInfo(class_332 graphics, DraggableModule dm) {
        String posText = this.resizing != null && dm == this.resizing ? String.format("%d, %d  %.0f%%", dm.x, dm.y, Float.valueOf(dm.scale * 100.0f)) : String.format("%d, %d", dm.x, dm.y);
        int posWidth = this.field_22793.method_1727(posText);
        int infoX = dm.x + dm.width / 2 - posWidth / 2;
        int infoY = dm.y + dm.height + 4;
        infoX = Math.max(4, Math.min(this.field_22789 - posWidth - 4, infoX));
        if (infoY + 10 > this.field_22790 - 20) {
            infoY = dm.y - 12;
        }
        graphics.method_25294(infoX - 3, infoY - 1, infoX + posWidth + 3, infoY + 9, Theme.withAlpha(-16777216, 120));
        graphics.method_51433(this.field_22793, posText, infoX, infoY, Theme.withAlpha(-3356452, 180), false);
    }

    private void renderFooter(class_332 graphics) {
        String hints = "Drag \u2022 Right-click options \u2022 ESC to save";
        int hintsWidth = this.field_22793.method_1727(hints);
        graphics.method_51433(this.field_22793, hints, (this.field_22789 - hintsWidth) / 2, this.field_22790 - 12, Theme.withAlpha(-6646352, 100), false);
    }

    private boolean isHovered(DraggableModule dm, int mouseX, int mouseY) {
        int padding = 6;
        return mouseX >= dm.x - padding && mouseX <= dm.x + dm.width + padding && mouseY >= dm.y - padding && mouseY <= dm.y + dm.height + padding;
    }

    private boolean isResizeHandleHovered(DraggableModule dm, int mouseX, int mouseY) {
        int handleSize = 16;
        int handleX = dm.x + dm.width - handleSize;
        int handleY = dm.y + dm.height - handleSize;
        return mouseX >= handleX && mouseX <= dm.x + dm.width + 4 && mouseY >= handleY && mouseY <= dm.y + dm.height + 4;
    }

    public boolean method_25402(class_11909 event, boolean bl) {
        double mouseX = event.comp_4798();
        double mouseY = event.comp_4799();
        int button = event.method_74245();
        if (this.configPanelOpen && this.configTarget != null && this.configPanelAnimation.getValue() > 0.5f) {
            int panelX = Math.min(this.configTarget.x + this.configTarget.width + 10, this.field_22789 - 160 - 10);
            int panelY = Math.max(10, Math.min(this.configTarget.y, this.field_22790 - 200));
            int panelHeight = 150;
            int currentWidth = (int)(160.0f * this.configPanelAnimation.getValue());
            if (mouseX >= (double)panelX && mouseX <= (double)(panelX + currentWidth) && mouseY >= (double)panelY && mouseY <= (double)(panelY + panelHeight)) {
                return this.handleConfigPanelClick((int)mouseX, (int)mouseY, panelX, panelY);
            }
            this.configPanelAnimation.animateTo(0.0f);
            this.configPanelOpen = false;
            return true;
        }
        if (button == 1) {
            for (DraggableModule dm : this.draggables) {
                if (!this.isHovered(dm, (int)mouseX, (int)mouseY)) continue;
                this.configTarget = dm;
                this.configPanelOpen = true;
                this.configPanelAnimation.animateTo(1.0f);
                return true;
            }
        }
        if (button == 0) {
            if (this.configPanelOpen) {
                this.configPanelAnimation.animateTo(0.0f);
                this.configPanelOpen = false;
            }
            for (DraggableModule dm : this.draggables) {
                if (!this.isResizeHandleHovered(dm, (int)mouseX, (int)mouseY)) continue;
                this.resizing = dm;
                this.selected = dm;
                this.resizeStartX = (int)mouseX;
                this.resizeStartY = (int)mouseY;
                this.resizeStartScale = dm.scale;
                return true;
            }
            for (DraggableModule dm : this.draggables) {
                if (!this.isHovered(dm, (int)mouseX, (int)mouseY)) continue;
                this.dragging = dm;
                this.selected = dm;
                this.dragOffsetX = (int)mouseX - dm.x;
                this.dragOffsetY = (int)mouseY - dm.y;
                return true;
            }
        }
        if (this.toolbarExpanded && this.toolbarAnimation.getValue() > 0.5f) {
            int toolbarWidth = 360;
            int toolbarX = (this.field_22789 - toolbarWidth) / 2;
            int toolbarY = 10;
            int currentHeight = (int)(66.0f * this.toolbarAnimation.getValue());
            if (mouseX >= (double)toolbarX && mouseX <= (double)(toolbarX + toolbarWidth) && mouseY >= (double)toolbarY && mouseY <= (double)(toolbarY + currentHeight)) {
                return this.handleToolbarClick((int)mouseX, (int)mouseY, toolbarX, toolbarY);
            }
        }
        if (button == 0) {
            this.selected = null;
        }
        return super.method_25402(event, bl);
    }

    private boolean handleToolbarClick(int mouseX, int mouseY, int toolbarX, int toolbarY) {
        int btnY = toolbarY + 22;
        int btnHeight = 18;
        int btnSpacing = 8;
        int btnX = toolbarX + 12;
        String gridText = this.gridEnabled ? "\u229e Grid" : "\u229f Grid";
        int gridBtnWidth = this.field_22793.method_1727(gridText) + 12;
        if (mouseX >= btnX && mouseX <= btnX + gridBtnWidth && mouseY >= btnY && mouseY <= btnY + btnHeight) {
            this.gridEnabled = !this.gridEnabled;
            return true;
        }
        String snapText = this.snapToGrid ? "\u25c9 Snap" : "\u25cb Snap";
        int snapBtnWidth = this.field_22793.method_1727(snapText) + 12;
        if (mouseX >= (btnX += gridBtnWidth + btnSpacing) && mouseX <= btnX + snapBtnWidth && mouseY >= btnY && mouseY <= btnY + btnHeight) {
            this.snapToGrid = !this.snapToGrid;
            return true;
        }
        String guidesText = this.showGuides ? "\u25c8 Guides" : "\u25c7 Guides";
        int guidesBtnWidth = this.field_22793.method_1727(guidesText) + 12;
        if (mouseX >= (btnX += snapBtnWidth + btnSpacing) && mouseX <= btnX + guidesBtnWidth && mouseY >= btnY && mouseY <= btnY + btnHeight) {
            this.showGuides = !this.showGuides;
            return true;
        }
        return true;
    }

    private boolean handleConfigPanelClick(int mouseX, int mouseY, int panelX, int panelY) {
        if (this.configTarget == null) {
            return false;
        }
        int actionY = panelY + 45;
        int actionHeight = 16;
        if (mouseY >= actionY && mouseY <= actionY + actionHeight) {
            this.configTarget.x = 10;
            this.configTarget.y = 10;
            this.configTarget.module.setHudPosition(10, 10);
            return true;
        }
        if (mouseY >= (actionY += 22) && mouseY <= actionY + actionHeight) {
            this.configTarget.x = (this.field_22789 - this.configTarget.width) / 2;
            this.configTarget.module.setHudPosition(this.configTarget.x, this.configTarget.y);
            return true;
        }
        if (mouseY >= (actionY += 22) && mouseY <= actionY + actionHeight) {
            this.configTarget.y = (this.field_22790 - this.configTarget.height) / 2;
            this.configTarget.module.setHudPosition(this.configTarget.x, this.configTarget.y);
            return true;
        }
        if (mouseY >= (actionY += 28) && mouseY <= actionY + actionHeight) {
            class_310.method_1551().method_1507((class_437)new ModuleConfigScreen(this.configTarget.module, this));
            return true;
        }
        return true;
    }

    public boolean method_25406(class_11909 event) {
        int button = event.method_74245();
        if (button == 0) {
            if (this.resizing != null) {
                this.resizing.module.setHudScale(this.resizing.scale);
                this.resizing = null;
                return true;
            }
            if (this.dragging != null) {
                this.dragging.module.setHudPosition(this.dragging.x, this.dragging.y);
                this.dragging = null;
                this.activeGuidesX.clear();
                this.activeGuidesY.clear();
                return true;
            }
        }
        return super.method_25406(event);
    }

    public boolean method_25403(class_11909 event, double deltaX, double deltaY) {
        double mouseX = event.comp_4798();
        double mouseY = event.comp_4799();
        if (this.resizing != null) {
            int deltaFromStart = (int)mouseX - this.resizeStartX + (int)mouseY - this.resizeStartY;
            float scaleDelta = (float)deltaFromStart / 100.0f;
            this.resizing.setScale(this.resizeStartScale + scaleDelta);
            this.resizing.module.setHudScale(this.resizing.scale);
            return true;
        }
        if (this.dragging != null) {
            int newX = (int)mouseX - this.dragOffsetX;
            int newY = (int)mouseY - this.dragOffsetY;
            this.activeGuidesX.clear();
            this.activeGuidesY.clear();
            if (this.snapToGrid) {
                newX = Math.round((float)newX / (float)this.gridSize) * this.gridSize;
                newY = Math.round((float)newY / (float)this.gridSize) * this.gridSize;
            }
            if (this.showGuides && this.snapToGrid) {
                int draggingCenterX = newX + this.dragging.width / 2;
                int draggingCenterY = newY + this.dragging.height / 2;
                int draggingRight = newX + this.dragging.width;
                int draggingBottom = newY + this.dragging.height;
                int screenCenterX = this.field_22789 / 2;
                int screenCenterY = this.field_22790 / 2;
                if (Math.abs(draggingCenterX - screenCenterX) < 8) {
                    newX = screenCenterX - this.dragging.width / 2;
                    this.activeGuidesX.add(screenCenterX);
                }
                if (Math.abs(draggingCenterY - screenCenterY) < 8) {
                    newY = screenCenterY - this.dragging.height / 2;
                    this.activeGuidesY.add(screenCenterY);
                }
                if (Math.abs(newX) < 8) {
                    newX = 0;
                    this.activeGuidesX.add(0);
                }
                if (Math.abs(draggingRight - this.field_22789) < 8) {
                    newX = this.field_22789 - this.dragging.width;
                    this.activeGuidesX.add(this.field_22789);
                }
                if (Math.abs(newY) < 8) {
                    newY = 0;
                    this.activeGuidesY.add(0);
                }
                if (Math.abs(draggingBottom - this.field_22790) < 8) {
                    newY = this.field_22790 - this.dragging.height;
                    this.activeGuidesY.add(this.field_22790);
                }
                for (DraggableModule other : this.draggables) {
                    if (other == this.dragging) continue;
                    int otherCenterX = other.x + other.width / 2;
                    int otherCenterY = other.y + other.height / 2;
                    int otherRight = other.x + other.width;
                    int otherBottom = other.y + other.height;
                    if (Math.abs(newX - other.x) < 8) {
                        newX = other.x;
                        this.activeGuidesX.add(other.x);
                    }
                    if (Math.abs(draggingRight - otherRight) < 8) {
                        newX = otherRight - this.dragging.width;
                        this.activeGuidesX.add(otherRight);
                    }
                    if (Math.abs(newY - other.y) < 8) {
                        newY = other.y;
                        this.activeGuidesY.add(other.y);
                    }
                    if (Math.abs(draggingBottom - otherBottom) < 8) {
                        newY = otherBottom - this.dragging.height;
                        this.activeGuidesY.add(otherBottom);
                    }
                    if (Math.abs(draggingCenterX - otherCenterX) < 8) {
                        newX = otherCenterX - this.dragging.width / 2;
                        this.activeGuidesX.add(otherCenterX);
                    }
                    if (Math.abs(draggingCenterY - otherCenterY) >= 8) continue;
                    newY = otherCenterY - this.dragging.height / 2;
                    this.activeGuidesY.add(otherCenterY);
                }
            }
            this.dragging.x = Math.max(0, Math.min(this.field_22789 - this.dragging.width, newX));
            this.dragging.y = Math.max(0, Math.min(this.field_22790 - this.dragging.height, newY));
            this.dragging.module.setHudPosition(this.dragging.x, this.dragging.y);
            return true;
        }
        return super.method_25403(event, deltaX, deltaY);
    }

    public boolean method_25401(double mouseX, double mouseY, double horizAmount, double vertAmount) {
        if (this.toolbarExpanded && this.toolbarAnimation.getValue() > 0.5f) {
            int toolbarWidth = 360;
            int toolbarX = (this.field_22789 - toolbarWidth) / 2;
            int toolbarY = 10;
            int currentHeight = (int)(66.0f * this.toolbarAnimation.getValue());
            if (mouseX >= (double)toolbarX && mouseX <= (double)(toolbarX + toolbarWidth) && mouseY >= (double)toolbarY && mouseY <= (double)(toolbarY + currentHeight)) {
                this.gridSize = vertAmount > 0.0 ? Math.min(50, this.gridSize + 5) : Math.max(5, this.gridSize - 5);
                return true;
            }
        }
        if (this.isShiftKeyDown()) {
            this.gridSize = vertAmount > 0.0 ? Math.min(50, this.gridSize + 5) : Math.max(5, this.gridSize - 5);
            return true;
        }
        return super.method_25401(mouseX, mouseY, horizAmount, vertAmount);
    }

    public boolean method_25404(class_11908 event) {
        int keyCode = event.comp_4795();
        if (keyCode == 258) {
            this.toolbarExpanded = !this.toolbarExpanded;
            this.toolbarAnimation.animateTo(this.toolbarExpanded ? 1.0f : 0.0f);
            return true;
        }
        if (keyCode == 71) {
            this.gridEnabled = !this.gridEnabled;
            return true;
        }
        if (keyCode == 83) {
            this.snapToGrid = !this.snapToGrid;
            return true;
        }
        if (keyCode == 65) {
            this.showGuides = !this.showGuides;
            return true;
        }
        if ((keyCode == 61 || keyCode == 334) && this.selected != null) {
            float step = this.isShiftKeyDown() ? 0.5f : 0.1f;
            this.selected.setScale(this.selected.scale + step);
            this.selected.module.setHudScale(this.selected.scale);
            return true;
        }
        if ((keyCode == 45 || keyCode == 333) && this.selected != null) {
            float step = this.isShiftKeyDown() ? 0.5f : 0.1f;
            this.selected.setScale(this.selected.scale - step);
            this.selected.module.setHudScale(this.selected.scale);
            return true;
        }
        if ((keyCode == 48 || keyCode == 320) && this.selected != null) {
            this.selected.setScale(1.0f);
            this.selected.module.setHudScale(this.selected.scale);
            return true;
        }
        if (this.selected != null) {
            int nudge = this.isShiftKeyDown() ? 10 : 1;
            boolean moved = false;
            switch (keyCode) {
                case 262: {
                    this.selected.x = Math.min(this.field_22789 - this.selected.width, this.selected.x + nudge);
                    moved = true;
                    break;
                }
                case 263: {
                    this.selected.x = Math.max(0, this.selected.x - nudge);
                    moved = true;
                    break;
                }
                case 264: {
                    this.selected.y = Math.min(this.field_22790 - this.selected.height, this.selected.y + nudge);
                    moved = true;
                    break;
                }
                case 265: {
                    this.selected.y = Math.max(0, this.selected.y - nudge);
                    moved = true;
                }
            }
            if (moved) {
                if (this.snapToGrid) {
                    this.selected.x = Math.round((float)this.selected.x / (float)this.gridSize) * this.gridSize;
                    this.selected.y = Math.round((float)this.selected.y / (float)this.gridSize) * this.gridSize;
                }
                this.selected.module.setHudPosition(this.selected.x, this.selected.y);
                return true;
            }
        }
        return super.method_25404(event);
    }

    public void method_25419() {
        for (DraggableModule dm : this.draggables) {
            dm.module.setHudPosition(dm.x, dm.y);
            dm.module.setHudScale(dm.scale);
        }
        FastClientHUDClient.getInstance().getModuleManager().saveConfig();
        super.method_25419();
    }

    public boolean method_25421() {
        return false;
    }

    @Environment(value=EnvType.CLIENT)
    private static class DraggableModule {
        Module module;
        int x;
        int y;
        int width;
        int height;
        float scale;

        DraggableModule(Module module, int x, int y) {
            this.module = module;
            this.x = x;
            this.y = y;
            this.scale = module.getHudScale();
            this.updateDimensions();
        }

        void updateDimensions() {
            this.width = (int)((float)this.module.getHudWidth() * this.scale);
            this.height = (int)((float)this.module.getHudHeight() * this.scale);
        }

        void setScale(float newScale) {
            this.scale = Math.max(0.5f, Math.min(3.0f, newScale));
            this.updateDimensions();
        }
    }
}

