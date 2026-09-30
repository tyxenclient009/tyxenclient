/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_3288
 *  net.minecraft.class_332
 */
package net.tyxen.hud.modules.impl.hud;

import java.util.Collection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.gui.TyxenUI;
import net.tyxen.hud.modules.Category;
import net.tyxen.hud.modules.Module;
import net.minecraft.class_3288;
import net.minecraft.class_332;

@Environment(value=EnvType.CLIENT)
public class PackDisplay
extends Module {
    public PackDisplay() {
        super("PackDisplay", "Shows active resource packs", Category.HUD);
    }

    private static boolean isBuiltinPack(class_3288 pack) {
        String name;
        try {
            name = pack.method_14457().getString();
        }
        catch (Throwable t) {
            return true;
        }
        if (name.equals("Default") || name.startsWith("Fabric")) {
            return true;
        }
        try {
            String id = pack.method_14463();
            return id != null && (id.equals("vanilla") || id.equals("fabric"));
        }
        catch (Throwable t) {
            return false;
        }
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
        Collection<class_3288> packs = mc.method_1520().method_14444();
        int maxWidth = 0;
        int count = 0;
        for (class_3288 pack : packs) {
            if (PackDisplay.isBuiltinPack(pack)) continue;
            String name = pack.method_14457().getString();
            maxWidth = Math.max(maxWidth, PackDisplay.mc.field_1772.method_1727(name));
            ++count;
        }
        if (count == 0) {
            graphics.method_51448().popMatrix();
            return;
        }
        TyxenUI.hudPanel(graphics, x - 5, y - 4, maxWidth + 10, count * 12 + 6);
        int offsetY = 0;
        for (class_3288 pack : packs) {
            if (PackDisplay.isBuiltinPack(pack)) continue;
            String name = pack.method_14457().getString();
            graphics.method_51433(PackDisplay.mc.field_1772, name, x, y + offsetY, -1, true);
            offsetY += 12;
        }
        graphics.method_51448().popMatrix();
    }

    @Override
    public int getHudWidth() {
        if (mc.method_1520() == null) {
            return 0;
        }
        int maxWidth = 0;
        int count = 0;
        Collection<class_3288> packs = mc.method_1520().method_14444();
        for (class_3288 pack : packs) {
            int width;
            if (PackDisplay.isBuiltinPack(pack)) continue;
            String name = pack.method_14457().getString();
            if ((width = PackDisplay.mc.field_1772.method_1727(name)) <= maxWidth) continue;
            maxWidth = width;
            ++count;
        }
        return count == 0 ? 0 : maxWidth + 10;
    }

    @Override
    public int getHudHeight() {
        if (mc.method_1520() == null) {
            return 0;
        }
        int count = 0;
        Collection<class_3288> packs = mc.method_1520().method_14444();
        for (class_3288 pack : packs) {
            if (PackDisplay.isBuiltinPack(pack)) continue;
            ++count;
        }
        return count == 0 ? 0 : count * 12 + 6;
    }
}

