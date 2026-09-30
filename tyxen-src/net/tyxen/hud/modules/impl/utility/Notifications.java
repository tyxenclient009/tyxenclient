/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_332
 */
package net.tyxen.hud.modules.impl.utility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.gui.DisplaySpace;
import net.tyxen.hud.modules.Category;
import net.tyxen.hud.modules.Module;
import net.tyxen.hud.modules.settings.BooleanSetting;
import net.tyxen.hud.modules.settings.NumberSetting;
import net.minecraft.class_332;

@Environment(value=EnvType.CLIENT)
public class Notifications
extends Module {
    private final BooleanSetting moduleToggle = this.register(new BooleanSetting("module_toggle", "Show notification when modules are toggled", true));
    private final BooleanSetting showEnabled = this.register(new BooleanSetting("show_enabled", "Show when modules are enabled", true));
    private final BooleanSetting showDisabled = this.register(new BooleanSetting("show_disabled", "Show when modules are disabled", true));
    private final NumberSetting duration = this.register(new NumberSetting("duration", "How long notifications stay on screen", 3.0, 1.0, 10.0, 0.5));
    private static final List<Notification> notifications = new ArrayList<Notification>();
    private static final int PRIMARY = -15066578;
    private static final int ACCENT = -16733441;

    public Notifications() {
        super("Notifications", "Shows popup notifications for module toggles", Category.UTILITY);
    }

    public static void addNotification(String title, String message, NotificationType type) {
        notifications.add(new Notification(title, message, type, System.currentTimeMillis()));
    }

    public void onModuleToggle(String moduleName, boolean enabled) {
        if (!this.isEnabled() || !((Boolean)this.moduleToggle.getValue()).booleanValue()) {
            return;
        }
        if (enabled && !((Boolean)this.showEnabled.getValue()).booleanValue()) {
            return;
        }
        if (!enabled && !((Boolean)this.showDisabled.getValue()).booleanValue()) {
            return;
        }
        String title = enabled ? "Module Enabled" : "Module Disabled";
        NotificationType type = enabled ? NotificationType.INFO : NotificationType.WARNING;
        Notifications.addNotification(title, moduleName, type);
    }

    @Override
    public void onRender(class_332 graphics, float tickDelta) {
        if (!this.isInGame()) {
            return;
        }
        long now = System.currentTimeMillis();
        long durationMs = (long)((Double)this.duration.getValue() * 1000.0);
        Iterator<Notification> iter = notifications.iterator();
        while (iter.hasNext()) {
            Notification n = iter.next();
            if (now - n.timestamp <= durationMs) continue;
            iter.remove();
        }
        int screenWidth = DisplaySpace.width();
        int screenHeight = DisplaySpace.height();
        int notifWidth = 160;
        int notifHeight = 30;
        int padding = 8;
        int y = screenHeight - padding - notifHeight;
        int total = notifications.size();
        int shown = Math.min(total, 5);
        for (int k = 0; k < shown; ++k) {
            Notification n = notifications.get(total - 1 - k);
            float age = (float)(now - n.timestamp) / (float)durationMs;
            float alpha = 1.0f;
            if (age < 0.1f) {
                alpha = age / 0.1f;
            } else if (age > 0.8f) {
                alpha = (1.0f - age) / 0.2f;
            }
            int x = screenWidth - notifWidth - padding;
            if (age < 0.1f) {
                x += (int)((1.0f - age / 0.1f) * (float)(notifWidth + padding));
            }
            int bgColor = switch (n.type.ordinal()) {
                case 1 -> -14505438;
                case 2 -> -5592542;
                case 3 -> -5627358;
                default -> -15066578;
            };
            bgColor = bgColor & 0xFFFFFF | (int)(alpha * 200.0f) << 24;
            int borderColor = 0xAAFF | (int)(alpha * 255.0f) << 24;
            int textColor = 0xFFFFFF | (int)(alpha * 255.0f) << 24;
            int subtextColor = 0xAAAAAA | (int)(alpha * 255.0f) << 24;
            graphics.method_25294(x, y, x + notifWidth, y + notifHeight, bgColor);
            graphics.method_25294(x, y, x + 3, y + notifHeight, borderColor);
            graphics.method_51433(Notifications.mc.field_1772, n.title, x + 8, y + 4, textColor, true);
            graphics.method_51433(Notifications.mc.field_1772, n.message, x + 8, y + 16, subtextColor, true);
            y -= notifHeight + 4;
        }
    }

    @Environment(value=EnvType.CLIENT)
    private static class Notification {
        String title;
        String message;
        NotificationType type;
        long timestamp;

        Notification(String title, String message, NotificationType type, long timestamp) {
            this.title = title;
            this.message = message;
            this.type = type;
            this.timestamp = timestamp;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum NotificationType {
        INFO,
        SUCCESS,
        WARNING,
        ERROR;

    }
}

