/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_3288
 *  net.minecraft.class_332
 */
package net.fastclient.hud.modules.impl.hud;

import java.util.Collection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fastclient.hud.gui.FastClientUI;
import net.fastclient.hud.modules.Category;
import net.fastclient.hud.modules.Module;
import net.minecraft.class_3288;
import net.minecraft.class_332;

@Environment(value=EnvType.CLIENT)
public class PackDisplay
extends Module {
    public PackDisplay() {
        super("PackDisplay", "Shows active resource packs", Category.HUD);
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
        Collection packs = mc.method_1520().method_14444();
        int maxWidth = 0;
        int count = 0;
        for (class_3288 pack : packs) {
            String name = pack.method_14457().getString();
            if (name.equals("Default") || name.equals("Fabric Mods")) continue;
            maxWidth = Math.max(maxWidth, PackDisplay.mc.field_1772.method_1727(name));
            ++count;
        }
        if (count == 0) {
            graphics.method_51448().popMatrix();
            return;
        }
        FastClientUI.hudPanel(graphics, x - 5, y - 4, maxWidth + 10, count * 12 + 6);
        int offsetY = 0;
        for (class_3288 pack : packs) {
            String name = pack.method_14457().getString();
            if (name.equals("Default") || name.equals("Fabric Mods")) continue;
            graphics.method_51433(PackDisplay.mc.field_1772, name, x, y + offsetY, -1, true);
            offsetY += 12;
        }
        graphics.method_51448().popMatrix();
    }

    @Override
    public int getHudWidth() {
        int maxWidth = 0;
        Collection packs = mc.method_1520().method_14444();
        for (class_3288 pack : packs) {
            int width;
            String name = pack.method_14457().getString();
            if (name.equals("Default") || name.equals("Fabric Mods") || (width = PackDisplay.mc.field_1772.method_1727(name)) <= maxWidth) continue;
            maxWidth = width;
        }
        return maxWidth + 10;
    }

    @Override
    public int getHudHeight() {
        int count = 0;
        Collection packs = mc.method_1520().method_14444();
        for (class_3288 pack : packs) {
            String name = pack.method_14457().getString();
            if (name.equals("Default") || name.equals("Fabric Mods")) continue;
            ++count;
        }
        return count * 12 + 6;
    }
}

