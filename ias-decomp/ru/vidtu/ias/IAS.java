/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Contract
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package ru.vidtu.ias;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.config.IASConfig;
import ru.vidtu.ias.config.IASStorage;
import ru.vidtu.ias.utils.Holder;

public final class IAS {
    @NotNull
    public static final String CLIENT_ID = "54fd49e4-2103-4044-9603-2b028c814ec3";
    @NotNull
    public static final Duration TIMEOUT = Duration.ofSeconds(Long.getLong("ias.timeout", 15L));
    @NotNull
    public static final String USER_AGENT = "IAS/%s (https://github.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher; pig@vidtu.ru)".formatted(IAS.class.getPackage().getImplementationVersion());
    @NotNull
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"IAS");
    @Nullable
    private static ScheduledExecutorService executor;
    private static Path gameDirectory;
    private static Path configDirectory;
    private static boolean disabled;

    @Contract(value="-> fail", pure=true)
    private IAS() {
        throw new AssertionError((Object)"No instances.");
    }

    public static void init(@NotNull Path gamePath, @NotNull Path configPath) {
        LOGGER.info("IAS: Initializing IAS...");
        gameDirectory = gamePath;
        configDirectory = configPath;
        LOGGER.debug("IAS: Current user agent: {}", (Object)USER_AGENT);
        try {
            IAS.disclaimersStorage();
        }
        catch (Throwable t) {
            LOGGER.error("IAS: Unable to write disclaimers.", t);
        }
        try {
            IAS.loadConfig();
        }
        catch (Throwable t) {
            LOGGER.error("IAS: Unable to load IAS config.", t);
        }
        try {
            IAS.loadStorage();
        }
        catch (Throwable t) {
            LOGGER.error("IAS: Unable to load IAS storage.", t);
        }
        executor = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "IAS"));
        if (Boolean.getBoolean("ias.skipDisableScanning")) {
            LOGGER.debug("IAS: Skipped IAS remote scanning because system property is set.");
            return;
        }
        String version = String.valueOf(IAS.class.getPackage().getImplementationVersion());
        Holder task = new Holder();
        task.set(executor.scheduleWithFixedDelay(() -> {
            try {
                if (disabled || Boolean.getBoolean("ias.skipDisableScanning")) {
                    LOGGER.debug("IAS: Skipped IAS remote scanning because system property is set or the mod is already disabled.");
                    return;
                }
                HttpClient client = HttpClient.newBuilder().connectTimeout(TIMEOUT).version(HttpClient.Version.HTTP_2).followRedirects(HttpClient.Redirect.NORMAL).executor(Runnable::run).build();
                HttpResponse<Stream<String>> response = client.send(HttpRequest.newBuilder().uri(new URI("https://raw.githubusercontent.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/main/.ias/disabled_v1")).header("User-Agent", USER_AGENT).timeout(TIMEOUT).GET().build(), HttpResponse.BodyHandlers.ofLines());
                int code = response.statusCode();
                if (code < 200 || code > 299) {
                    return;
                }
                boolean bl = disabled = disabled || response.body().anyMatch(line -> "ALL".equalsIgnoreCase(line = line.strip()) || version.equalsIgnoreCase((String)line));
                if (!disabled) {
                    LOGGER.debug("IAS: Completed remote disabling check. Not disabled.");
                    return;
                }
                LOGGER.error("IAS: The In-Game Account Switcher mod has been disabled by remote due to serious issues. Please, see the mod page for more information. ({})", (Object)version);
                ScheduledFuture actualTask = (ScheduledFuture)task.get();
                if (actualTask == null) {
                    return;
                }
                actualTask.cancel(false);
            }
            catch (Throwable t) {
                LOGGER.debug("IAS: Unable to perform remote disabling check.", t);
            }
        }, 0L, 60L, TimeUnit.MINUTES));
        LOGGER.info("IAS: IAS has been loaded.");
    }

    public static void close() {
        LOGGER.info("IAS: Closing IAS...");
        try {
            ScheduledExecutorService executor = IAS.executor;
            if (executor != null) {
                LOGGER.info("IAS: Shutting down IAS executor...");
                executor.shutdown();
                if (executor.awaitTermination(30L, TimeUnit.SECONDS)) {
                    LOGGER.info("IAS: IAS executor shut down.");
                } else {
                    LOGGER.warn("IAS: Unable to shutdown IAS executor. Shutting down forcefully...");
                    executor.shutdownNow();
                    if (executor.awaitTermination(30L, TimeUnit.SECONDS)) {
                        LOGGER.info("IAS: IAS executor shut down forcefully.");
                    } else {
                        LOGGER.error("IAS: Unable to shutdown IAS executor forcefully.");
                    }
                }
            }
        }
        catch (InterruptedException e) {
            LOGGER.error("IAS: IAS executor interrupted while shutting down. Shutting down forcefully...", (Throwable)e);
            ScheduledExecutorService executor = IAS.executor;
            if (executor != null) {
                executor.shutdownNow();
            }
            Thread.currentThread().interrupt();
        }
        executor = null;
        if (gameDirectory != null) {
            try {
                IAS.disclaimersStorage();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        LOGGER.info("IAS: IAS has been unloaded.");
    }

    @Contract(pure=true)
    @NotNull
    public static ScheduledExecutorService executor() {
        ScheduledExecutorService executor = IAS.executor;
        Objects.requireNonNull(executor, "IAS executor is not available.");
        return executor;
    }

    @Contract(pure=true)
    public static boolean disabled() {
        return disabled;
    }

    public static void loadConfig() {
        IASConfig.load(configDirectory);
    }

    public static void saveConfig() {
        IASConfig.save(configDirectory);
    }

    public static void loadStorage() {
        IASStorage.load(gameDirectory);
    }

    public static void saveStorage() {
        IASStorage.save(gameDirectory);
    }

    public static void disclaimersStorage() {
        IASStorage.disclaimers(gameDirectory);
    }

    public static void gameDisclaimerShownStorage() {
        IASStorage.gameDisclaimerShown(gameDirectory);
    }

    static {
        disabled = false;
    }
}

