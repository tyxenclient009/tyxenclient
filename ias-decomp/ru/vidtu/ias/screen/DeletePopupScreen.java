/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_2561
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_364
 *  net.minecraft.class_437
 *  net.minecraft.class_5244
 *  net.minecraft.class_5489
 *  net.minecraft.class_7919
 *  org.joml.Matrix3x2fStack
 */
package ru.vidtu.ias.screen;

import java.time.Duration;
import java.util.function.Supplier;
import net.minecraft.class_11908;
import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_364;
import net.minecraft.class_437;
import net.minecraft.class_5244;
import net.minecraft.class_5489;
import net.minecraft.class_7919;
import org.joml.Matrix3x2fStack;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.platform.IStonecutter;
import ru.vidtu.ias.screen.PopupButton;

final class DeletePopupScreen
extends class_437 {
    private final class_437 parent;
    private final class_2561 prompt;
    private final Runnable handler;
    private class_5489 label;

    DeletePopupScreen(class_437 parent, Account account, Runnable handler) {
        super((class_2561)class_2561.method_43471((String)"ias.delete"));
        this.parent = parent;
        this.prompt = class_2561.method_43469((String)"ias.delete.confirm", (Object[])new Object[]{account.name()});
        this.handler = handler;
    }

    protected void method_25426() {
        assert (this.field_22787 != null);
        if (this.parent != null) {
            this.parent.method_25423(this.field_22789, this.field_22790);
        }
        PopupButton button = new PopupButton(this.field_22789 / 2 - 75, this.field_22790 / 2 + 49 - 22, 74, 20, this.field_22785, btn -> {
            this.handler.run();
            this.method_25419();
        }, Supplier::get);
        button.method_47400(class_7919.method_47407((class_2561)class_2561.method_43469((String)"ias.delete.hint", (Object[])new Object[]{class_2561.method_43471((String)"key.keyboard.left.shift")})));
        button.method_47402(Duration.ofMillis(250L));
        button.color(1.0f, 0.5f, 0.5f, true);
        this.method_37063((class_364)button);
        this.method_37063((class_364)new PopupButton(this.field_22789 / 2 + 1, this.field_22790 / 2 + 49 - 22, 74, 20, class_5244.field_24335, btn -> this.method_25419(), Supplier::get));
        this.label = class_5489.method_30890((class_327)this.field_22793, (class_2561)this.prompt, (int)150);
    }

    public void method_25394(class_332 graphics, int mouseX, int mouseY, float delta) {
        assert (this.field_22787 != null);
        Matrix3x2fStack pose = graphics.method_51448();
        super.method_25394(graphics, mouseX, mouseY, delta);
        pose.pushMatrix();
        pose.scale(2.0f, 2.0f);
        graphics.method_27534(this.field_22793, this.field_22785, this.field_22789 / 4, this.field_22790 / 4 - 24, -1);
        pose.popMatrix();
        IStonecutter.renderMultilineLabelCentered(this.label, graphics, this.field_22789 / 2, (this.field_22790 - this.label.method_30887() * 9) / 2 - 4);
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
        graphics.method_25294(centerX - 80, centerY - 50, centerX + 80, centerY + 50, -132112336);
        graphics.method_25294(centerX - 79, centerY - 51, centerX + 79, centerY - 50, -132112336);
        graphics.method_25294(centerX - 79, centerY + 50, centerX + 79, centerY + 51, -132112336);
    }

    public boolean method_25404(class_11908 event) {
        boolean select = event.method_74229();
        if (select) {
            this.handler.run();
            this.method_25419();
            return true;
        }
        return super.method_25404(event);
    }

    public void method_25419() {
        assert (this.field_22787 != null);
        this.field_22787.method_1507(this.parent);
    }

    public String toString() {
        return "DeletePopupScreen{}";
    }
}

