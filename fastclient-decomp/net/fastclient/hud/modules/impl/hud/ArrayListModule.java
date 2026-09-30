/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_332
 */
package net.fastclient.hud.modules.impl.hud;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fastclient.hud.core.ModuleManager;
import net.fastclient.hud.gui.FastClientUI;
import net.fastclient.hud.modules.Category;
import net.fastclient.hud.modules.Module;
import net.fastclient.hud.modules.settings.BooleanSetting;
import net.fastclient.hud.modules.settings.ColorSetting;
import net.fastclient.hud.modules.settings.ModeSetting;
import net.minecraft.class_332;

@Environment(value=EnvType.CLIENT)
public class ArrayListModule
extends Module {
    private final ModeSetting sortMode = this.register(new ModeSetting("sort_mode", "Sort order", "length", new String[]{"length", "alphabetical"}));
    private final ColorSetting textColor = this.register(new ColorSetting("text_color", "Module text color", 255, 255, 255));
    private final BooleanSetting background = this.register(new BooleanSetting("background", "Show background", true));

    public ArrayListModule() {
        super("ArrayList", "List of active modules", Category.HUD);
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
        List enabledModules = ModuleManager.getInstance().getEnabledModules().stream().filter(m -> m != this).collect(Collectors.toList());
        if (this.sortMode.is("length")) {
            enabledModules.sort((a, b) -> ArrayListModule.mc.field_1772.method_1727(b.getDisplayName()) - ArrayListModule.mc.field_1772.method_1727(a.getDisplayName()));
        } else {
            enabledModules.sort(Comparator.comparing(Module::getDisplayName));
        }
        int offsetY = 0;
        int color = this.textColor.getRGB() | 0xFF000000;
        for (Module module : enabledModules) {
            String name = module.getDisplayName();
            int textWidth = ArrayListModule.mc.field_1772.method_1727(name);
            if (((Boolean)this.background.getValue()).booleanValue()) {
                FastClientUI.hudPanel(graphics, x - 5, y + offsetY - 3, textWidth + 10, 15);
            }
            graphics.method_51433(ArrayListModule.mc.field_1772, name, x, y + offsetY, color, true);
            offsetY += 13;
        }
        graphics.method_51448().popMatrix();
    }

    @Override
    public int getHudWidth() {
        List enabledModules = ModuleManager.getInstance().getEnabledModules().stream().filter(m -> m != this).collect(Collectors.toList());
        int maxWidth = 0;
        for (Module module : enabledModules) {
            int width = ArrayListModule.mc.field_1772.method_1727(module.getDisplayName());
            if (width <= maxWidth) continue;
            maxWidth = width;
        }
        return maxWidth + 10;
    }

    @Override
    public int getHudHeight() {
        List enabledModules = ModuleManager.getInstance().getEnabledModules().stream().filter(m -> m != this).collect(Collectors.toList());
        return enabledModules.size() * 13 + 2;
    }
}

