/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_332
 */
package net.fastclient.hud.modules.impl.hud;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fastclient.hud.gui.FastClientUI;
import net.fastclient.hud.modules.Category;
import net.fastclient.hud.modules.Module;
import net.fastclient.hud.modules.settings.BooleanSetting;
import net.fastclient.hud.modules.settings.NumberSetting;
import net.fastclient.hud.utils.PlayerUtils;
import net.minecraft.class_332;

@Environment(value=EnvType.CLIENT)
public class DirectionHUD
extends Module {
    private final BooleanSetting background = this.register(new BooleanSetting("background", "Show background", true));
    private final NumberSetting bgOpacity = this.register(new NumberSetting("opacity", "Background opacity", 80.0, 0.0, 255.0, 5.0));

    public DirectionHUD() {
        super("DirectionHUD", "Shows facing direction and yaw", Category.HUD);
        this.bgOpacity.visibleWhen(this.background::isEnabled);
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
        String direction = PlayerUtils.getFacingDirection();
        float yaw = DirectionHUD.mc.field_1724.method_36454();
        String yawText = String.format("%.1f\u00b0", Float.valueOf((yaw % 360.0f + 360.0f) % 360.0f));
        if (this.background.isEnabled()) {
            FastClientUI.hudTwoLine(graphics, DirectionHUD.mc.field_1772, direction, yawText, x, y, -1, -3025448, this.bgOpacity.getIntValue());
        } else {
            graphics.method_51433(DirectionHUD.mc.field_1772, direction, x, y, -1, true);
            graphics.method_51433(DirectionHUD.mc.field_1772, yawText, x, y + 10, -3025448, true);
        }
        graphics.method_51448().popMatrix();
    }

    @Override
    public int getHudWidth() {
        if (DirectionHUD.mc.field_1724 == null) {
            return 60;
        }
        String direction = PlayerUtils.getFacingDirection();
        float yaw = DirectionHUD.mc.field_1724.method_36454();
        String yawText = String.format("%.1f\u00b0", Float.valueOf((yaw % 360.0f + 360.0f) % 360.0f));
        return Math.max(DirectionHUD.mc.field_1772.method_1727(direction), DirectionHUD.mc.field_1772.method_1727(yawText)) + 10;
    }

    @Override
    public int getHudHeight() {
        return 28;
    }
}

