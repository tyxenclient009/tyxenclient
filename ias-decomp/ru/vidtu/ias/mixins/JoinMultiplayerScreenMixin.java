/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_344
 *  net.minecraft.class_350
 *  net.minecraft.class_364
 *  net.minecraft.class_4185
 *  net.minecraft.class_437
 *  net.minecraft.class_500
 *  net.minecraft.class_7919
 *  net.minecraft.class_8021
 *  org.jetbrains.annotations.ApiStatus$Internal
 *  org.jetbrains.annotations.Contract
 *  org.jspecify.annotations.NullMarked
 *  org.jspecify.annotations.Nullable
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.vidtu.ias.mixins;

import java.time.Duration;
import net.minecraft.class_2561;
import net.minecraft.class_344;
import net.minecraft.class_350;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_500;
import net.minecraft.class_7919;
import net.minecraft.class_8021;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vidtu.ias.IASMinecraft;
import ru.vidtu.ias.config.IASConfig;
import ru.vidtu.ias.screen.AccountScreen;
import ru.vidtu.ias.utils.Expression;

@NullMarked
@ApiStatus.Internal
@Mixin(value={class_500.class})
public final class JoinMultiplayerScreenMixin
extends class_437 {
    @Unique
    private @Nullable class_4185 ias_button;

    @Deprecated
    @Contract(value="-> fail", pure=true)
    private JoinMultiplayerScreenMixin() {
        super(null);
        throw new AssertionError((Object)"IAS: No instances.");
    }

    @Inject(method={"method_48640"}, at={@At(value="RETURN")})
    private void ias_repositionElements_return(CallbackInfo ci) {
        if (!IASConfig.serversButton) {
            return;
        }
        Integer x = Expression.parsePosition(IASConfig.serversButtonX, this.field_22789, this.field_22790);
        Integer y = Expression.parsePosition(IASConfig.serversButtonY, this.field_22789, this.field_22790);
        if (x == null || y == null) {
            x = this.field_22789 / 2 + 158;
            y = this.field_22790 - 30;
            for (int i = 0; i < 64; ++i) {
                boolean overlapping = false;
                for (class_364 child : this.method_25396()) {
                    if (!(child instanceof class_8021)) continue;
                    class_8021 le = (class_8021)child;
                    if (child instanceof class_350 || child == this.ias_button) continue;
                    int x1 = le.method_46426() - 4;
                    int y1 = le.method_46427() - 4;
                    int x2 = x1 + le.method_25368() + 8;
                    int y2 = y1 + le.method_25364() + 8;
                    if (x < x1 || y < y1 || x + 20 > x2 || y + 20 > y2) continue;
                    x = Math.max(x, x2);
                    overlapping = true;
                }
                if (!overlapping) break;
            }
        }
        if (this.ias_button != null) {
            this.ias_button.method_46421(x.intValue());
            this.ias_button.method_46419(y.intValue());
        } else {
            this.ias_button = new class_344(x.intValue(), y.intValue(), 20, 20, IASMinecraft.BUTTON, btn -> this.field_22787.method_1507((class_437)new AccountScreen(this)), (class_2561)class_2561.method_43470((String)"In-Game Account Switcher"));
            class_344 button = this.ias_button;
            button.method_47400(class_7919.method_47407((class_2561)button.method_25369()));
            button.method_47402(Duration.ofMillis(250L));
            this.method_37063((class_364)button);
        }
    }
}

