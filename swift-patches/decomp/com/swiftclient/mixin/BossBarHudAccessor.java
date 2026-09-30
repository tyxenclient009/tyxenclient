/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_337
 *  net.minecraft.class_345
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package com.swiftclient.mixin;

import java.util.Map;
import java.util.UUID;
import net.minecraft.class_337;
import net.minecraft.class_345;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_337.class})
public interface BossBarHudAccessor {
    @Accessor(value="field_2060")
    public Map<UUID, class_345> swift$bossBars();
}

