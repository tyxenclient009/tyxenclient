/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.minecraft.UserApiService
 *  com.mojang.authlib.minecraft.UserApiService$UserProperties
 *  com.mojang.authlib.yggdrasil.ProfileResult
 *  net.minecraft.class_310
 *  net.minecraft.class_320
 *  net.minecraft.class_5520
 *  net.minecraft.class_6628
 *  net.minecraft.class_7497
 *  net.minecraft.class_7574
 *  net.minecraft.class_7853
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package ru.vidtu.ias.mixins;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_310;
import net.minecraft.class_320;
import net.minecraft.class_5520;
import net.minecraft.class_6628;
import net.minecraft.class_7497;
import net.minecraft.class_7574;
import net.minecraft.class_7853;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_310.class})
public interface MinecraftAccessor {
    @Accessor(value="field_62106")
    @Mutable
    public void ias$services(class_7497 var1);

    @Accessor(value="field_1726")
    @Mutable
    public void ias$user(class_320 var1);

    @Accessor(value="field_45899")
    @Mutable
    public void ias$profileFuture(CompletableFuture<ProfileResult> var1);

    @Accessor(value="field_26902")
    @Mutable
    public void ias$userApiService(UserApiService var1);

    @Accessor(value="field_47680")
    @Mutable
    public void ias$userPropertiesFuture(CompletableFuture<UserApiService.UserProperties> var1);

    @Accessor(value="field_26842")
    @Mutable
    public void ias$playerSocialManager(class_5520 var1);

    @Accessor(value="field_41331")
    @Mutable
    public void ias$telemetryManager(class_6628 var1);

    @Accessor(value="field_39068")
    @Mutable
    public void ias$profileKeyPairManager(class_7853 var1);

    @Accessor(value="field_39492")
    @Mutable
    public void ias$reportingContext(class_7574 var1);
}

