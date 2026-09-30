/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_2561
 *  net.minecraft.class_338
 *  net.minecraft.class_5250
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 */
package net.tyxen.hud.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.tyxen.hud.core.ModuleManager;
import net.tyxen.hud.modules.impl.hud.ChatTimestamps;
import net.tyxen.hud.modules.impl.utility.AutoGG;
import net.minecraft.class_2561;
import net.minecraft.class_338;
import net.minecraft.class_5250;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_338.class})
public class ChatHudMixin {
    @ModifyVariable(method={"method_44811"}, at=@At(value="HEAD"), argsOnly=true, ordinal=0)
    private class_2561 modifyMessage(class_2561 message) {
        ChatTimestamps timestamps;
        AutoGG autoGG;
        ModuleManager mm = ModuleManager.getInstance();
        if (mm != null && (autoGG = mm.getModule(AutoGG.class)) != null && autoGG.isEnabled()) {
            autoGG.onChatReceived(message.getString());
        }
        if ((timestamps = ChatTimestamps.getInstance()) == null || !timestamps.isEnabled()) {
            return message;
        }
        class_5250 timestampComponent = class_2561.method_43470((String)timestamps.getFormattedTimestamp());
        return timestampComponent.method_10852(message);
    }
}

