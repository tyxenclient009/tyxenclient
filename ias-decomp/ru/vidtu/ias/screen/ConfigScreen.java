/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1109
 *  net.minecraft.class_1113
 *  net.minecraft.class_124
 *  net.minecraft.class_2561
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_3414
 *  net.minecraft.class_3417
 *  net.minecraft.class_342
 *  net.minecraft.class_364
 *  net.minecraft.class_403
 *  net.minecraft.class_4185
 *  net.minecraft.class_4286
 *  net.minecraft.class_437
 *  net.minecraft.class_5244
 *  net.minecraft.class_7919
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package ru.vidtu.ias.screen;

import java.time.Duration;
import java.util.Objects;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_403;
import net.minecraft.class_4185;
import net.minecraft.class_4286;
import net.minecraft.class_437;
import net.minecraft.class_5244;
import net.minecraft.class_7919;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.config.IASConfig;
import ru.vidtu.ias.config.ServerMode;
import ru.vidtu.ias.config.TextAlign;
import ru.vidtu.ias.utils.Expression;

public final class ConfigScreen
extends class_437 {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"IAS/ConfigScreen");
    private final class_437 parent;
    private class_342 titleTextX;
    private class_342 titleTextY;
    private class_4185 titleTextAlign;
    private class_342 titleButtonX;
    private class_342 titleButtonY;
    private class_342 serversTextX;
    private class_342 serversTextY;
    private class_4185 serversTextAlign;
    private class_342 serversButtonX;
    private class_342 serversButtonY;

    public ConfigScreen(class_437 parent) {
        super((class_2561)class_2561.method_43471((String)"ias.config"));
        this.parent = parent;
    }

    protected void method_25426() {
        assert (this.field_22787 != null);
        if (IAS.disabled()) {
            this.field_22787.method_1507((class_437)new class_403(this::method_25419, (class_2561)class_2561.method_43471((String)"ias.disabled.title").method_27692(class_124.field_1061), (class_2561)class_2561.method_43471((String)"ias.disabled.text"), class_5244.field_24339, true));
            return;
        }
        class_4286 box = class_4286.method_54787((class_2561)class_2561.method_43471((String)"ias.config.titleText"), (class_327)this.field_22793).method_54789(5, 20).method_54794(IASConfig.titleText).method_54791((cb, value) -> {
            IASConfig.titleText = value;
            this.titleTextX.field_22763 = value;
            this.titleTextY.field_22763 = value;
            this.titleTextX.method_1888(value);
            this.titleTextY.method_1888(value);
            this.titleTextAlign.field_22763 = value;
        }).method_54793(class_7919.method_47407((class_2561)class_2561.method_43471((String)"ias.config.titleText.tip"))).method_54788();
        box.method_47402(Duration.ofMillis(250L));
        this.method_37063((class_364)box);
        this.titleTextX = new class_342(this.field_22793, 9 + box.method_25368(), 20, 75, 20, this.titleTextX, (class_2561)class_2561.method_43471((String)"ias.config.titleText.x"));
        this.titleTextX.method_47404((class_2561)this.titleTextX.method_25369().method_27661().method_27692(class_124.field_1063));
        this.titleTextX.method_47400(class_7919.method_47407((class_2561)class_2561.method_43469((String)"ias.config.titleText.x.tip", (Object[])new Object[]{class_2561.method_43471((String)"key.keyboard.left.alt")})));
        this.titleTextX.method_47402(Duration.ofMillis(250L));
        this.titleTextX.field_22763 = box.method_20372();
        this.titleTextX.method_1888(box.method_20372());
        this.titleTextX.method_1880(128);
        this.titleTextX.method_1863(value -> {
            IASConfig.titleTextX = value = value.isBlank() ? null : Expression.SPACE_PATTERN.matcher(value.strip()).replaceAll(" ");
            this.titleTextX.method_1868(Expression.positionValidityColor(value, this.field_22789, this.field_22790, true));
        });
        this.titleTextX.method_1852(Objects.requireNonNullElse(IASConfig.titleTextX, ""));
        this.method_37063((class_364)this.titleTextX);
        this.titleTextY = new class_342(this.field_22793, 88 + box.method_25368(), 20, 75, 20, this.titleTextY, (class_2561)class_2561.method_43471((String)"ias.config.titleText.y"));
        this.titleTextY.method_47404((class_2561)this.titleTextY.method_25369().method_27661().method_27692(class_124.field_1063));
        this.titleTextY.method_47400(class_7919.method_47407((class_2561)class_2561.method_43469((String)"ias.config.titleText.y.tip", (Object[])new Object[]{class_2561.method_43471((String)"key.keyboard.left.alt")})));
        this.titleTextY.method_47402(Duration.ofMillis(250L));
        this.titleTextY.field_22763 = box.method_20372();
        this.titleTextY.method_1888(box.method_20372());
        this.titleTextY.method_1880(128);
        this.titleTextY.method_1863(value -> {
            IASConfig.titleTextY = value = value.isBlank() ? null : Expression.SPACE_PATTERN.matcher(value.strip()).replaceAll(" ");
            this.titleTextY.method_1868(Expression.positionValidityColor(value, this.field_22789, this.field_22790, false));
        });
        this.titleTextY.method_1852(Objects.requireNonNullElse(IASConfig.titleTextY, ""));
        this.method_37063((class_364)this.titleTextY);
        this.titleTextAlign = class_4185.method_46430((class_2561)class_5244.method_32700((class_2561)class_2561.method_43471((String)"ias.config.titleTextAlign"), (class_2561)class_2561.method_43471((String)IASConfig.titleTextAlign.toString())), btn -> {
            IASConfig.titleTextAlign = switch (IASConfig.titleTextAlign) {
                default -> throw new MatchException(null, null);
                case TextAlign.LEFT -> TextAlign.CENTER;
                case TextAlign.CENTER -> TextAlign.RIGHT;
                case TextAlign.RIGHT -> TextAlign.LEFT;
            };
            btn.method_25355((class_2561)class_5244.method_32700((class_2561)class_2561.method_43471((String)"ias.config.titleTextAlign"), (class_2561)class_2561.method_43471((String)IASConfig.titleTextAlign.toString())));
        }).method_46434(167 + box.method_25368(), 20, Math.min(150, Math.max(20, this.field_22789 - 171 - box.method_25368())), 20).method_46431();
        this.titleTextAlign.field_22763 = box.method_20372();
        this.titleTextAlign.method_47400(class_7919.method_47407((class_2561)class_2561.method_43471((String)"ias.config.titleTextAlign.tip")));
        this.titleTextAlign.method_47402(Duration.ofMillis(250L));
        this.method_37063((class_364)this.titleTextAlign);
        box = class_4286.method_54787((class_2561)class_2561.method_43471((String)"ias.config.titleButton"), (class_327)this.field_22793).method_54789(5, 44).method_54794(IASConfig.titleButton).method_54791((cb, value) -> {
            IASConfig.titleButton = value;
            this.titleButtonX.field_22763 = value;
            this.titleButtonY.field_22763 = value;
            this.titleButtonX.method_1888(value);
            this.titleButtonY.method_1888(value);
        }).method_54793(class_7919.method_47407((class_2561)class_2561.method_43471((String)"ias.config.titleButton.tip"))).method_54788();
        box.method_47402(Duration.ofMillis(250L));
        this.method_37063((class_364)box);
        this.titleButtonX = new class_342(this.field_22793, 9 + box.method_25368(), 44, 75, 20, this.titleButtonX, (class_2561)class_2561.method_43471((String)"ias.config.titleButton.x"));
        this.titleButtonX.method_47404((class_2561)this.titleButtonX.method_25369().method_27661().method_27692(class_124.field_1063));
        this.titleButtonX.method_47400(class_7919.method_47407((class_2561)class_2561.method_43469((String)"ias.config.titleButton.x.tip", (Object[])new Object[]{class_2561.method_43471((String)"key.keyboard.left.alt")})));
        this.titleButtonX.method_47402(Duration.ofMillis(250L));
        this.titleButtonX.field_22763 = box.method_20372();
        this.titleButtonX.method_1888(box.method_20372());
        this.titleButtonX.method_1880(128);
        this.titleButtonX.method_1863(value -> {
            IASConfig.titleButtonX = value = value.isBlank() ? null : Expression.SPACE_PATTERN.matcher(value.strip()).replaceAll(" ");
            this.titleButtonX.method_1868(Expression.positionValidityColor(value, this.field_22789, this.field_22790, true));
        });
        this.titleButtonX.method_1852(Objects.requireNonNullElse(IASConfig.titleButtonX, ""));
        this.method_37063((class_364)this.titleButtonX);
        this.titleButtonY = new class_342(this.field_22793, 88 + box.method_25368(), 44, 75, 20, this.titleButtonY, (class_2561)class_2561.method_43471((String)"ias.config.titleButton.y"));
        this.titleButtonY.method_47404((class_2561)this.titleButtonY.method_25369().method_27661().method_27692(class_124.field_1063));
        this.titleButtonY.method_47400(class_7919.method_47407((class_2561)class_2561.method_43469((String)"ias.config.titleButton.y.tip", (Object[])new Object[]{class_2561.method_43471((String)"key.keyboard.left.alt")})));
        this.titleButtonY.method_47402(Duration.ofMillis(250L));
        this.titleButtonY.field_22763 = box.method_20372();
        this.titleButtonY.method_1888(box.method_20372());
        this.titleButtonY.method_1880(128);
        this.titleButtonY.method_1863(value -> {
            IASConfig.titleButtonY = value = value.isBlank() ? null : Expression.SPACE_PATTERN.matcher(value.strip()).replaceAll(" ");
            this.titleButtonY.method_1868(Expression.positionValidityColor(value, this.field_22789, this.field_22790, false));
        });
        this.titleButtonY.method_1852(Objects.requireNonNullElse(IASConfig.titleButtonY, ""));
        this.method_37063((class_364)this.titleButtonY);
        box = class_4286.method_54787((class_2561)class_2561.method_43471((String)"ias.config.serversText"), (class_327)this.field_22793).method_54789(5, 68).method_54794(IASConfig.serversText).method_54791((cb, value) -> {
            IASConfig.serversText = value;
            this.serversTextX.field_22763 = value;
            this.serversTextY.field_22763 = value;
            this.serversTextX.method_1888(value);
            this.serversTextY.method_1888(value);
            this.serversTextAlign.field_22763 = value;
        }).method_54793(class_7919.method_47407((class_2561)class_2561.method_43471((String)"ias.config.serversText.tip"))).method_54788();
        box.method_47402(Duration.ofMillis(250L));
        this.method_37063((class_364)box);
        this.serversTextX = new class_342(this.field_22793, 9 + box.method_25368(), 68, 75, 20, this.serversTextX, (class_2561)class_2561.method_43471((String)"ias.config.serversText.x"));
        this.serversTextX.method_47404((class_2561)this.serversTextX.method_25369().method_27661().method_27692(class_124.field_1063));
        this.serversTextX.method_47400(class_7919.method_47407((class_2561)class_2561.method_43469((String)"ias.config.serversText.x.tip", (Object[])new Object[]{class_2561.method_43471((String)"key.keyboard.left.alt")})));
        this.serversTextX.method_47402(Duration.ofMillis(250L));
        this.serversTextX.field_22763 = box.method_20372();
        this.serversTextX.method_1888(box.method_20372());
        this.serversTextX.method_1880(128);
        this.serversTextX.method_1863(value -> {
            IASConfig.serversTextX = value = value.isBlank() ? null : Expression.SPACE_PATTERN.matcher(value.strip()).replaceAll(" ");
            this.serversTextX.method_1868(Expression.positionValidityColor(value, this.field_22789, this.field_22790, true));
        });
        this.serversTextX.method_1852(Objects.requireNonNullElse(IASConfig.serversTextX, ""));
        this.method_37063((class_364)this.serversTextX);
        this.serversTextY = new class_342(this.field_22793, 88 + box.method_25368(), 68, 75, 20, this.serversTextY, (class_2561)class_2561.method_43471((String)"ias.config.serversText.y"));
        this.serversTextY.method_47404((class_2561)this.serversTextY.method_25369().method_27661().method_27692(class_124.field_1063));
        this.serversTextY.method_47400(class_7919.method_47407((class_2561)class_2561.method_43469((String)"ias.config.serversText.y.tip", (Object[])new Object[]{class_2561.method_43471((String)"key.keyboard.left.alt")})));
        this.serversTextY.method_47402(Duration.ofMillis(250L));
        this.serversTextY.field_22763 = box.method_20372();
        this.serversTextY.method_1888(box.method_20372());
        this.serversTextY.method_1880(128);
        this.serversTextY.method_1863(value -> {
            IASConfig.serversTextY = value = value.isBlank() ? null : Expression.SPACE_PATTERN.matcher(value.strip()).replaceAll(" ");
            this.serversTextY.method_1868(Expression.positionValidityColor(value, this.field_22789, this.field_22790, false));
        });
        this.serversTextY.method_1852(Objects.requireNonNullElse(IASConfig.serversTextY, ""));
        this.method_37063((class_364)this.serversTextY);
        this.serversTextAlign = class_4185.method_46430((class_2561)class_5244.method_32700((class_2561)class_2561.method_43471((String)"ias.config.serversTextAlign"), (class_2561)class_2561.method_43471((String)IASConfig.serversTextAlign.toString())), btn -> {
            IASConfig.serversTextAlign = switch (IASConfig.serversTextAlign) {
                default -> throw new MatchException(null, null);
                case TextAlign.LEFT -> TextAlign.CENTER;
                case TextAlign.CENTER -> TextAlign.RIGHT;
                case TextAlign.RIGHT -> TextAlign.LEFT;
            };
            btn.method_25355((class_2561)class_5244.method_32700((class_2561)class_2561.method_43471((String)"ias.config.serversTextAlign"), (class_2561)class_2561.method_43471((String)IASConfig.serversTextAlign.toString())));
        }).method_46434(167 + box.method_25368(), 68, Math.min(150, Math.max(20, this.field_22789 - 171 - box.method_25368())), 20).method_46431();
        this.serversTextAlign.field_22763 = box.method_20372();
        this.serversTextAlign.method_47400(class_7919.method_47407((class_2561)class_2561.method_43471((String)"ias.config.serversTextAlign.tip")));
        this.serversTextAlign.method_47402(Duration.ofMillis(250L));
        this.method_37063((class_364)this.serversTextAlign);
        box = class_4286.method_54787((class_2561)class_2561.method_43471((String)"ias.config.serversButton"), (class_327)this.field_22793).method_54789(5, 92).method_54794(IASConfig.serversButton).method_54791((cb, value) -> {
            IASConfig.serversButton = value;
            this.serversButtonX.field_22763 = value;
            this.serversButtonY.field_22763 = value;
            this.serversButtonX.method_1888(value);
            this.serversButtonY.method_1888(value);
        }).method_54793(class_7919.method_47407((class_2561)class_2561.method_43471((String)"ias.config.serversButton.tip"))).method_54788();
        box.method_47402(Duration.ofMillis(250L));
        this.method_37063((class_364)box);
        this.serversButtonX = new class_342(this.field_22793, 9 + box.method_25368(), 92, 75, 20, this.serversButtonX, (class_2561)class_2561.method_43471((String)"ias.config.serversButton.x"));
        this.serversButtonX.method_47404((class_2561)this.serversButtonX.method_25369().method_27661().method_27692(class_124.field_1063));
        this.serversButtonX.method_47400(class_7919.method_47407((class_2561)class_2561.method_43469((String)"ias.config.serversButton.x.tip", (Object[])new Object[]{class_2561.method_43471((String)"key.keyboard.left.alt")})));
        this.serversButtonX.method_47402(Duration.ofMillis(250L));
        this.serversButtonX.field_22763 = box.method_20372();
        this.serversButtonX.method_1888(box.method_20372());
        this.serversButtonX.method_1880(128);
        this.serversButtonX.method_1863(value -> {
            IASConfig.serversButtonX = value = value.isBlank() ? null : Expression.SPACE_PATTERN.matcher(value.strip()).replaceAll(" ");
            this.serversButtonX.method_1868(Expression.positionValidityColor(value, this.field_22789, this.field_22790, true));
        });
        this.serversButtonX.method_1852(Objects.requireNonNullElse(IASConfig.serversButtonX, ""));
        this.method_37063((class_364)this.serversButtonX);
        this.serversButtonY = new class_342(this.field_22793, 88 + box.method_25368(), 92, 75, 20, this.serversButtonY, (class_2561)class_2561.method_43471((String)"ias.config.serversButton.y"));
        this.serversButtonY.method_47404((class_2561)this.serversButtonY.method_25369().method_27661().method_27692(class_124.field_1063));
        this.serversButtonY.method_47400(class_7919.method_47407((class_2561)class_2561.method_43469((String)"ias.config.serversButton.y.tip", (Object[])new Object[]{class_2561.method_43471((String)"key.keyboard.left.alt")})));
        this.serversButtonY.method_47402(Duration.ofMillis(250L));
        this.serversButtonY.field_22763 = box.method_20372();
        this.serversButtonY.method_1888(box.method_20372());
        this.serversButtonY.method_1880(128);
        this.serversButtonY.method_1863(value -> {
            IASConfig.serversButtonY = value = value.isBlank() ? null : Expression.SPACE_PATTERN.matcher(value.strip()).replaceAll(" ");
            this.serversButtonY.method_1868(Expression.positionValidityColor(value, this.field_22789, this.field_22790, false));
        });
        this.serversButtonY.method_1852(Objects.requireNonNullElse(IASConfig.serversButtonY, ""));
        this.method_37063((class_364)this.serversButtonY);
        box = class_4286.method_54787((class_2561)class_2561.method_43471((String)"ias.config.allowNoCrypt"), (class_327)this.field_22793).method_54789(5, 116).method_54794(IASConfig.allowNoCrypt).method_54791((cb, value) -> {
            IASConfig.allowNoCrypt = value;
        }).method_54793(class_7919.method_47407((class_2561)class_2561.method_43471((String)"ias.config.allowNoCrypt.tip"))).method_54788();
        box.method_47402(Duration.ofMillis(250L));
        this.method_37063((class_364)box);
        class_4185 button = class_4185.method_46430((class_2561)class_5244.method_32700((class_2561)class_2561.method_43471((String)"ias.config.server"), (class_2561)class_2561.method_43471((String)IASConfig.server.toString())), btn -> {
            IASConfig.server = switch (IASConfig.server) {
                default -> throw new MatchException(null, null);
                case ServerMode.ALWAYS -> ServerMode.AVAILABLE;
                case ServerMode.AVAILABLE -> ServerMode.NEVER;
                case ServerMode.NEVER -> ServerMode.ALWAYS;
            };
            btn.method_25355((class_2561)class_5244.method_32700((class_2561)class_2561.method_43471((String)"ias.config.server"), (class_2561)class_2561.method_43471((String)IASConfig.server.toString())));
        }).method_46434(9 + box.method_25368(), 116, 200, 20).method_46436(class_7919.method_47407((class_2561)class_2561.method_43471((String)"ias.config.server.tip"))).method_46431();
        button.method_47402(Duration.ofMillis(250L));
        this.method_37063((class_364)button);
        box = class_4286.method_54787((class_2561)class_2561.method_43471((String)"ias.config.nickWarns"), (class_327)this.field_22793).method_54789(5, 140).method_54794(IASConfig.nickWarns).method_54791((cb, value) -> {
            IASConfig.nickWarns = value;
        }).method_54793(class_7919.method_47407((class_2561)class_2561.method_43471((String)"ias.config.nickWarns.tip"))).method_54788();
        box.method_47402(Duration.ofMillis(250L));
        this.method_37063((class_364)box);
        box = class_4286.method_54787((class_2561)class_2561.method_43471((String)"ias.config.passwordEchoing"), (class_327)this.field_22793).method_54789(10 + box.method_25368(), 140).method_54794(IASConfig.passwordEchoing).method_54791((cb, value) -> {
            IASConfig.passwordEchoing = value;
        }).method_54793(class_7919.method_47407((class_2561)class_2561.method_43471((String)"ias.config.passwordEchoing.tip"))).method_54788();
        box.method_47402(Duration.ofMillis(250L));
        this.method_37063((class_364)box);
        box = class_4286.method_54787((class_2561)class_2561.method_43471((String)"ias.config.unexpectedPigs"), (class_327)this.field_22793).method_54789(5, 164).method_54794(IASConfig.unexpectedPigs).method_54791((cb, value) -> {
            IASConfig.unexpectedPigs = value;
            this.field_22787.method_1483().method_4873((class_1113)class_1109.method_4758((class_3414)(value ? class_3417.field_14615 : class_3417.field_14689), (float)1.0f));
        }).method_54793(class_7919.method_47407((class_2561)class_2561.method_43471((String)"ias.config.unexpectedPigs.tip"))).method_54788();
        box.method_47402(Duration.ofMillis(250L));
        this.method_37063((class_364)box);
        box = class_4286.method_54787((class_2561)class_2561.method_43471((String)"ias.config.barNick"), (class_327)this.field_22793).method_54789(5, 188).method_54794(IASConfig.barNick).method_54791((cb, value) -> {
            IASConfig.barNick = value;
            this.field_22787.method_24288();
        }).method_54793(class_7919.method_47407((class_2561)class_2561.method_43471((String)"ias.config.barNick.tip"))).method_54788();
        box.method_47402(Duration.ofMillis(250L));
        this.method_37063((class_364)box);
        box = class_4286.method_54787((class_2561)class_2561.method_43471((String)"ias.config.closeOnLogin"), (class_327)this.field_22793).method_54789(5, 212).method_54794(IASConfig.closeOnLogin).method_54791((cb, value) -> {
            IASConfig.closeOnLogin = value;
        }).method_54793(class_7919.method_47407((class_2561)class_2561.method_43471((String)"ias.config.closeOnLogin.tip"))).method_54788();
        box.method_47402(Duration.ofMillis(250L));
        this.method_37063((class_364)box);
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_5244.field_24334, btn -> this.method_25419()).method_46434(this.field_22789 / 2 - 100, this.field_22790 - 24, 200, 20).method_46431());
    }

    public void method_25419() {
        assert (this.field_22787 != null);
        try {
            IAS.disclaimersStorage();
            IAS.saveConfig();
        }
        catch (Throwable t) {
            LOGGER.error("IAS: Unable to save config.", t);
        }
        this.field_22787.method_1507(this.parent);
    }

    public void method_25394(class_332 graphics, int mouseX, int mouseY, float delta) {
        super.method_25394(graphics, mouseX, mouseY, delta);
        graphics.method_27534(this.field_22793, this.field_22785, this.field_22789 / 2, 5, -1);
        if (this.field_22787.method_74189()) {
            graphics.method_51438(this.field_22793, (class_2561)class_2561.method_43469((String)"ias.config.mousePos", (Object[])new Object[]{mouseX, mouseY}), mouseX, mouseY);
        }
    }

    public String toString() {
        return "ConfigScreen{}";
    }
}

