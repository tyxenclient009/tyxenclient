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
import net.fastclient.hud.utils.PlayerUtils;
import net.minecraft.class_332;

@Environment(value=EnvType.CLIENT)
public class SpeedHUD
extends Module {
    private final BooleanSetting background = this.register(new BooleanSetting("background", "Show background", true));

    public SpeedHUD() {
        super("SpeedHUD", "Shows player movement speed", Category.HUD);
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
        double speed = PlayerUtils.getSpeed();
        String text = String.format("Speed: %.2f m/s", speed);
        if (this.background.isEnabled()) {
            FastClientUI.hudText(graphics, SpeedHUD.mc.field_1772, text, x, y, -1, true);
        } else {
            graphics.method_51433(SpeedHUD.mc.field_1772, text, x, y, -1, true);
        }
        graphics.method_51448().popMatrix();
    }

    @Override
    public int getHudWidth() {
        double speed = PlayerUtils.getSpeed();
        String text = String.format("Speed: %.2f m/s", speed);
        return SpeedHUD.mc.field_1772.method_1727(text) + 10;
    }

    @Override
    public int getHudHeight() {
        return 17;
    }
}

