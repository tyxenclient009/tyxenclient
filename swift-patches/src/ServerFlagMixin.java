package com.swiftclient.mixin;

import com.swiftclient.util.ServerGeo;
import java.util.Arrays;
import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_642;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * TYX addition — Feather-style server flags in the multiplayer list.
 * Draws a small country-code chip right after each server's name (resolved
 * off-thread and cached, so the list never stalls); hovering the chip
 * shows a tooltip with the full country and the server's ISP.
 */
@Mixin(targets = "net.minecraft.class_4267$class_4270")
public abstract class ServerFlagMixin {
    @Shadow
    private net.minecraft.class_642 field_19120;

    @Shadow
    public abstract int method_73380();

    @Shadow
    public abstract int method_73382();

    @Inject(method = {
        "method_25343(Lnet/minecraft/class_332;IIZF)V"}, at = {@At(value = "TAIL")}, require = 0)
    private void swift$flag(class_332 ctx, int mouseX, int mouseY, boolean hovered, CallbackInfo ci) {
        try {
            class_642 server = this.field_19120;
            if (server == null || server.field_3761 == null) {
                return;
            }
            ServerGeo.Info geo = ServerGeo.get(server.field_3761);
            if (geo == null) {
                return;
            }
            class_310 client = class_310.method_1551();
            if (client == null) {
                return;
            }
            class_327 tr = client.field_1772;
            int nameW = tr.method_1727(server.field_3752 == null ? "" : server.field_3752);
            int tw = tr.method_1727(geo.code());
            int x = this.method_73380() + 32 + 3 + nameW + 5;
            int y = this.method_73382() + 1;
            ctx.method_25294(x - 3, y - 1, x + tw + 3, y + 10, -1728053248);
            ctx.method_25294(x - 3, y - 1, x + tw + 3, y, -12872002);
            ctx.method_27535(tr, class_2561.method_43470(geo.code()), x, y + 1, -1);
            if (mouseX >= x - 3 && mouseX < x + tw + 3 && mouseY >= y - 1 && mouseY < y + 10) {
                List<class_2561> lines = Arrays.asList(
                        class_2561.method_43470(geo.country()),
                        class_2561.method_43470("ISP: " + geo.isp()));
                ctx.method_51434(tr, lines, mouseX, mouseY);
            }
        } catch (Throwable t) {
            System.err.println("[swiftclient] server flag failed: " + t);
        }
    }
}
