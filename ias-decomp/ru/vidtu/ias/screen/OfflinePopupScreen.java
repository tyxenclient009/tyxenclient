/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_124
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_364
 *  net.minecraft.class_437
 *  net.minecraft.class_5244
 *  net.minecraft.class_7919
 *  org.joml.Matrix3x2fStack
 */
package ru.vidtu.ias.screen;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.class_11908;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_364;
import net.minecraft.class_437;
import net.minecraft.class_5244;
import net.minecraft.class_7919;
import org.joml.Matrix3x2fStack;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.account.OfflineAccount;
import ru.vidtu.ias.auth.microsoft.MSAuth;
import ru.vidtu.ias.config.IASConfig;
import ru.vidtu.ias.screen.PopupBox;
import ru.vidtu.ias.screen.PopupButton;

public final class OfflinePopupScreen
extends class_437 {
    private final class_437 parent;
    private final Consumer<Account> handler;
    private PopupBox name;
    private PopupButton done;
    private boolean locked = false;

    OfflinePopupScreen(class_437 parent, Consumer<Account> handler) {
        super((class_2561)class_2561.method_43471((String)"ias.offline"));
        this.parent = parent;
        this.handler = handler;
    }

    protected void method_25426() {
        assert (this.field_22787 != null);
        if (this.parent != null) {
            this.parent.method_25423(this.field_22789, this.field_22790);
        }
        this.name = new PopupBox(this.field_22793, this.field_22789 / 2 - 75, this.field_22790 / 2 - 10 + 5, 148, 20, this.name, (class_2561)class_2561.method_43471((String)"ias.offline.nick"), this::done, false);
        this.name.method_1880(16);
        if (IASConfig.unexpectedPigs) {
            this.name.method_47404((class_2561)class_2561.method_43470((String)("Boar" + ((Object)((Object)this)).hashCode())).method_27692(class_124.field_1063));
        } else {
            this.name.method_47404((class_2561)class_2561.method_43470((String)"Steve").method_27692(class_124.field_1063));
        }
        this.method_37063((class_364)this.name);
        this.done = new PopupButton(this.field_22789 / 2 - 75, this.field_22790 / 2 + 49 - 22, 74, 20, class_5244.field_24334, btn -> this.done(), Supplier::get);
        this.done.color(1.0f, 0.5f, 0.5f, true);
        this.method_37063((class_364)this.done);
        PopupButton button = new PopupButton(this.field_22789 / 2 + 1, this.field_22790 / 2 + 49 - 22, 74, 20, class_5244.field_24335, btn -> this.method_25419(), Supplier::get);
        button.color(1.0f, 1.0f, 1.0f, true);
        this.method_37063((class_364)button);
        this.name.method_1863(value -> this.type(false));
        this.type(true);
    }

    private void done() {
        assert (this.field_22787 != null);
        if (this.name == null) {
            return;
        }
        String value = this.name.method_1882();
        if (value.isBlank()) {
            return;
        }
        int length = value.length();
        if (length < 3 || length > 16) {
            this.handler.accept(new OfflineAccount(value, null));
            return;
        }
        for (int i = 0; i < length; ++i) {
            int c = value.codePointAt(i);
            if (c == 95 || c >= 48 && c <= 57 || c >= 97 && c <= 122 || c >= 65 && c <= 90) continue;
            this.handler.accept(new OfflineAccount(value, null));
            return;
        }
        this.locked = true;
        this.type(false);
        MSAuth.nameToMcp(value).whenCompleteAsync((profile, throwable) -> {
            UUID skin = profile != null ? profile.uuid() : null;
            this.handler.accept(new OfflineAccount(value, skin));
        }, (Executor)this.field_22787);
    }

    private void type(boolean instant) {
        if (this.done == null || this.name == null) {
            return;
        }
        if (this.locked) {
            this.done.field_22763 = false;
            this.name.field_22763 = false;
            this.done.method_47400(null);
            this.done.color(0.5f, 0.5f, 0.5f, instant);
            return;
        }
        String value = this.name.method_1882();
        this.name.field_22763 = true;
        if (value.isBlank()) {
            this.done.field_22763 = false;
            this.done.method_47400(class_7919.method_47407((class_2561)class_2561.method_43471((String)"ias.offline.nick.blank")));
            this.done.method_47402(Duration.ZERO);
            this.done.color(1.0f, 0.5f, 0.5f, instant);
            return;
        }
        this.done.field_22763 = true;
        int length = value.length();
        if (length < 3) {
            if (this.field_22787.method_74189()) {
                this.done.field_22763 = true;
                this.done.color(0.75f, 0.75f, 0.25f, instant);
            } else {
                this.done.field_22763 = false;
                this.done.color(1.0f, 1.0f, 0.5f, instant);
            }
            this.done.method_47400(class_7919.method_47407((class_2561)class_2561.method_43469((String)"ias.offline.nick.short", (Object[])new Object[]{class_2561.method_43471((String)"key.keyboard.left.alt")})));
            this.done.method_47402(Duration.ZERO);
            return;
        }
        if (length > 16) {
            if (this.field_22787.method_74189()) {
                this.done.field_22763 = true;
                this.done.color(0.75f, 0.75f, 0.25f, instant);
            } else {
                this.done.field_22763 = false;
                this.done.color(1.0f, 1.0f, 0.5f, instant);
            }
            this.done.method_47400(class_7919.method_47407((class_2561)class_2561.method_43469((String)"ias.offline.nick.long", (Object[])new Object[]{class_2561.method_43471((String)"key.keyboard.left.alt")})));
            this.done.method_47402(Duration.ZERO);
            return;
        }
        for (int i = 0; i < length; ++i) {
            boolean alt;
            int c = value.codePointAt(i);
            if (c == 95 || c >= 48 && c <= 57 || c >= 97 && c <= 122 || c >= 65 && c <= 90) continue;
            this.done.field_22763 = alt = this.field_22787.method_74189();
            if (alt) {
                this.done.field_22763 = true;
                this.done.color(0.75f, 0.75f, 0.25f, instant);
            } else {
                this.done.field_22763 = false;
                this.done.color(1.0f, 1.0f, 0.5f, instant);
            }
            this.done.method_47400(class_7919.method_47407((class_2561)class_2561.method_43469((String)"ias.offline.nick.chars", (Object[])new Object[]{Character.toString(c), class_2561.method_43471((String)"key.keyboard.left.alt")})));
            this.done.method_47402(Duration.ZERO);
            return;
        }
        this.done.color(0.5f, 1.0f, 0.5f, instant);
        this.done.method_47400(null);
    }

    public boolean method_25404(class_11908 event) {
        int key = event.comp_4795();
        boolean res = super.method_25404(event);
        if (key == 342 || key == 346) {
            this.type(false);
        }
        return res;
    }

    public boolean method_16803(class_11908 event) {
        int key = event.comp_4795();
        boolean res = super.method_16803(event);
        if (key == 342 || key == 346) {
            this.type(false);
        }
        return res;
    }

    public void method_25394(class_332 graphics, int mouseX, int mouseY, float delta) {
        assert (this.field_22787 != null);
        Matrix3x2fStack pose = graphics.method_51448();
        super.method_25394(graphics, mouseX, mouseY, delta);
        pose.pushMatrix();
        pose.scale(2.0f, 2.0f);
        graphics.method_27534(this.field_22793, this.field_22785, this.field_22789 / 4, this.field_22790 / 4 - 24, -1);
        pose.popMatrix();
        if (this.name != null) {
            graphics.method_27534(this.field_22793, this.name.method_25369(), this.field_22789 / 2, this.field_22790 / 2 - 10 - 5, -1);
        }
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

    public void method_25419() {
        assert (this.field_22787 != null);
        this.field_22787.method_1507(this.parent);
    }

    public String toString() {
        return "OfflinePopupScreen{}";
    }
}

