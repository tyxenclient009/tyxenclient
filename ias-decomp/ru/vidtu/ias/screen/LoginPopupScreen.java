/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11735
 *  net.minecraft.class_12225
 *  net.minecraft.class_124
 *  net.minecraft.class_2561
 *  net.minecraft.class_2583
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_364
 *  net.minecraft.class_437
 *  net.minecraft.class_5244
 *  net.minecraft.class_5250
 *  net.minecraft.class_5481
 *  net.minecraft.class_5489
 *  org.joml.Matrix3x2fStack
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package ru.vidtu.ias.screen;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import net.minecraft.class_11735;
import net.minecraft.class_12225;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_364;
import net.minecraft.class_437;
import net.minecraft.class_5244;
import net.minecraft.class_5250;
import net.minecraft.class_5481;
import net.minecraft.class_5489;
import org.joml.Matrix3x2fStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.IASMinecraft;
import ru.vidtu.ias.auth.LoginData;
import ru.vidtu.ias.auth.handlers.LoginHandler;
import ru.vidtu.ias.config.IASConfig;
import ru.vidtu.ias.platform.IStonecutter;
import ru.vidtu.ias.screen.PopupBox;
import ru.vidtu.ias.screen.PopupButton;
import ru.vidtu.ias.utils.exceptions.FriendlyException;

final class LoginPopupScreen
extends class_437
implements LoginHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"IAS/LoginPopupScreen");
    private final class_437 parent;
    private final Object lock = new Object();
    private class_2561 stage = class_2561.method_43471((String)"ias.login.initializing").method_27692(class_124.field_1054);
    private class_5489 label;
    private PopupBox password;
    private CompletableFuture<String> passFuture;
    private class_5489 cryptPasswordTip;
    private float error = Float.NaN;
    private class_5489 errorNote;

    LoginPopupScreen(class_437 parent) {
        super((class_2561)class_2561.method_43471((String)"ias.login"));
        this.parent = parent;
    }

    @Override
    public boolean cancelled() {
        assert (this.field_22787 != null);
        return this != this.field_22787.field_1755;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void method_25426() {
        assert (this.field_22787 != null);
        Object object = this.lock;
        synchronized (object) {
            this.label = null;
        }
        if (this.parent != null) {
            this.parent.method_25423(this.field_22789, this.field_22790);
        }
        this.method_37063((class_364)new PopupButton(this.field_22789 / 2 - 75, this.field_22790 / 2 + 74 - 22, 150, 20, class_5244.field_24335, btn -> this.method_25419(), Supplier::get));
        if (this.passFuture != null) {
            this.password = new PopupBox(this.field_22793, this.field_22789 / 2 - 100, this.field_22790 / 2 - 10 + 5, 178, 20, this.password, (class_2561)class_2561.method_43471((String)"ias.password"), () -> {
                if (this.passFuture == null || this.password == null) {
                    return;
                }
                String value = this.password.method_1882();
                if (value.isBlank()) {
                    return;
                }
                this.passFuture.complete(value);
            }, true);
            this.password.method_47404((class_2561)class_2561.method_43471((String)"ias.password.hint").method_27692(class_124.field_1063));
            this.password.method_73210((s, i) -> IASConfig.passwordEchoing ? class_5481.method_30747((String)"*".repeat(s.length()), (class_2583)class_2583.field_24360) : class_5481.field_26385);
            this.password.method_1880(32);
            this.method_37063((class_364)this.password);
            PopupButton button = new PopupButton(this.field_22789 / 2 - 100 + 180, this.field_22790 / 2 - 10 + 5, 20, 20, (class_2561)class_2561.method_43470((String)">>"), btn -> {
                if (this.passFuture == null || this.password == null) {
                    return;
                }
                String value = this.password.method_1882();
                if (value.isBlank()) {
                    return;
                }
                this.passFuture.complete(value);
            }, Supplier::get);
            button.field_22763 = !this.password.method_1882().isBlank();
            this.method_37063((class_364)button);
            this.password.method_1863(value -> {
                button.field_22763 = !value.isBlank();
            });
            this.cryptPasswordTip = class_5489.method_30890((class_327)this.field_22793, (class_2561)class_2561.method_43471((String)"ias.password.tip").method_54663(0xFFFF00), (int)320);
        }
    }

    public void method_25419() {
        assert (this.field_22787 != null);
        if (this.passFuture != null) {
            this.passFuture.complete(null);
        }
        this.field_22787.method_1507(this.parent);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void method_25394(class_332 graphics, int mouseX, int mouseY, float delta) {
        assert (this.field_22787 != null);
        Matrix3x2fStack pose = graphics.method_51448();
        super.method_25394(graphics, mouseX, mouseY, delta);
        pose.pushMatrix();
        pose.scale(2.0f, 2.0f);
        graphics.method_27534(this.field_22793, this.field_22785, this.field_22789 / 4, this.field_22790 / 4 - 37, -1);
        pose.popMatrix();
        if (this.passFuture != null && this.password != null && this.cryptPasswordTip != null) {
            graphics.method_27534(this.field_22793, this.password.method_25369(), this.field_22789 / 2, this.field_22790 / 2 - 10 - 5, -1);
            pose.pushMatrix();
            pose.scale(0.5f, 0.5f);
            IStonecutter.renderMultilineLabelCentered(this.cryptPasswordTip, graphics, this.field_22789, this.field_22790 + 40);
            pose.popMatrix();
        } else {
            Object object = this.lock;
            synchronized (object) {
                if (this.label == null) {
                    class_2561 component = (class_2561)Objects.requireNonNullElse(this.stage, class_2561.method_43473());
                    this.label = class_5489.method_30890((class_327)this.field_22793, (class_2561)component, (int)240);
                    this.field_22787.method_44713().method_47976(component);
                }
                IStonecutter.renderMultilineLabelCentered(this.label, graphics, this.field_22789 / 2, (this.field_22790 - this.label.method_30887() * 9) / 2 - 4);
            }
            if (Float.isFinite(this.error)) {
                int opacityMask;
                float opacityFloat;
                if (this.errorNote == null) {
                    this.errorNote = class_5489.method_30890((class_327)this.field_22793, (class_2561)class_2561.method_43471((String)"ias.error.note").method_27692(class_124.field_1075), (int)245);
                }
                if (this.error < 1.0f) {
                    this.error = Math.min(this.error + delta * 0.1f, 1.0f);
                    opacityFloat = this.error * this.error * this.error * this.error;
                    int opacity = Math.max(9, (int)(opacityFloat * 255.0f));
                    opacityMask = opacity << 24;
                } else {
                    opacityFloat = 1.0f;
                    opacityMask = -16777216;
                }
                int w = this.errorNote.method_44048() / 4 + 2;
                int h = this.errorNote.method_30887() * 9 / 2 + 1;
                int cx = this.field_22789 / 2;
                int sy = this.field_22790 / 2 + 87;
                graphics.method_25294(cx - w, sy, cx + w, sy + h, 0x101010 | opacityMask);
                graphics.method_25294(cx - w + 1, sy - 1, cx + w - 1, sy, 0x101010 | opacityMask);
                graphics.method_25294(cx - w + 1, sy + h, cx + w - 1, sy + h + 1, 0x101010 | opacityMask);
                pose.pushMatrix();
                pose.scale(0.5f, 0.5f);
                class_12225 renderer = graphics.method_75788();
                renderer.method_75764(renderer.method_75760().method_75782(opacityFloat));
                this.errorNote.method_75816(class_11735.field_62010, this.field_22789, this.field_22790 + 174, 9, renderer);
                pose.popMatrix();
            }
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
        graphics.method_25294(centerX - 125, centerY - 75, centerX + 125, centerY + 75, -132112336);
        graphics.method_25294(centerX - 124, centerY - 76, centerX + 124, centerY - 75, -132112336);
        graphics.method_25294(centerX - 124, centerY + 75, centerX + 124, centerY + 76, -132112336);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void stage(String stage, Object ... args) {
        assert (this.field_22787 != null);
        if (this != this.field_22787.field_1755) {
            return;
        }
        class_5250 component = class_2561.method_43469((String)stage, (Object[])args).method_27692(class_124.field_1054);
        Object object = this.lock;
        synchronized (object) {
            this.stage = component;
            this.label = null;
        }
    }

    @Override
    public CompletableFuture<String> password() {
        assert (this.field_22787 != null);
        if (this.passFuture != null) {
            return this.passFuture;
        }
        this.passFuture = new CompletableFuture();
        this.passFuture.thenAcceptAsync(password -> {
            this.passFuture = null;
            this.password = null;
            this.cryptPasswordTip = null;
            this.method_25423(this.field_22789, this.field_22790);
        }, (Executor)this.field_22787);
        this.field_22787.execute(() -> this.method_25423(this.field_22789, this.field_22790));
        return this.passFuture;
    }

    @Override
    public void success(LoginData data, boolean changed) {
        assert (this.field_22787 != null);
        if (this != this.field_22787.field_1755) {
            return;
        }
        if (data == null) {
            this.field_22787.execute(() -> {
                if (this != this.field_22787.field_1755) {
                    return;
                }
                this.field_22787.method_1507(this.parent);
            });
            return;
        }
        this.stage("ias.login.services", new Object[0]);
        if (changed) {
            try {
                IAS.disclaimersStorage();
                IAS.saveStorage();
            }
            catch (Throwable t) {
                LOGGER.error("IAS: Unable to save storage.", t);
            }
        }
        ((CompletableFuture)IASMinecraft.account(this.field_22787, data).thenRunAsync(() -> {
            if (this != this.field_22787.field_1755) {
                return;
            }
            this.field_22787.method_1507(this.parent);
        }, (Executor)this.field_22787)).exceptionally(ex -> {
            this.error(new RuntimeException("Unable to change account.", (Throwable)ex));
            return null;
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void error(Throwable error) {
        assert (this.field_22787 != null);
        LOGGER.error("IAS: Login error.", error);
        if (this != this.field_22787.field_1755) {
            return;
        }
        FriendlyException probable = FriendlyException.friendlyInChain(error);
        String key = probable != null ? probable.key() : "ias.error";
        class_5250 component = class_2561.method_43471((String)key).method_27692(class_124.field_1061);
        Object object = this.lock;
        synchronized (object) {
            this.stage = component;
            this.label = null;
            this.error = 0.0f;
        }
    }

    public String toString() {
        return "LoginPopupScreen{stage=" + String.valueOf(this.stage) + ", label=" + String.valueOf(this.label) + "}";
    }
}

