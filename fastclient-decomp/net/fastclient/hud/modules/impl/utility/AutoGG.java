/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.fastclient.hud.modules.impl.utility;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fastclient.hud.modules.Category;
import net.fastclient.hud.modules.Module;
import net.fastclient.hud.modules.settings.NumberSetting;
import net.fastclient.hud.modules.settings.TextSetting;

@Environment(value=EnvType.CLIENT)
public class AutoGG
extends Module {
    private final TextSetting message = this.register(new TextSetting("message", "Message to send", "gg"));
    private final NumberSetting delay = this.register(new NumberSetting("delay", "Delay before sending (seconds)", 1.0, 0.5, 5.0, 0.5));
    private static final String[] TRIGGERS = new String[]{"has won", "winner", "winners", "victory", "game over", "game ended", "1st place", "you win", "you lost", "you died", "top survivors", "won the game"};
    private static final int COOLDOWN_TICKS = 200;
    private int sendDelayTicks = -1;
    private int cooldownTicks = 0;

    public AutoGG() {
        super("AutoGG", "Automatically send GG when a game ends", Category.UTILITY);
    }

    @Override
    protected void onEnable() {
        this.sendDelayTicks = -1;
        this.cooldownTicks = 0;
    }

    public void onChatReceived(String text) {
        if (this.cooldownTicks > 0 || this.sendDelayTicks >= 0) {
            return;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        for (String trigger : TRIGGERS) {
            if (!lower.contains(trigger)) continue;
            this.sendDelayTicks = (int)((Double)this.delay.getValue() * 20.0);
            return;
        }
    }

    @Override
    public void onTick() {
        if (!this.isInGame()) {
            return;
        }
        if (this.cooldownTicks > 0) {
            --this.cooldownTicks;
        }
        if (this.sendDelayTicks >= 0) {
            --this.sendDelayTicks;
            if (this.sendDelayTicks < 0) {
                this.sendGG();
            }
        }
    }

    private void sendGG() {
        String text = (String)this.message.getValue();
        if (text == null || text.isEmpty() || mc.method_1562() == null) {
            return;
        }
        if (text.startsWith("/")) {
            mc.method_1562().method_45730(text.substring(1));
        } else {
            mc.method_1562().method_45729(text);
        }
        this.cooldownTicks = 200;
    }
}

