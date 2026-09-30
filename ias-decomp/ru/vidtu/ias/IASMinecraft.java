/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.minecraft.UserApiService
 *  com.mojang.authlib.minecraft.UserApiService$UserProperties
 *  com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_320
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_344
 *  net.minecraft.class_350
 *  net.minecraft.class_364
 *  net.minecraft.class_368
 *  net.minecraft.class_370
 *  net.minecraft.class_370$class_9037
 *  net.minecraft.class_374
 *  net.minecraft.class_412
 *  net.minecraft.class_4185
 *  net.minecraft.class_437
 *  net.minecraft.class_442
 *  net.minecraft.class_500
 *  net.minecraft.class_5348
 *  net.minecraft.class_5520
 *  net.minecraft.class_6628
 *  net.minecraft.class_7497
 *  net.minecraft.class_7569
 *  net.minecraft.class_7574
 *  net.minecraft.class_7853
 *  net.minecraft.class_7919
 *  net.minecraft.class_8021
 *  net.minecraft.class_8666
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package ru.vidtu.ias;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.io.File;
import java.net.Proxy;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_320;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_344;
import net.minecraft.class_350;
import net.minecraft.class_364;
import net.minecraft.class_368;
import net.minecraft.class_370;
import net.minecraft.class_374;
import net.minecraft.class_412;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_500;
import net.minecraft.class_5348;
import net.minecraft.class_5520;
import net.minecraft.class_6628;
import net.minecraft.class_7497;
import net.minecraft.class_7569;
import net.minecraft.class_7574;
import net.minecraft.class_7853;
import net.minecraft.class_7919;
import net.minecraft.class_8021;
import net.minecraft.class_8666;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.auth.LoginData;
import ru.vidtu.ias.config.IASConfig;
import ru.vidtu.ias.mixins.MinecraftAccessor;
import ru.vidtu.ias.platform.IStonecutter;
import ru.vidtu.ias.screen.AccountScreen;
import ru.vidtu.ias.utils.Expression;
import ru.vidtu.ias.utils.IUtils;
import ru.vidtu.ias.utils.exceptions.FriendlyException;

public final class IASMinecraft {
    public static final class_370.class_9037 NICK_WARN = new class_370.class_9037(10000L);
    public static final class_8666 BUTTON = new class_8666(IStonecutter.identifier("button_plain"), IStonecutter.identifier("button_disabled"), IStonecutter.identifier("button_focus"));
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"IAS/IASMinecraft");
    private static int textX;
    private static int textY;
    private static class_2561 text;

    private IASMinecraft() {
        throw new AssertionError((Object)"No instances.");
    }

    public static void init() {
        IAS.init(IStonecutter.GAME_DIRECTORY, IStonecutter.CONFIG_DIRECTORY);
    }

    public static void onInit(class_310 minecraft, class_437 screen, Consumer<class_4185> buttonAdder) {
        class_327 font;
        Integer cy;
        Integer cx;
        int height;
        int width;
        if (IASConfig.titleButton && screen instanceof class_442) {
            width = screen.field_22789;
            height = screen.field_22790;
            Integer x = Expression.parsePosition(IASConfig.titleButtonX, width, height);
            Integer y = Expression.parsePosition(IASConfig.titleButtonY, width, height);
            if (x == null || y == null) {
                x = width / 2 + 104;
                y = height / 4 + 72;
                for (int i = 0; i < 64; ++i) {
                    boolean overlapping = false;
                    for (class_364 child : screen.method_25396()) {
                        if (!(child instanceof class_8021)) continue;
                        class_8021 le = (class_8021)child;
                        if (child instanceof class_350) continue;
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
            class_344 button = new class_344(x.intValue(), y.intValue(), 20, 20, BUTTON, btn -> minecraft.method_1507((class_437)new AccountScreen(screen)), (class_2561)class_2561.method_43470((String)"In-Game Account Switcher"));
            button.method_47400(class_7919.method_47407((class_2561)button.method_25369()));
            button.method_47402(Duration.ofMillis(250L));
            buttonAdder.accept((class_4185)button);
        }
        if (IASConfig.titleText && screen instanceof class_442) {
            int n;
            width = screen.field_22789;
            height = screen.field_22790;
            cx = Expression.parsePosition(IASConfig.titleTextX, width, height);
            cy = Expression.parsePosition(IASConfig.titleTextY, width, height);
            font = minecraft.field_1772;
            class_320 user = minecraft.method_1548();
            text = class_2561.method_43469((String)"ias.title", (Object[])new Object[]{user != null ? user.method_1676() : "(broken by mods)"});
            if (cx == null || cy == null) {
                n = (width - font.method_27525((class_5348)text)) / 2;
            } else {
                switch (IASConfig.titleTextAlign) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case LEFT: {
                        n = cx;
                        break;
                    }
                    case CENTER: {
                        n = cx - font.method_27525((class_5348)text) / 2;
                        break;
                    }
                    case RIGHT: {
                        n = cx - font.method_27525((class_5348)text);
                    }
                }
            }
            textX = n;
            int n2 = textY = cx == null || cy == null ? height / 4 + 164 : cy;
        }
        if (IASConfig.serversText && screen instanceof class_500) {
            int n;
            width = screen.field_22789;
            height = screen.field_22790;
            cx = Expression.parsePosition(IASConfig.serversTextX, width, height);
            cy = Expression.parsePosition(IASConfig.serversTextY, width, height);
            font = minecraft.field_1772;
            class_320 user = minecraft.method_1548();
            text = class_2561.method_43469((String)"ias.title", (Object[])new Object[]{user != null ? user.method_1676() : "(broken by mods)"});
            if (cx == null || cy == null) {
                n = (width - font.method_27525((class_5348)text)) / 2;
            } else {
                switch (IASConfig.serversTextAlign) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case LEFT: {
                        n = cx;
                        break;
                    }
                    case CENTER: {
                        n = cx - font.method_27525((class_5348)text) / 2;
                        break;
                    }
                    case RIGHT: {
                        n = cx - font.method_27525((class_5348)text);
                    }
                }
            }
            textX = n;
            int n3 = textY = cx == null || cy == null ? 5 : cy;
        }
        if (!IASConfig.nickWarns || !(screen instanceof class_412) || minecraft.method_1566().method_1997(class_370.class, (Object)NICK_WARN) != null) {
            return;
        }
        class_320 user = minecraft.method_1548();
        String name = user != null ? user.method_1676() : "";
        String key = IUtils.warnKey(name);
        if (key == null) {
            return;
        }
        class_374 manager = minecraft.method_1566();
        manager.method_1999((class_368)class_370.method_29047((class_310)minecraft, (class_370.class_9037)NICK_WARN, (class_2561)class_2561.method_43470((String)"In-Game Account Switcher"), (class_2561)class_2561.method_43469((String)key, (Object[])new Object[]{name})));
    }

    public static void onDraw(class_437 screen, class_327 font, class_332 graphics) {
        if (IASConfig.titleText && screen instanceof class_442) {
            graphics.method_27535(font, text, textX, textY, -3372920);
        }
        if (IASConfig.serversText && screen instanceof class_500) {
            graphics.method_27535(font, text, textX, textY, -3372920);
        }
    }

    public static CompletableFuture<Void> account(class_310 minecraft, LoginData data) {
        LOGGER.info("IAS: Received login request: {}", (Object)data);
        if (minecraft.field_1724 != null || minecraft.field_1687 != null || minecraft.method_1562() != null || minecraft.method_1560() != null || minecraft.field_1761 != null || minecraft.method_47392()) {
            return CompletableFuture.failedFuture(new FriendlyException("Changing accounts in world.", "ias.error.world"));
        }
        return CompletableFuture.runAsync(() -> {
            UserApiService.UserProperties properties;
            LOGGER.info("IAS: Creating user...");
            boolean online = data.online();
            class_320 user = new class_320(data.name(), data.uuid(), data.token(), Optional.empty(), Optional.empty());
            YggdrasilAuthenticationService service = online ? new YggdrasilAuthenticationService(minecraft.method_1487()) : YggdrasilAuthenticationService.createOffline((Proxy)minecraft.method_1487());
            class_7497 services = class_7497.method_44143((YggdrasilAuthenticationService)service, (File)minecraft.field_1697);
            CompletableFuture<Object> profile = CompletableFuture.completedFuture(online ? services.comp_837().fetchProfile(data.uuid(), true) : null);
            MinecraftAccessor accessor = (MinecraftAccessor)minecraft;
            UserApiService apiService = online ? service.createUserApiService(data.token()) : UserApiService.OFFLINE;
            try {
                properties = apiService.fetchProperties();
            }
            catch (Throwable ignored) {
                properties = UserApiService.OFFLINE_PROPERTIES;
            }
            CompletableFuture<UserApiService.UserProperties> propertiesFuture = CompletableFuture.completedFuture(properties);
            class_5520 social = new class_5520(minecraft, apiService);
            class_6628 telemetry = new class_6628(minecraft, apiService, user);
            class_7853 keyPair = class_7853.method_46532((UserApiService)apiService, (class_320)user, (Path)minecraft.field_1697.toPath());
            class_7574 reporting = class_7574.method_44599((class_7569)class_7569.method_44586(), (UserApiService)apiService);
            minecraft.execute(() -> {
                LOGGER.info("IAS: Flushing user...");
                accessor.ias$services(services);
                accessor.ias$user(user);
                accessor.ias$profileFuture(profile);
                accessor.ias$userApiService(apiService);
                accessor.ias$userPropertiesFuture(propertiesFuture);
                accessor.ias$playerSocialManager(social);
                accessor.ias$telemetryManager(telemetry);
                accessor.ias$profileKeyPairManager(keyPair);
                accessor.ias$reportingContext(reporting);
                minecraft.method_24288();
                LOGGER.info("IAS: Flushed user.");
            });
        }, IAS.executor()).exceptionally(t -> {
            LOGGER.error("IAS: Unable to log in: {}.", (Object)data, t);
            throw new RuntimeException("Unable to change account to: " + String.valueOf(data), (Throwable)t);
        });
    }

    static {
        text = class_2561.method_43469((String)"ias.title", (Object[])new Object[]{"(not loaded for some reason)"});
    }
}

