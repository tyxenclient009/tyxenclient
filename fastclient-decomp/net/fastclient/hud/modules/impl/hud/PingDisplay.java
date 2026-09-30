/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_332
 *  net.minecraft.class_640
 */
package net.fastclient.hud.modules.impl.hud;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fastclient.hud.gui.FastClientUI;
import net.fastclient.hud.modules.Category;
import net.fastclient.hud.modules.Module;
import net.fastclient.hud.modules.settings.BooleanSetting;
import net.fastclient.hud.modules.settings.NumberSetting;
import net.minecraft.class_332;
import net.minecraft.class_640;

@Environment(value=EnvType.CLIENT)
public class PingDisplay
extends Module {
    private final BooleanSetting background = this.register(new BooleanSetting("background", "Show background", true));
    private final NumberSetting bgOpacity = this.register(new NumberSetting("opacity", "Background opacity", 80.0, 0.0, 255.0, 5.0));

    public PingDisplay() {
        super("PingDisplay", "Shows network latency", Category.HUD);
        this.bgOpacity.visibleWhen(this.background::isEnabled);
    }

    @Override
    public void onRender(class_332 graphics, float tickDelta) {
        int color;
        class_640 entry;
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
        int ping = 0;
        if (mc.method_1562() != null && (entry = mc.method_1562().method_2871(PingDisplay.mc.field_1724.method_5667())) != null) {
            ping = entry.method_2959();
        }
        String text = "Ping: " + ping + "ms";
        int n = ping < 50 ? -11141291 : (color = ping < 100 ? -171 : -43691);
        if (this.background.isEnabled()) {
            FastClientUI.hudText(graphics, PingDisplay.mc.field_1772, text, x, y, color, true, this.bgOpacity.getIntValue());
        } else {
            graphics.method_51433(PingDisplay.mc.field_1772, text, x, y, color, true);
        }
        graphics.method_51448().popMatrix();
    }

    @Override
    public int getHudWidth() {
        class_640 entry;
        int ping = 0;
        if (mc.method_1562() != null && PingDisplay.mc.field_1724 != null && (entry = mc.method_1562().method_2871(PingDisplay.mc.field_1724.method_5667())) != null) {
            ping = entry.method_2959();
        }
        String text = "Ping: " + ping + "ms";
        return PingDisplay.mc.field_1772.method_1727(text) + 10;
    }

    @Override
    public int getHudHeight() {
        return 17;
    }
}

