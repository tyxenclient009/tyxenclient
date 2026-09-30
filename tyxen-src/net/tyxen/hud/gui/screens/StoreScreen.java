/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10799
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package net.tyxen.hud.gui.screens;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.gui.DisplaySpace;
import net.tyxen.hud.gui.TyxenUI;
import net.tyxen.hud.render.AnimationUtils;
import net.tyxen.hud.store.CosmeticCategory;
import net.tyxen.hud.store.StoreCosmetic;
import net.tyxen.hud.store.StoreManager;
import net.tyxen.hud.store.StoreState;
import net.minecraft.class_10799;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

@Environment(value=EnvType.CLIENT)
public class StoreScreen
extends class_437 {
    private static final class_2960 LOGO_TEXTURE = class_2960.method_60655((String)"tyxen", (String)"textures/gui/title-tyxen-logo.png");
    private static final class_2960 DIAMOND_TEXTURE = class_2960.method_60655((String)"tyxen", (String)"textures/gui/title/diamond.png");
    private static final class_2960 COIN_TEXTURE = class_2960.method_60655((String)"tyxen", (String)"textures/gui/title/coin.png");
    private static final class_2960 SEARCH_ICON = class_2960.method_60655((String)"tyxen", (String)"textures/gui/title/fc_search.png");
    private static final int CLOSE_BUTTON_SIZE = 30;
    private static final int HEADER_ROW_GAP = 6;
    private static final int HEADER_PANEL_GAP = 6;
    private static final int TAB_HEIGHT = 30;
    private static final int CATEGORY_BAR_H = 126;
    private static final int CATEGORY_TAB_MAX_SIZE = 46;
    private static final int CATEGORY_TAB_MIN_SIZE = 34;
    private static final int CATEGORY_TAB_GAP = 6;
    private static final int CONTENT_PAD = 24;
    private static final int CONTROL_GAP = 8;
    private static final int CLAIM_BUTTON_W = 118;
    private static final int COIN_PILL_W = 112;
    private static final int OWNED_TOGGLE_W = 108;
    private static final int REMOVE_ALL_W = 104;
    private static final int CARD_W = 178;
    private static final int CARD_H = 218;
    private static final int CARD_SPACING = 12;
    private static final int GRID_PADDING = 10;
    private static final int SCROLLBAR_W = 5;
    private final class_437 previousScreen;
    private final StoreManager storeManager;
    private final StoreState state;
    private volatile String onlineText = "...";
    private long onlineNextFetch = 0L;
    private final List<float[]> coinFly = new ArrayList<float[]>();
    private int shownBalance = -1;
    private int animFrom = 0;
    private int animTo = 0;
    private long animT0 = 0L;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private CosmeticCategory selectedCategory = CosmeticCategory.FEATURED;
    private String searchQuery = "";
    private boolean ownedOnly = false;
    private boolean searchFocused = false;
    private AnimationUtils.Animation openAnimation;
    private final float[] tabHoverProgress = new float[CosmeticCategory.values().length];
    private long lastUpdate = System.currentTimeMillis();
    private long cursorBlinkTime = 0L;
    private String statusMessage = "";
    private long statusMessageUntil = 0L;
    private double scrollOffset = 0.0;
    private double targetScrollOffset = 0.0;
    private double maxScroll = 0.0;
    private boolean scrollbarDragging = false;
    private double dragStartY = 0.0;
    private double dragStartScroll = 0.0;
    private final List<StoreCosmetic> visibleItems = new ArrayList<StoreCosmetic>();
    private final List<int[]> cardRects = new ArrayList<int[]>();

    public StoreScreen(class_437 previousScreen) {
        super((class_2561)class_2561.method_43470((String)"Tyxen Store"));
        this.previousScreen = previousScreen;
        this.storeManager = StoreManager.getInstance();
        this.state = this.storeManager.getState();
        this.selectedCategory = this.parseSavedCategory(this.state.getLastSelectedCategory());
    }

    protected void method_25426() {
        this.storeManager.refreshFromBackend();
        int displayWidth = DisplaySpace.width();
        int displayHeight = DisplaySpace.height();
        this.panelWidth = Math.min(Math.max(760, (int)((float)displayWidth * 0.7f)), Math.max(1, displayWidth - 48));
        this.panelHeight = Math.min(Math.max(480, (int)((float)displayHeight * 0.74f)), Math.max(1, displayHeight - 96));
        this.panelX = (displayWidth - this.panelWidth) / 2;
        this.panelY = Math.max(74, (displayHeight - this.panelHeight) / 2 + 18);
        if (this.openAnimation == null) {
            this.openAnimation = new AnimationUtils.Animation(0.0f, 200L);
            this.openAnimation.setEasing(AnimationUtils::easeOutCubic);
        }
        this.openAnimation.animateTo(1.0f);
        this.targetScrollOffset = this.state.getScrollOffset();
        this.scrollOffset = this.state.getScrollOffset();
    }

    private CosmeticCategory parseSavedCategory(String savedCategory) {
        return CosmeticCategory.fromSerialized(savedCategory);
    }

    private int gridX() {
        return this.panelX + 4;
    }

    private int gridY() {
        return this.panelY + 126;
    }

    private int gridW() {
        return this.panelWidth - 8 - 5 - 6;
    }

    private int gridH() {
        return this.panelHeight - 126 - 12;
    }

    public void method_25420(class_332 graphics, int mouseX, int mouseY, float delta) {
        graphics.method_25294(0, 0, DisplaySpace.width(), DisplaySpace.height(), TyxenUI.withAlpha(-16777216, 55));
    }

    public void method_25394(class_332 graphics, int mouseX, int mouseY, float delta) {
        int pxMouseX = DisplaySpace.mouseX(mouseX);
        int pxMouseY = DisplaySpace.mouseY(mouseY);
        DisplaySpace.push(graphics);
        this.method_25420(graphics, pxMouseX, pxMouseY, delta);
        long now = System.currentTimeMillis();
        float dt = (float)(now - this.lastUpdate) / 1000.0f;
        this.lastUpdate = now;
        float animProgress = this.openAnimation.getValue();
        int alpha = (int)(animProgress * 255.0f);
        this.drawPanelBackground(graphics, alpha);
        this.drawHeader(graphics, alpha, pxMouseX, pxMouseY);
        this.drawCategoryTabs(graphics, pxMouseX, pxMouseY, alpha, dt);
        this.drawSearchBar(graphics, alpha, pxMouseX, pxMouseY);
        this.drawStatus(graphics, alpha);
        this.drawProductGrid(graphics, pxMouseX, pxMouseY, alpha, dt);
        super.method_25394(graphics, mouseX, mouseY, delta);
        DisplaySpace.pop(graphics);
    }

    private void drawPanelBackground(class_332 graphics, int alpha) {
        graphics.method_25294(this.panelX, this.panelY, this.panelX + this.panelWidth, this.panelY + this.panelHeight, TyxenUI.withAlpha(-234156528, alpha));
        graphics.method_25294(this.panelX, this.panelY, this.panelX + this.panelWidth, this.panelY + 126, TyxenUI.withAlpha(-15986412, Math.min(alpha, 230)));
        graphics.method_25294(this.panelX, this.gridY() - 1, this.panelX + this.panelWidth, this.gridY(), TyxenUI.withAlpha(1143616571, alpha));
    }

    private void drawHeader(class_332 graphics, int alpha, int mouseX, int mouseY) {
        boolean claimHovered;
        int headerH = 32;
        int headerY = this.getHeaderNavY();
        int brandY = this.getHeaderBrandY();
        int logoH = 24;
        int logoW = Math.round((float)logoH * 3.167702f);
        graphics.method_51448().pushMatrix();
        graphics.method_51448().translate((float)this.panelX, (float)(brandY + 4));
        graphics.method_51448().scale((float)logoW / 510.0f, (float)logoH / 161.0f);
        graphics.method_25291(class_10799.field_56883, LOGO_TEXTURE, 0, 0, 0.0f, 0.0f, 510, 161, 510, 161, TyxenUI.withAlpha(-1, alpha));
        graphics.method_51448().popMatrix();
        int storeBtnW = this.panelWidth < 1050 ? 152 : 168;
        graphics.method_25294(this.panelX, headerY, this.panelX + storeBtnW, headerY + headerH, TyxenUI.withAlpha(-14498466, alpha));
        graphics.method_51433(this.field_22793, "Cosmetic Store", this.panelX + 14, headerY + 12, TyxenUI.withAlpha(-723724, alpha), true);
        int coinX = this.panelX + storeBtnW + 8;
        graphics.method_25294(coinX, headerY, coinX + 112, headerY + headerH, TyxenUI.withAlpha(-435153640, alpha));
          int iconSize = 18;
          graphics.method_25291(class_10799.field_56883, COIN_TEXTURE, coinX + 11, headerY + 7, 0.0f, 0.0f, iconSize, iconSize, 64, 64, TyxenUI.withAlpha(-1, alpha));
          if (this.shownBalance < 0) {
              this.shownBalance = this.state.getCoinBalance();
              this.animFrom = this.shownBalance;
              this.animTo = this.shownBalance;
              this.animT0 = 0L;
          }
          long nowA = System.currentTimeMillis();
          if (this.animTo != this.animFrom && nowA >= this.animT0) {
              float at = Math.min(1.0f, (float)(nowA - this.animT0) / 900.0f);
              this.shownBalance = this.animFrom + Math.round((float)(this.animTo - this.animFrom) * at);
              if (at >= 1.0f) {
                  this.animFrom = this.animTo;
              }
          } else if (this.animTo == this.animFrom) {
              this.shownBalance = this.state.getCoinBalance();
              this.animFrom = this.shownBalance;
          }
          String coinText = String.valueOf(this.shownBalance);
          graphics.method_51433(this.field_22793, coinText, coinX + 36, headerY + 12, TyxenUI.withAlpha(-723724, alpha), true);
        int claimX = coinX + 112 + 8;
        boolean canClaim = this.state.canClaimDailyCoins();
        boolean bl = claimHovered = mouseX >= claimX && mouseX <= claimX + 118 && mouseY >= headerY && mouseY <= headerY + headerH;
        int claimBg = canClaim ? TyxenUI.withAlpha(claimHovered ? -11870592 : -14498466, alpha) : TyxenUI.withAlpha(claimHovered ? -266722777 : -435153640, alpha);
        graphics.method_25294(claimX, headerY, claimX + 118, headerY + headerH, claimBg);
          String claimText = canClaim ? "Claim +" + this.storeManager.getDailyCoinClaimAmount() : "Claimed";
          if (System.currentTimeMillis() > this.onlineNextFetch) {
              this.onlineNextFetch = System.currentTimeMillis() + 30000L;
              this.storeManager.onlineCount().thenAccept(a -> this.onlineText = a[0] + " online / " + a[1]);
          }
          int onW = this.field_22793.method_1727(this.onlineText) + 24;
          int onX = claimX + 118 + 8;
          graphics.method_25294(onX, headerY, onX + onW, headerY + headerH, TyxenUI.withAlpha(-435153640, alpha));
          graphics.method_51433(this.field_22793, this.onlineText, onX + 12, headerY + 12, TyxenUI.withAlpha(-723724, alpha), true);
          long nowC = System.currentTimeMillis();
          for (int pi = this.coinFly.size() - 1; pi >= 0; --pi) {
              float[] cp = this.coinFly.get(pi);
              float ct = (float)(nowC - cp[0]) / cp[1];
              if (ct < 0.0f) {
                  continue;
              }
              if (ct >= 1.0f) {
                  this.coinFly.remove(pi);
                  continue;
              }
              float cx = cp[2] + (cp[4] - cp[2]) * ct;
              float cy = cp[3] + (cp[5] - cp[3]) * ct - (float)(Math.sin(ct * Math.PI) * 34.0);
              float spin = Math.abs((float)Math.cos(ct * Math.PI * 3.0f));
              int cw = Math.max(3, (int)(17.0f * spin));
              int fade = ct > 0.75f ? (int)((1.0f - ct) * 4.0f * 255.0f) : 255;
              int ccol = (fade << 24) | 0xFFFFFF;
              graphics.method_25291(class_10799.field_56883, COIN_TEXTURE, (int)(cx - (float)cw / 2.0f), (int)(cy - 8.0f), 0.0f, 0.0f, cw, 16, 64, 64, ccol);
          }
        graphics.method_51433(this.field_22793, claimText, claimX + (118 - this.field_22793.method_1727(claimText)) / 2, headerY + 12, TyxenUI.withAlpha(canClaim ? -723724 : -9934744, alpha), true);
        int closeX = this.panelX + this.panelWidth - 30;
        int closeY = headerY + (headerH - 30) / 2;
        boolean closeHovered = mouseX >= closeX && mouseX <= closeX + 30 && mouseY >= closeY && mouseY <= closeY + 30;
        graphics.method_25294(closeX, closeY, closeX + 30, closeY + 30, TyxenUI.withAlpha(closeHovered ? -266722777 : -435153640, alpha));
        int n = closeX + (30 - this.field_22793.method_1727("X")) / 2;
        Objects.requireNonNull(this.field_22793);
        graphics.method_51433(this.field_22793, "X", n, closeY + (30 - 9) / 2 + 1, TyxenUI.withAlpha(closeHovered ? -723724 : -7303024, alpha), true);
    }

    private void drawCategoryTabs(class_332 graphics, int mouseX, int mouseY, int alpha, float dt) {
        CosmeticCategory[] categories = CosmeticCategory.values();
        int tabSize = this.getCategoryTabSize();
        int tabGap = this.getCategoryTabGap();
        int tabY = this.getCategoryTabsY();
        int tabX = this.getCategoryTabsStartX();
        CosmeticCategory labelCategory = this.selectedCategory;
        for (int i = 0; i < categories.length; ++i) {
            boolean isHovered;
            CosmeticCategory cat = categories[i];
            boolean isSelected = cat == this.selectedCategory;
            boolean bl = isHovered = mouseX >= tabX && mouseX <= tabX + tabSize && mouseY >= tabY && mouseY <= tabY + tabSize;
            if (isHovered) {
                labelCategory = cat;
            }
            this.tabHoverProgress[i] = AnimationUtils.smoothDelta(this.tabHoverProgress[i], isHovered ? 1.0f : 0.0f, 0.4f, dt * 60.0f);
            boolean drawHoverTexture = isSelected || this.tabHoverProgress[i] > 0.2f;
            class_2960 categoryIcon = DisplaySpace.texture(this.categoryIcon(cat, drawHoverTexture));
            graphics.method_25291(class_10799.field_56883, categoryIcon, tabX, tabY, 0.0f, 0.0f, tabSize, tabSize, tabSize, tabSize, TyxenUI.withAlpha(-1, alpha));
            if (isSelected) {
                graphics.method_25294(tabX + 4, tabY + tabSize - 3, tabX + tabSize - 4, tabY + tabSize - 1, TyxenUI.withAlpha(-11870592, alpha));
            } else if (isHovered) {
                TyxenUI.outline(graphics, tabX, tabY, tabSize, tabSize, TyxenUI.withAlpha(1143616571, alpha));
            }
            tabX += tabSize + tabGap;
        }
        this.drawSelectedCategoryLabel(graphics, labelCategory, this.getCategoryLabelY(tabSize), alpha);
    }

    private void drawSearchBar(class_332 graphics, int alpha, int mouseX, int mouseY) {
        boolean ownedHovered;
        StoreControlsLayout layout = this.getStoreControlsLayout();
        int searchBarY = layout.searchY;
        int searchBarX = layout.searchX;
        int searchH = 30;
        int searchW = layout.searchW;
        int searchBg = this.searchFocused ? TyxenUI.withAlpha(-266722777, alpha) : TyxenUI.withAlpha(-435153640, alpha);
        graphics.method_25294(searchBarX, searchBarY, searchBarX + searchW, searchBarY + searchH, searchBg);
        TyxenUI.outline(graphics, searchBarX, searchBarY, searchW, searchH, this.searchFocused ? TyxenUI.withAlpha(-14498466, alpha) : TyxenUI.withAlpha(1143616571, alpha));
        int iconSize = 14;
        graphics.method_25291(class_10799.field_56883, DisplaySpace.texture(SEARCH_ICON), searchBarX + 10, searchBarY + 8, 0.0f, 0.0f, iconSize, iconSize, iconSize, iconSize, TyxenUI.withAlpha(-1, alpha));
        int textX = searchBarX + 32;
        int textY = searchBarY + 11;
        if (this.searchQuery.isEmpty() && !this.searchFocused) {
            graphics.method_51433(this.field_22793, "Search cosmetics...", textX, textY, TyxenUI.withAlpha(-7303024, alpha), false);
        } else {
            long time;
            String displayText = this.fitText(this.searchQuery, searchW - 44);
            graphics.method_51433(this.field_22793, displayText, textX, textY, TyxenUI.withAlpha(-723724, alpha), false);
            if (this.searchFocused && ((time = System.currentTimeMillis()) - this.cursorBlinkTime) % 1000L < 500L) {
                int cursorX = textX + this.field_22793.method_1727(this.searchQuery);
                cursorX = Math.min(cursorX, searchBarX + searchW - 8);
                graphics.method_25294(cursorX, textY - 2, cursorX + 1, textY + 14, TyxenUI.withAlpha(-723724, alpha));
            }
        }
        boolean bl = ownedHovered = mouseX >= layout.ownedX && mouseX <= layout.ownedX + 108 && mouseY >= searchBarY && mouseY <= searchBarY + searchH;
        int ownedBg = this.ownedOnly ? TyxenUI.withAlpha(-15511009, alpha) : TyxenUI.withAlpha(ownedHovered ? -266722777 : -435153640, alpha);
        graphics.method_25294(layout.ownedX, searchBarY, layout.ownedX + 108, searchBarY + searchH, ownedBg);
        String ownedText = this.ownedOnly ? "Owned ON" : "Owned";
        graphics.method_51433(this.field_22793, ownedText, layout.ownedX + (108 - this.field_22793.method_1727(ownedText)) / 2, searchBarY + 11, TyxenUI.withAlpha(this.ownedOnly ? -723724 : -7303024, alpha), false);
        boolean hasEquipped = !this.state.getEquippedItemIds().isEmpty();
        boolean removeHovered = mouseX >= layout.removeAllX && mouseX <= layout.removeAllX + 104 && mouseY >= searchBarY && mouseY <= searchBarY + searchH;
        int removeBg = TyxenUI.withAlpha(hasEquipped && removeHovered ? -14498466 : -435153640, alpha);
        graphics.method_25294(layout.removeAllX, searchBarY, layout.removeAllX + 104, searchBarY + searchH, removeBg);
        String removeText = "Remove All";
        graphics.method_51433(this.field_22793, removeText, layout.removeAllX + (104 - this.field_22793.method_1727(removeText)) / 2, searchBarY + 11, TyxenUI.withAlpha(hasEquipped && removeHovered ? -723724 : -7303024, alpha), false);
    }

    private void drawStatus(class_332 graphics, int alpha) {
        if (this.statusMessage.isEmpty() || System.currentTimeMillis() > this.statusMessageUntil) {
            return;
        }
        StoreControlsLayout layout = this.getStoreControlsLayout();
        int statusX = layout.searchX + layout.searchW + 12;
        int statusY = layout.searchY + 11;
        int maxW = layout.ownedX - statusX - 12;
        if (maxW <= 40) {
            return;
        }
        String message = this.fitText(this.statusMessage, maxW);
        graphics.method_51433(this.field_22793, message, statusX, statusY, TyxenUI.withAlpha(-7303024, alpha), false);
    }

    private int getHeaderNavY() {
        return this.panelY - 6 - 32;
    }

    private int getHeaderBrandY() {
        return this.getHeaderNavY() - 6 - 32;
    }

    private void drawSelectedCategoryLabel(class_332 graphics, CosmeticCategory category, int y, int alpha) {
        String label = category.getDisplayName().toUpperCase(Locale.ROOT);
        int count = category == CosmeticCategory.OWNED ? this.storeManager.getOwnedCosmetics().size() : this.storeManager.getCosmeticsByCategory(category).size();
        String countText = count + (count == 1 ? " item" : " items");
        if (category == CosmeticCategory.NAMETAGS) {
            int equippedTags = this.storeManager.getEquippedChatTags().size();
            countText = countText + "  /  " + equippedTags + "/3 active";
        }
        int x = this.panelX + 24;
        graphics.method_51433(this.field_22793, label, x, y, TyxenUI.withAlpha(-723724, alpha), true);
        graphics.method_51433(this.field_22793, countText, x + this.field_22793.method_1727(label) + 10, y, TyxenUI.withAlpha(-7303024, alpha), false);
    }

    private int getCategoryTabsY() {
        return this.panelY + 14;
    }

    private int getCategoryLabelY(int tabSize) {
        return this.getCategoryTabsY() + tabSize + 8;
    }

    private int getCategoryTabGap() {
        return this.panelWidth < 720 ? 4 : 6;
    }

    private int getCategoryTabSize() {
        CosmeticCategory[] categories = CosmeticCategory.values();
        int gap = this.getCategoryTabGap();
        int available = this.panelWidth - 48;
        int size = (available - Math.max(0, categories.length - 1) * gap) / Math.max(1, categories.length);
        return Math.max(34, Math.min(46, size));
    }

    private int getCategoryTabsStartX() {
        return this.panelX + 24;
    }

    private class_2960 categoryIcon(CosmeticCategory category, boolean hover) {
        String suffix = hover ? "_hover" : "";
        return class_2960.method_60655((String)"tyxen", (String)("textures/gui/store/categories/" + category.getIconName() + suffix + ".png"));
    }

    private int getSearchWidth(int availableWidth) {
        return Math.min(390, Math.max(160, availableWidth / 3));
    }

    private StoreControlsLayout getStoreControlsLayout() {
        int searchY = this.panelY + 126 - 30 - 12;
        int rightEdge = this.panelX + this.panelWidth - 24;
        int removeAllX = rightEdge - 104;
        int ownedX = removeAllX - 8 - 108;
        int searchX = this.panelX + 24;
        int maxSearchW = ownedX - searchX - 12;
        int searchW = Math.min(this.getSearchWidth(this.panelWidth), Math.max(120, maxSearchW));
        return new StoreControlsLayout(searchX, searchY, searchW, ownedX, removeAllX);
    }

    private void setStatus(String message) {
        this.statusMessage = message;
        this.statusMessageUntil = System.currentTimeMillis() + 2400L;
    }

    private List<StoreCosmetic> getFilteredItems() {
        return this.storeManager.searchCosmetics(this.searchQuery, this.selectedCategory, this.ownedOnly);
    }

    private void recomputeCardRects(List<StoreCosmetic> items) {
        this.cardRects.clear();
        this.visibleItems.clear();
        this.visibleItems.addAll(items);
        int columns = Math.max(1, (this.gridW() + 12) / 190);
        int rows = items.isEmpty() ? 0 : (int)Math.ceil((double)items.size() / (double)columns);
        int totalH = rows == 0 ? 0 : 20 + rows * 218 + (rows - 1) * 12;
        this.maxScroll = Math.max(0, totalH - this.gridH());
        int startX = this.gridX() + 10;
        int startY = this.gridY() + 10 - (int)this.scrollOffset;
        for (int i = 0; i < items.size(); ++i) {
            int col = i % columns;
            int row = i / columns;
            int cardX = startX + col * 190;
            int cardY = startY + row * 230;
            this.cardRects.add(new int[]{cardX, cardY, 178, 218});
        }
    }

    private void drawProductGrid(class_332 graphics, int mouseX, int mouseY, int alpha, float dt) {
        List<StoreCosmetic> items = this.getFilteredItems();
        this.clampScroll();
        this.scrollOffset = AnimationUtils.smoothDelta((float)this.scrollOffset, (float)this.targetScrollOffset, 0.3f, dt * 60.0f);
        this.scrollOffset = Math.max(0.0, Math.min(this.maxScroll, this.scrollOffset));
        this.state.setScrollOffset(this.targetScrollOffset);
        this.recomputeCardRects(items);
        DisplaySpace.enableScissor(graphics, this.gridX(), this.gridY(), this.gridX() + this.gridW(), this.gridY() + this.gridH());
        if (items.isEmpty()) {
            this.drawEmptyState(graphics, alpha);
        }
        for (int i = 0; i < items.size(); ++i) {
            int[] rect = this.cardRects.get(i);
            int cardX = rect[0];
            int cardY = rect[1];
            if (cardY + 218 <= this.gridY() || cardY >= this.gridY() + this.gridH()) continue;
            this.drawCard(graphics, items.get(i), cardX, cardY, 178, 218, mouseX, mouseY, alpha);
        }
        DisplaySpace.disableScissor(graphics);
        if (this.maxScroll > 0.0) {
            this.renderScrollbar(graphics);
        }
    }

    private void drawEmptyState(class_332 graphics, int alpha) {
        String title = this.selectedCategory == CosmeticCategory.OWNED || this.ownedOnly ? "No owned cosmetics here" : "No cosmetics found";
        String subtitle = "Try another category or search term.";
        int centerX = this.gridX() + this.gridW() / 2;
        int centerY = this.gridY() + this.gridH() / 2;
        graphics.method_51433(this.field_22793, title, centerX - this.field_22793.method_1727(title) / 2, centerY - 10, TyxenUI.withAlpha(-723724, alpha), true);
        graphics.method_51433(this.field_22793, subtitle, centerX - this.field_22793.method_1727(subtitle) / 2, centerY + 6, TyxenUI.withAlpha(-7303024, alpha), false);
    }

    private void renderScrollbar(class_332 graphics) {
        int scrollbarX = this.gridX() + this.gridW() + 2;
        int scrollbarTrackY = this.gridY() + 6;
        int scrollbarTrackH = Math.max(1, this.gridH() - 12);
        graphics.method_25294(scrollbarX, scrollbarTrackY, scrollbarX + 5, scrollbarTrackY + scrollbarTrackH, 1880760099);
        double visibleRatio = (double)this.gridH() / ((double)this.gridH() + this.maxScroll);
        int thumbHeight = Math.min(scrollbarTrackH, Math.max(24, (int)((double)scrollbarTrackH * visibleRatio)));
        int thumbY = scrollbarTrackY + (int)(this.scrollOffset / this.maxScroll * (double)(scrollbarTrackH - thumbHeight));
        int thumbColor = this.scrollbarDragging ? -14498466 : -1722656160;
        graphics.method_25294(scrollbarX, thumbY, scrollbarX + 5, thumbY + thumbHeight, thumbColor);
    }

    private void drawCard(class_332 graphics, StoreCosmetic cosmetic, int x, int y, int w, int h, int mouseX, int mouseY, int alpha) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        int cardBg = TyxenUI.withAlpha(hovered ? -434955745 : -653586413, alpha);
        graphics.method_25294(x, y, x + w, y + h, cardBg);
        TyxenUI.outline(graphics, x, y, w, h, TyxenUI.withAlpha(hovered ? 1717331565 : 1143616571, alpha));
        if (hovered) {
            graphics.method_25294(x + 1, y + h - 3, x + w - 1, y + h - 1, TyxenUI.withAlpha(-11870592, alpha));
        }
        int rarityColor = TyxenUI.withAlpha(cosmetic.getRarity().getColor(), alpha);
        graphics.method_25294(x + 1, y + 1, x + w - 1, y + 4, rarityColor);
        int previewSize = Math.min(w - 34, h - 92);
        int previewX = x + (w - previewSize) / 2;
        int previewY = y + 15;
        int framePad = 6;
        int frameColor = TyxenUI.blend(-435153640, cosmetic.getRarity().getColor(), hovered ? 0.18f : 0.08f);
        graphics.method_25294(previewX - framePad, previewY - framePad, previewX + previewSize + framePad, previewY + previewSize + framePad, TyxenUI.withAlpha(frameColor, alpha));
        TyxenUI.outline(graphics, previewX - framePad, previewY - framePad, previewSize + framePad * 2, previewSize + framePad * 2, TyxenUI.withAlpha(hovered ? cosmetic.getRarity().getColor() : 1143616571, alpha));
        String assetName = cosmetic.getPreviewAssetName();
        if (assetName != null && !assetName.isEmpty()) {
            class_2960 previewId = DisplaySpace.texture(class_2960.method_60655((String)"tyxen", (String)("textures/gui/store/items/" + assetName + ".png")));
            if (this.hasTexture(previewId)) {
                graphics.method_25291(class_10799.field_56883, previewId, previewX, previewY, 0.0f, 0.0f, previewSize, previewSize, previewSize, previewSize, TyxenUI.withAlpha(-1, alpha));
            } else {
                this.drawPlaceholderPreview(graphics, cosmetic, previewX, previewY, previewSize, alpha);
            }
        } else {
            this.drawPlaceholderPreview(graphics, cosmetic, previewX, previewY, previewSize, alpha);
        }
        int nameY = y + h - 64;
        String displayName = this.fitText(cosmetic.getDisplayName(), w - 20);
        graphics.method_51433(this.field_22793, displayName, x + (w - this.field_22793.method_1727(displayName)) / 2, nameY, TyxenUI.withAlpha(-723724, alpha), true);
        int rarityY = nameY + 14;
        String rarityText = cosmetic.getRarity().getDisplayName();
        graphics.method_51433(this.field_22793, rarityText, x + (w - this.field_22793.method_1727(rarityText)) / 2, rarityY, TyxenUI.withAlpha(cosmetic.getRarity().getColor(), alpha), false);
        int btnY = y + h - 30;
        int btnH = 22;
        int btnW = w - 20;
        int btnX = x + 10;
        boolean owned = this.state.owns(cosmetic.getId());
        boolean equipped = this.state.isEquipped(cosmetic.getId());
        if (owned && equipped) {
            int btnGap = 6;
            int equippedBtnW = (btnW - btnGap) / 2;
            int removeBtnW = btnW - equippedBtnW - btnGap;
            int removeBtnX = btnX + equippedBtnW + btnGap;
            int eqBg = TyxenUI.withAlpha(-15511009, alpha);
            graphics.method_25294(btnX, btnY, btnX + equippedBtnW, btnY + btnH, eqBg);
            String eqText = "Equipped";
            String fittedEqText = this.fitText(eqText, equippedBtnW - 6);
            graphics.method_51433(this.field_22793, fittedEqText, btnX + (equippedBtnW - this.field_22793.method_1727(fittedEqText)) / 2, btnY + 5, TyxenUI.withAlpha(-723724, alpha), true);
            boolean removeHovered = mouseX >= removeBtnX && mouseX <= removeBtnX + removeBtnW && mouseY >= btnY && mouseY <= btnY + btnH;
            int removeBg = TyxenUI.withAlpha(removeHovered ? -14498466 : -435153640, alpha);
            graphics.method_25294(removeBtnX, btnY, removeBtnX + removeBtnW, btnY + btnH, removeBg);
            String removeText = "Remove";
            String fittedRemoveText = this.fitText(removeText, removeBtnW - 6);
            graphics.method_51433(this.field_22793, fittedRemoveText, removeBtnX + (removeBtnW - this.field_22793.method_1727(fittedRemoveText)) / 2, btnY + 5, TyxenUI.withAlpha(removeHovered ? -723724 : -7303024, alpha), false);
        } else if (owned) {
            int eqBg = TyxenUI.withAlpha(hovered ? -15243738 : -435153640, alpha);
            graphics.method_25294(btnX, btnY, btnX + btnW, btnY + btnH, eqBg);
            String eqText = "Equip";
            graphics.method_51433(this.field_22793, eqText, btnX + (btnW - this.field_22793.method_1727(eqText)) / 2, btnY + 5, TyxenUI.withAlpha(hovered ? -723724 : -7303024, alpha), true);
        } else {
            boolean canAfford;
            boolean bl = canAfford = this.state.getCoinBalance() >= cosmetic.getPrice();
            int buyBg = canAfford ? TyxenUI.withAlpha(hovered ? -11870592 : -14498466, alpha) : TyxenUI.withAlpha(-435153640, alpha);
            graphics.method_25294(btnX, btnY, btnX + btnW, btnY + btnH, buyBg);
            Object buyText = cosmetic.getPrice() <= 0 ? "Free" : (canAfford ? "Buy " + cosmetic.getPrice() : "Need " + (cosmetic.getPrice() - this.state.getCoinBalance()));
            this.drawPriceLabel(graphics, (String)buyText, cosmetic.getPrice() > 0, btnX, btnY, btnW, TyxenUI.withAlpha(canAfford ? -723724 : -9934744, alpha), alpha);
        }
    }

    private void drawPriceLabel(class_332 graphics, String text, boolean showDiamond, int x, int y, int w, int textColor, int alpha) {
        if (!showDiamond) {
            graphics.method_51433(this.field_22793, text, x + (w - this.field_22793.method_1727(text)) / 2, y + 6, textColor, true);
            return;
        }
        int iconSize = 11;
        int gap = 4;
        int totalW = iconSize + gap + this.field_22793.method_1727(text);
        int startX = x + (w - totalW) / 2;
        graphics.method_25291(class_10799.field_56883, DisplaySpace.texture(DIAMOND_TEXTURE), startX, y + 5, 0.0f, 0.0f, iconSize, iconSize, iconSize, iconSize, TyxenUI.withAlpha(-13312, alpha));
        graphics.method_51433(this.field_22793, text, startX + iconSize + gap, y + 6, textColor, true);
    }

    private boolean hasTexture(class_2960 id) {
        return class_310.method_1551().method_1478().method_14486(id).isPresent();
    }

    private void drawPlaceholderPreview(class_332 graphics, StoreCosmetic cosmetic, int x, int y, int size, int alpha) {
        int bg = TyxenUI.blend(-435153640, cosmetic.getRarity().getColor(), 0.22f);
        graphics.method_25294(x, y, x + size, y + size, TyxenUI.withAlpha(bg, alpha));
        TyxenUI.outline(graphics, x, y, size, size, TyxenUI.withAlpha(cosmetic.getRarity().getColor(), alpha));
        int accent = TyxenUI.withAlpha(cosmetic.getRarity().getColor(), Math.min(alpha, 150));
        graphics.method_25294(x + 8, y + 8, x + size - 8, y + 10, accent);
        graphics.method_25294(x + 8, y + size - 10, x + size - 8, y + size - 8, accent);
        graphics.method_25294(x + size / 2 - 1, y + 16, x + size / 2 + 1, y + size - 16, accent);
        String glyph = this.categoryGlyph(cosmetic);
        float scale = size >= 92 ? 2.2f : 1.7f;
        this.drawScaledCenteredText(graphics, glyph, x + size / 2, y + size / 2, scale, TyxenUI.withAlpha(-723724, alpha), true);
    }

    private String categoryGlyph(StoreCosmetic cosmetic) {
        return switch (cosmetic.getSlot()) {
            case "cloak" -> "CL";
            case "elytra" -> "EL";
            case "hat" -> "HT";
            case "back" -> "BK";
            case "aura" -> "AU";
            case "nametag" -> "NT";
            case "arm" -> "AR";
            case "leg" -> "BT";
            case "pet" -> "PT";
            default -> "TX";
        };
    }

    private void drawScaledCenteredText(class_332 graphics, String text, int centerX, int centerY, float scale, int color, boolean shadow) {
        float textX = (float)centerX - (float)this.field_22793.method_1727(text) * scale / 2.0f;
        float f = centerY;
        Objects.requireNonNull(this.field_22793);
        float textY = f - 9.0f * scale / 2.0f;
        graphics.method_51448().pushMatrix();
        graphics.method_51448().translate(textX, textY);
        graphics.method_51448().scale(scale, scale);
        graphics.method_51448().translate(-textX, -textY);
        graphics.method_51433(this.field_22793, text, (int)textX, (int)textY, color, shadow);
        graphics.method_51448().popMatrix();
    }

    private String fitText(String text, int maxWidth) {
        if (this.field_22793.method_1727(text) <= maxWidth) {
            return text;
        }
        String result = text;
        while (result.length() > 1 && this.field_22793.method_1727(result + "...") > maxWidth) {
            result = result.substring(0, result.length() - 1);
        }
        return result + "...";
    }

    private void clampScroll() {
        this.targetScrollOffset = Math.max(0.0, Math.min(this.maxScroll, this.targetScrollOffset));
        this.scrollOffset = Math.max(0.0, Math.min(this.maxScroll, this.scrollOffset));
    }

    private int findCardAt(int mouseX, int mouseY) {
        for (int i = 0; i < this.cardRects.size(); ++i) {
            int[] r = this.cardRects.get(i);
            if (mouseX < r[0] || mouseX > r[0] + r[2] || mouseY < r[1] || mouseY > r[1] + r[3]) continue;
            return i;
        }
        return -1;
    }

    public boolean method_25402(class_11909 event, boolean bl) {
        int cardIdx;
        double mouseX = DisplaySpace.mouseX(event.comp_4798());
        double mouseY = DisplaySpace.mouseY(event.comp_4799());
        int button = event.method_74245();
        if (button != 0) {
            return super.method_25402(event, bl);
        }
        int headerH = 32;
        int headerY = this.getHeaderNavY();
        int storeBtnW = this.panelWidth < 1050 ? 152 : 168;
        int claimX = this.panelX + storeBtnW + 8 + 112 + 8;
        int closeX = this.panelX + this.panelWidth - 30;
        int closeY = headerY + (headerH - 30) / 2;
        if (mouseX >= (double)closeX && mouseX <= (double)(closeX + 30) && mouseY >= (double)closeY && mouseY <= (double)(closeY + 30)) {
            this.method_25419();
            return true;
        }
          if (mouseX >= (double)claimX && mouseX <= (double)(claimX + 118) && mouseY >= (double)headerY && mouseY <= (double)(headerY + headerH)) {
              if (this.storeManager.claimDailyCoins()) {
                  int amount = this.storeManager.getDailyCoinClaimAmount();
                  this.setStatus("Claimed " + amount + " coins");
                  int coinX = this.panelX + storeBtnW + 8;
                  this.animFrom = this.shownBalance < 0 ? this.state.getCoinBalance() : this.shownBalance;
                  this.animTo = this.animFrom + amount;
                  this.animT0 = System.currentTimeMillis() + 250L;
                  this.coinFly.clear();
                  for (int ci = 0; ci < 6; ++ci) {
                      this.coinFly.add(new float[]{(float)(System.currentTimeMillis() + ci * 90L), 750.0f, (float)(claimX + 59), (float)(headerY + 16), (float)(coinX + 20), (float)(headerY + 16)});
                  }
            } else {
                this.setStatus("Daily coins already claimed");
            }
            this.searchFocused = false;
            return true;
        }
        CosmeticCategory[] categories = CosmeticCategory.values();
        int tabSize = this.getCategoryTabSize();
        int tabGap = this.getCategoryTabGap();
        int tabY = this.getCategoryTabsY();
        int tabX = this.getCategoryTabsStartX();
        for (int i = 0; i < categories.length; ++i) {
            CosmeticCategory cat = categories[i];
            if (mouseX >= (double)tabX && mouseX <= (double)(tabX + tabSize) && mouseY >= (double)tabY && mouseY <= (double)(tabY + tabSize)) {
                this.selectedCategory = cat;
                this.state.setLastSelectedCategory(cat.name());
                this.targetScrollOffset = 0.0;
                this.scrollOffset = 0.0;
                this.searchFocused = false;
                this.storeManager.saveState();
                return true;
            }
            tabX += tabSize + tabGap;
        }
        StoreControlsLayout controls = this.getStoreControlsLayout();
        if (mouseX >= (double)controls.searchX && mouseX <= (double)(controls.searchX + controls.searchW) && mouseY >= (double)controls.searchY && mouseY <= (double)(controls.searchY + 30)) {
            this.searchFocused = true;
            this.cursorBlinkTime = System.currentTimeMillis();
            return true;
        }
        if (!this.isWithinGrid(mouseX, mouseY)) {
            this.searchFocused = false;
        }
        if (mouseX >= (double)controls.ownedX && mouseX <= (double)(controls.ownedX + 108) && mouseY >= (double)controls.searchY && mouseY <= (double)(controls.searchY + 30)) {
            this.ownedOnly = !this.ownedOnly;
            this.targetScrollOffset = 0.0;
            this.scrollOffset = 0.0;
            this.searchFocused = false;
            return true;
        }
        if (mouseX >= (double)controls.removeAllX && mouseX <= (double)(controls.removeAllX + 104) && mouseY >= (double)controls.searchY && mouseY <= (double)(controls.searchY + 30)) {
            if (this.state.getEquippedItemIds().isEmpty()) {
                this.setStatus("No equipped cosmetics to remove");
            } else {
                this.storeManager.removeAll();
                this.setStatus("Removed all equipped cosmetics");
            }
            this.searchFocused = false;
            return true;
        }
        if (this.maxScroll > 0.0 && mouseX >= (double)(this.gridX() + this.gridW()) && mouseX <= (double)(this.gridX() + this.gridW() + 5 + 8) && mouseY >= (double)this.gridY() && mouseY <= (double)(this.gridY() + this.gridH())) {
            this.scrollbarDragging = true;
            this.dragStartY = mouseY;
            this.dragStartScroll = this.targetScrollOffset;
            this.searchFocused = false;
            return true;
        }
        int n = cardIdx = this.isWithinGrid(mouseX, mouseY) ? this.findCardAt((int)mouseX, (int)mouseY) : -1;
        if (cardIdx >= 0 && cardIdx < this.visibleItems.size()) {
            StoreCosmetic cosmetic = this.visibleItems.get(cardIdx);
            int[] rect = this.cardRects.get(cardIdx);
            int cardX = rect[0];
            int cardY = rect[1];
            int cardW = rect[2];
            int cardH = rect[3];
            int btnY = cardY + cardH - 30;
            int btnH = 22;
            int btnW = cardW - 20;
            int btnX = cardX + 10;
            boolean owned = this.state.owns(cosmetic.getId());
            boolean equipped = this.state.isEquipped(cosmetic.getId());
            if (cardY + cardH <= this.gridY() || cardY >= this.gridY() + this.gridH()) {
                return true;
            }
            if (owned && equipped) {
                int btnGap = 6;
                int equippedBtnW = (btnW - btnGap) / 2;
                int removeBtnW = btnW - equippedBtnW - btnGap;
                int removeBtnX = btnX + equippedBtnW + btnGap;
                if (mouseX >= (double)removeBtnX && mouseX <= (double)(removeBtnX + removeBtnW) && mouseY >= (double)btnY && mouseY <= (double)(btnY + btnH)) {
                    this.storeManager.unequip(cosmetic.getId());
                    this.setStatus("Removed " + cosmetic.getDisplayName());
                    this.searchFocused = false;
                    return true;
                }
                if (mouseX >= (double)btnX && mouseX <= (double)(btnX + equippedBtnW) && mouseY >= (double)btnY && mouseY <= (double)(btnY + btnH)) {
                    this.searchFocused = false;
                    return true;
                }
            } else if (owned) {
                if (mouseX >= (double)btnX && mouseX <= (double)(btnX + btnW) && mouseY >= (double)btnY && mouseY <= (double)(btnY + btnH)) {
                    this.storeManager.equip(cosmetic.getId());
                    this.setStatus("Equipped " + cosmetic.getDisplayName());
                    this.searchFocused = false;
                    return true;
                }
            } else if (mouseX >= (double)btnX && mouseX <= (double)(btnX + btnW) && mouseY >= (double)btnY && mouseY <= (double)(btnY + btnH)) {
                if (this.state.getCoinBalance() >= cosmetic.getPrice()) {
                    this.storeManager.purchase(cosmetic.getId());
                    this.storeManager.equip(cosmetic.getId());
                    this.setStatus("Purchased and equipped " + cosmetic.getDisplayName());
                } else {
                    this.setStatus("Not enough coins for " + cosmetic.getDisplayName());
                }
                this.searchFocused = false;
                return true;
            }
            this.searchFocused = false;
            return true;
        }
        return super.method_25402(event, bl);
    }

    private boolean isWithinGrid(double mouseX, double mouseY) {
        return mouseX >= (double)this.gridX() && mouseX <= (double)(this.gridX() + this.gridW()) && mouseY >= (double)this.gridY() && mouseY <= (double)(this.gridY() + this.gridH());
    }

    public boolean method_25406(class_11909 event) {
        this.scrollbarDragging = false;
        return super.method_25406(event);
    }

    public boolean method_25403(class_11909 event, double deltaX, double deltaY) {
        if (this.scrollbarDragging && this.maxScroll > 0.0) {
            int scrollbarTrackH = Math.max(1, this.gridH() - 12);
            double mouseY = DisplaySpace.mouseY(event.comp_4799());
            double scrollRatio = (mouseY - this.dragStartY) / (double)scrollbarTrackH;
            this.targetScrollOffset = Math.max(0.0, Math.min(this.maxScroll, this.dragStartScroll + scrollRatio * this.maxScroll));
            return true;
        }
        return super.method_25403(event, deltaX, deltaY);
    }

    public boolean method_25401(double mouseX, double mouseY, double horizAmount, double vertAmount) {
        int pxMouseX = DisplaySpace.mouseX(mouseX);
        int pxMouseY = DisplaySpace.mouseY(mouseY);
        if (pxMouseX >= this.gridX() && pxMouseX <= this.gridX() + this.gridW() && pxMouseY >= this.gridY() && pxMouseY <= this.gridY() + this.gridH()) {
            this.targetScrollOffset = Math.max(0.0, Math.min(this.maxScroll, this.targetScrollOffset - vertAmount * 34.0));
            return true;
        }
        return super.method_25401(mouseX, mouseY, horizAmount, vertAmount);
    }

    public boolean method_25404(class_11908 event) {
        int keyCode = event.comp_4795();
        if (keyCode == 256) {
            if (this.searchFocused && !this.searchQuery.isEmpty()) {
                this.searchQuery = "";
                return true;
            }
            if (!this.searchFocused) {
                this.method_25419();
                return true;
            }
            this.searchFocused = false;
            return true;
        }
        if (this.searchFocused) {
            if (keyCode == 259) {
                if (!this.searchQuery.isEmpty()) {
                    this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
                }
                return true;
            }
            if (keyCode == 257) {
                this.searchFocused = false;
                return true;
            }
            return true;
        }
        return super.method_25404(event);
    }

    public boolean method_25400(class_11905 event) {
        char c;
        if (this.searchFocused && (c = (char)event.comp_4793()) >= ' ') {
            this.searchQuery = this.searchQuery + c;
            this.cursorBlinkTime = System.currentTimeMillis();
            return true;
        }
        return super.method_25400(event);
    }

    public boolean method_25421() {
        return false;
    }

    public void method_25419() {
        this.state.setLastSelectedCategory(this.selectedCategory.name());
        this.state.setScrollOffset(this.targetScrollOffset);
        this.storeManager.saveState();
        if (this.previousScreen != null) {
            class_310.method_1551().method_1507(this.previousScreen);
        } else {
            class_310.method_1551().method_1507(null);
        }
    }

    @Environment(value=EnvType.CLIENT)
    private static final class StoreControlsLayout {
        final int searchX;
        final int searchY;
        final int searchW;
        final int ownedX;
        final int removeAllX;

        StoreControlsLayout(int searchX, int searchY, int searchW, int ownedX, int removeAllX) {
            this.searchX = searchX;
            this.searchY = searchY;
            this.searchW = searchW;
            this.ownedX = ownedX;
            this.removeAllX = removeAllX;
        }
    }
}

