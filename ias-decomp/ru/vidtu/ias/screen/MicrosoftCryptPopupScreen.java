/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_364
 *  net.minecraft.class_437
 *  net.minecraft.class_5244
 *  net.minecraft.class_7919
 *  org.joml.Matrix3x2fStack
 *  org.lwjgl.glfw.GLFW
 */
package ru.vidtu.ias.screen;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.class_11908;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_364;
import net.minecraft.class_437;
import net.minecraft.class_5244;
import net.minecraft.class_7919;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.config.IASConfig;
import ru.vidtu.ias.crypt.DummyCrypt;
import ru.vidtu.ias.crypt.HardwareCrypt;
import ru.vidtu.ias.screen.MicrosoftPopupScreen;
import ru.vidtu.ias.screen.PopupButton;

final class MicrosoftCryptPopupScreen
extends class_437 {
    private final class_437 parent;
    private final Consumer<Account> handler;
    private PopupButton plain;

    MicrosoftCryptPopupScreen(class_437 parent, Consumer<Account> handler) {
        super((class_2561)class_2561.method_43471((String)"ias.microsoft"));
        this.parent = parent;
        this.handler = handler;
    }

    protected void method_25426() {
        assert (this.field_22787 != null);
        if (this.parent != null) {
            this.parent.method_25423(this.field_22789, this.field_22790);
        }
        PopupButton button = new PopupButton(this.field_22789 / 2 - 75, this.field_22790 / 2 - 24 - 12, 150, 20, (class_2561)class_2561.method_43471((String)"ias.microsoft.password"), btn -> this.field_22787.method_1507((class_437)new MicrosoftPopupScreen(this.parent, this.handler, null)), Supplier::get);
        button.method_47400(class_7919.method_47407((class_2561)class_2561.method_43471((String)"ias.microsoft.password.tip")));
        button.method_47402(Duration.ofMillis(250L));
        button.color(0.5f, 1.0f, 0.5f, true);
        this.method_37063((class_364)button);
        button = new PopupButton(this.field_22789 / 2 - 75, this.field_22790 / 2 - 12, 150, 20, (class_2561)class_2561.method_43471((String)"ias.microsoft.hardware"), btn -> this.field_22787.method_1507((class_437)new MicrosoftPopupScreen(this.parent, this.handler, HardwareCrypt.INSTANCE_V2)), Supplier::get);
        button.method_47400(class_7919.method_47407((class_2561)class_2561.method_43471((String)"ias.microsoft.hardware.tip")));
        button.method_47402(Duration.ofMillis(250L));
        button.color(1.0f, 1.0f, 0.5f, true);
        this.method_37063((class_364)button);
        this.plain = new PopupButton(this.field_22789 / 2 - 75, this.field_22790 / 2 + 12, 150, 20, (class_2561)class_2561.method_43471((String)"ias.microsoft.plain"), btn -> this.field_22787.method_1507((class_437)new MicrosoftPopupScreen(this.parent, this.handler, DummyCrypt.INSTANCE)), Supplier::get);
        if (IASConfig.allowNoCrypt) {
            this.plain.method_47400(class_7919.method_47407((class_2561)class_2561.method_43469((String)"ias.microsoft.plain.tip.off", (Object[])new Object[]{class_2561.method_43471((String)"key.keyboard.left.alt"), GLFW.glfwGetKeyName((int)89, (int)-1)})));
        } else {
            this.plain.method_47400(class_7919.method_47407((class_2561)class_2561.method_43471((String)"ias.microsoft.plain.tip.no")));
        }
        this.plain.method_47402(Duration.ofMillis(250L));
        this.plain.color(1.0f, 0.5f, 0.5f, true);
        this.plain.field_22763 = false;
        this.method_37063((class_364)this.plain);
        this.method_37063((class_364)new PopupButton(this.field_22789 / 2 - 75, this.field_22790 / 2 + 79 - 22, 150, 20, class_5244.field_24335, btn -> this.method_25419(), Supplier::get));
    }

    public void method_25394(class_332 graphics, int mouseX, int mouseY, float delta) {
        assert (this.field_22787 != null);
        Matrix3x2fStack pose = graphics.method_51448();
        super.method_25394(graphics, mouseX, mouseY, delta);
        pose.pushMatrix();
        pose.scale(2.0f, 2.0f);
        graphics.method_27534(this.field_22793, this.field_22785, this.field_22789 / 4, this.field_22790 / 4 - 39, -1);
        pose.popMatrix();
    }

    public void method_25420(class_332 graphics, int mouseX, int mouseY, float delta) {
        assert (this.field_22787 != null);
        if (this.parent != null) {
            this.parent.method_47413(graphics, 0, 0, delta);
            graphics.method_71048();
            graphics.method_25294(0, 0, this.field_22789, this.field_22790, Integer.MIN_VALUE);
        } else {
            super.method_25420(graphics, mouseX, mouseY, delta);
        }
        int centerX = this.field_22789 / 2;
        int centerY = this.field_22790 / 2;
        graphics.method_25294(centerX - 80, centerY - 80, centerX + 80, centerY + 80, -132112336);
        graphics.method_25294(centerX - 79, centerY - 81, centerX + 79, centerY - 80, -132112336);
        graphics.method_25294(centerX - 79, centerY + 80, centerX + 79, centerY + 81, -132112336);
    }

    public void method_25419() {
        assert (this.field_22787 != null);
        this.field_22787.method_1507(this.parent);
    }

    public boolean method_25404(class_11908 event) {
        int key = event.comp_4795();
        boolean alt = event.method_74238();
        if (key == 89 && IASConfig.allowNoCrypt && this.plain != null && !this.plain.method_37303() && alt) {
            this.plain.field_22763 = true;
            this.plain.method_47400(class_7919.method_47407((class_2561)class_2561.method_43471((String)"ias.microsoft.plain.tip.on")));
            this.plain.method_47402(Duration.ofMillis(250L));
            this.plain.color(1.0f, 0.25f, 0.25f, false);
        }
        return super.method_25404(event);
    }

    public String toString() {
        return "MicrosoftCryptPopupScreen{}";
    }
}

