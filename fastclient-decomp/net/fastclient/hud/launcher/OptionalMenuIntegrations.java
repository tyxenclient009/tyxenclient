/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.class_124
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_370
 *  net.minecraft.class_370$class_9037
 *  net.minecraft.class_374
 *  net.minecraft.class_403
 *  net.minecraft.class_410
 *  net.minecraft.class_433
 *  net.minecraft.class_437
 *  net.minecraft.class_5250
 */
package net.fastclient.hud.launcher;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fastclient.hud.FastClientHUDClient;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_370;
import net.minecraft.class_374;
import net.minecraft.class_403;
import net.minecraft.class_410;
import net.minecraft.class_433;
import net.minecraft.class_437;
import net.minecraft.class_5250;

@Environment(value=EnvType.CLIENT)
public final class OptionalMenuIntegrations {
    private static final String MOD_MENU_ID = "modmenu";
    private static final String FLASHBACK_ID = "flashback";
    private static final String MOD_MENU_API = "com.terraformersmc.modmenu.api.ModMenuApi";
    private static final String FLASHBACK_MAIN = "com.moulberry.flashback.Flashback";
    private static final String FLASHBACK_REPLAY_SCREEN = "com.moulberry.flashback.screen.select_replay.SelectReplayScreen";
    private static final boolean MOD_MENU_LOADED = FabricLoader.getInstance().isModLoaded("modmenu");
    private static final boolean FLASHBACK_LOADED = FabricLoader.getInstance().isModLoaded("flashback");
    private static Boolean modMenuAvailable;
    private static Method modMenuCreateScreenMethod;
    private static Boolean flashbackReplaysAvailable;
    private static Method flashbackIncompatibleModsMethod;
    private static Constructor<?> flashbackReplayScreenConstructor;
    private static Boolean flashbackRecordingAvailable;
    private static Class<?> flashbackClass;
    private static Field flashbackRecorderField;
    private static Method flashbackIsInReplayMethod;
    private static Method flashbackStartRecordingMethod;
    private static Method flashbackFinishRecordingMethod;
    private static Method flashbackPauseRecordingMethod;
    private static Method flashbackCancelRecordingMethod;
    private static Method recorderIsPausedMethod;

    private OptionalMenuIntegrations() {
    }

    public static boolean isModMenuAvailable() {
        if (!MOD_MENU_LOADED) {
            return false;
        }
        if (modMenuAvailable != null) {
            return modMenuAvailable;
        }
        try {
            Class<?> api = OptionalMenuIntegrations.loadOptionalClass(MOD_MENU_API);
            modMenuCreateScreenMethod = OptionalMenuIntegrations.requireStaticMethod(api, "createModsScreen", class_437.class, class_437.class);
            modMenuAvailable = true;
        }
        catch (Throwable error) {
            OptionalMenuIntegrations.disableModMenu("the expected screen API is unavailable", error);
        }
        return Boolean.TRUE.equals(modMenuAvailable);
    }

    public static boolean isFlashbackReplaysAvailable() {
        if (!FLASHBACK_LOADED) {
            return false;
        }
        if (flashbackReplaysAvailable != null) {
            return flashbackReplaysAvailable;
        }
        try {
            Class<?> mainClass = OptionalMenuIntegrations.flashbackClass();
            flashbackIncompatibleModsMethod = OptionalMenuIntegrations.requireStaticMethod(mainClass, "getReplayIncompatibleMods", List.class, new Class[0]);
            Class<?> replayScreenClass = OptionalMenuIntegrations.loadOptionalClass(FLASHBACK_REPLAY_SCREEN);
            if (!class_437.class.isAssignableFrom(replayScreenClass)) {
                throw new NoSuchMethodException("Flashback replay selector is not a Minecraft screen");
            }
            flashbackReplayScreenConstructor = replayScreenClass.getConstructor(class_437.class);
            OptionalMenuIntegrations.requireFlashbackResource("icon_pixelated.png");
            flashbackReplaysAvailable = true;
        }
        catch (Throwable error) {
            OptionalMenuIntegrations.disableFlashbackReplays("the expected replay API or icon is unavailable", error);
        }
        return Boolean.TRUE.equals(flashbackReplaysAvailable);
    }

    public static boolean isFlashbackRecordingAvailable() {
        if (!FLASHBACK_LOADED) {
            return false;
        }
        if (flashbackRecordingAvailable != null) {
            return flashbackRecordingAvailable;
        }
        try {
            Class<?> mainClass = OptionalMenuIntegrations.flashbackClass();
            flashbackRecorderField = mainClass.getField("RECORDER");
            if (!Modifier.isStatic(flashbackRecorderField.getModifiers())) {
                throw new NoSuchFieldException("Flashback.RECORDER is no longer static");
            }
            recorderIsPausedMethod = OptionalMenuIntegrations.requireInstanceMethod(flashbackRecorderField.getType(), "isPaused", Boolean.TYPE, new Class[0]);
            flashbackIsInReplayMethod = OptionalMenuIntegrations.requireStaticMethod(mainClass, "isInReplay", Boolean.TYPE, new Class[0]);
            flashbackStartRecordingMethod = OptionalMenuIntegrations.requireStaticMethod(mainClass, "startRecordingReplay", Void.TYPE, new Class[0]);
            flashbackFinishRecordingMethod = OptionalMenuIntegrations.requireStaticMethod(mainClass, "finishRecordingReplay", Void.TYPE, new Class[0]);
            flashbackPauseRecordingMethod = OptionalMenuIntegrations.requireStaticMethod(mainClass, "pauseRecordingReplay", Void.TYPE, Boolean.TYPE);
            flashbackCancelRecordingMethod = OptionalMenuIntegrations.requireStaticMethod(mainClass, "cancelRecordingReplay", Void.TYPE, new Class[0]);
            OptionalMenuIntegrations.requireFlashbackResource("icon_pixelated_start.png");
            OptionalMenuIntegrations.requireFlashbackResource("icon_pixelated_finish.png");
            OptionalMenuIntegrations.requireFlashbackResource("icon_pixelated_pause.png");
            OptionalMenuIntegrations.requireFlashbackResource("icon_pixelated_cancel.png");
            flashbackRecordingAvailable = true;
        }
        catch (Throwable error) {
            OptionalMenuIntegrations.disableFlashbackRecording("the expected recording API or icons are unavailable", error);
        }
        return Boolean.TRUE.equals(flashbackRecordingAvailable);
    }

    public static boolean openModMenu(class_437 parent) {
        if (!OptionalMenuIntegrations.isModMenuAvailable()) {
            return false;
        }
        try {
            Object result = modMenuCreateScreenMethod.invoke(null, parent);
            if (!(result instanceof class_437)) {
                throw new IllegalStateException("Mod Menu returned a non-screen result");
            }
            class_437 screen = (class_437)result;
            class_310.method_1551().method_1507(screen);
            return true;
        }
        catch (Throwable error) {
            OptionalMenuIntegrations.disableModMenu("opening the Mods screen failed", error);
            OptionalMenuIntegrations.reportFailure("Mod Menu", "The incompatible Mods button was disabled", error);
            return false;
        }
    }

    public static boolean openFlashbackReplays(class_437 parent) {
        if (!OptionalMenuIntegrations.isFlashbackReplaysAvailable()) {
            return false;
        }
        try {
            class_310 minecraft = class_310.method_1551();
            List incompatibleMods = (List)flashbackIncompatibleModsMethod.invoke(null, new Object[0]);
            if (!minecraft.method_74187() && incompatibleMods != null && !incompatibleMods.isEmpty()) {
                String mods = String.join((CharSequence)", ", incompatibleMods);
                class_5250 description = class_2561.method_43471((String)"flashback.incompatible_with_viewing_description").method_10852((class_2561)class_2561.method_43470((String)mods).method_27692(class_124.field_1061));
                minecraft.method_1507((class_437)new class_403(() -> minecraft.method_1507(parent), (class_2561)class_2561.method_43471((String)"flashback.incompatible_with_viewing"), (class_2561)description));
                return true;
            }
            Object result = flashbackReplayScreenConstructor.newInstance(parent);
            if (!(result instanceof class_437)) {
                throw new IllegalStateException("Flashback returned a non-screen replay selector");
            }
            class_437 screen = (class_437)result;
            minecraft.method_1507(screen);
            return true;
        }
        catch (Throwable error) {
            OptionalMenuIntegrations.disableFlashbackReplays("opening the replay selector failed", error);
            OptionalMenuIntegrations.reportFailure("Flashback", "The incompatible Replays button was disabled", error);
            return false;
        }
    }

    public static FlashbackRecordingState getFlashbackRecordingState() {
        if (!OptionalMenuIntegrations.isFlashbackRecordingAvailable()) {
            return FlashbackRecordingState.HIDDEN;
        }
        try {
            if (((Boolean)flashbackIsInReplayMethod.invoke(null, new Object[0])).booleanValue()) {
                return FlashbackRecordingState.HIDDEN;
            }
            Object recorder = flashbackRecorderField.get(null);
            if (recorder == null) {
                return FlashbackRecordingState.READY;
            }
            Method isPaused = recorderIsPausedMethod;
            if (!isPaused.getDeclaringClass().isInstance(recorder)) {
                recorderIsPausedMethod = isPaused = OptionalMenuIntegrations.requireInstanceMethod(recorder.getClass(), "isPaused", Boolean.TYPE, new Class[0]);
            }
            return (Boolean)isPaused.invoke(recorder, new Object[0]) != false ? FlashbackRecordingState.PAUSED : FlashbackRecordingState.RECORDING;
        }
        catch (Throwable error) {
            OptionalMenuIntegrations.disableFlashbackRecording("reading the recording state failed", error);
            return FlashbackRecordingState.HIDDEN;
        }
    }

    public static boolean startFlashbackRecording() {
        if (!OptionalMenuIntegrations.isFlashbackRecordingAvailable()) {
            return false;
        }
        return OptionalMenuIntegrations.invokeFlashbackAndClosePause(flashbackStartRecordingMethod, new Object[0]);
    }

    public static boolean finishFlashbackRecording() {
        if (!OptionalMenuIntegrations.isFlashbackRecordingAvailable()) {
            return false;
        }
        return OptionalMenuIntegrations.invokeFlashbackAndClosePause(flashbackFinishRecordingMethod, new Object[0]);
    }

    public static boolean pauseFlashbackRecording(boolean paused) {
        if (!OptionalMenuIntegrations.isFlashbackRecordingAvailable()) {
            return false;
        }
        return OptionalMenuIntegrations.invokeFlashbackAndClosePause(flashbackPauseRecordingMethod, paused);
    }

    public static boolean confirmCancelFlashbackRecording() {
        if (!OptionalMenuIntegrations.isFlashbackRecordingAvailable()) {
            return false;
        }
        try {
            class_310 minecraft = class_310.method_1551();
            minecraft.method_1507((class_437)new class_410(confirmed -> {
                if (confirmed) {
                    if (OptionalMenuIntegrations.invokeFlashback(flashbackCancelRecordingMethod, new Object[0])) {
                        minecraft.method_1507(null);
                    }
                } else {
                    minecraft.method_1507((class_437)new class_433(true));
                }
            }, (class_2561)class_2561.method_43471((String)"flashback.confirm_cancel_recording"), (class_2561)class_2561.method_43471((String)"flashback.confirm_cancel_recording_description")));
            return true;
        }
        catch (Throwable error) {
            OptionalMenuIntegrations.disableFlashbackRecording("opening the cancel confirmation failed", error);
            OptionalMenuIntegrations.reportFailure("Flashback", "The incompatible recording buttons were disabled", error);
            return false;
        }
    }

    private static boolean invokeFlashbackAndClosePause(Method method, Object ... arguments) {
        if (!OptionalMenuIntegrations.invokeFlashback(method, arguments)) {
            return false;
        }
        class_310.method_1551().method_1507(null);
        return true;
    }

    private static boolean invokeFlashback(Method method, Object ... arguments) {
        if (!OptionalMenuIntegrations.isFlashbackRecordingAvailable()) {
            return false;
        }
        try {
            method.invoke(null, arguments);
            return true;
        }
        catch (Throwable error) {
            OptionalMenuIntegrations.disableFlashbackRecording("performing a recording action failed", error);
            OptionalMenuIntegrations.reportFailure("Flashback", "The incompatible recording buttons were disabled", error);
            return false;
        }
    }

    private static Class<?> flashbackClass() throws ClassNotFoundException {
        if (flashbackClass == null) {
            flashbackClass = OptionalMenuIntegrations.loadOptionalClass(FLASHBACK_MAIN);
        }
        return flashbackClass;
    }

    private static Class<?> loadOptionalClass(String className) throws ClassNotFoundException {
        return Class.forName(className, false, OptionalMenuIntegrations.class.getClassLoader());
    }

    private static Method requireStaticMethod(Class<?> owner, String name, Class<?> returnType, Class<?> ... parameterTypes) throws ReflectiveOperationException {
        Method method = owner.getMethod(name, parameterTypes);
        if (!Modifier.isStatic(method.getModifiers()) || !returnType.isAssignableFrom(method.getReturnType())) {
            throw new NoSuchMethodException(owner.getName() + "." + name + " has an incompatible signature");
        }
        return method;
    }

    private static Method requireInstanceMethod(Class<?> owner, String name, Class<?> returnType, Class<?> ... parameterTypes) throws ReflectiveOperationException {
        Method method = owner.getMethod(name, parameterTypes);
        if (Modifier.isStatic(method.getModifiers()) || !returnType.isAssignableFrom(method.getReturnType())) {
            throw new NoSuchMethodException(owner.getName() + "." + name + " has an incompatible signature");
        }
        return method;
    }

    private static void requireFlashbackResource(String path) {
        class_2960 id = class_2960.method_60655((String)FLASHBACK_ID, (String)path);
        if (class_310.method_1551().method_1478().method_14486(id).isEmpty()) {
            throw new IllegalStateException("Missing Flashback resource " + String.valueOf(id));
        }
    }

    private static void disableModMenu(String reason, Throwable error) {
        modMenuAvailable = false;
        OptionalMenuIntegrations.logDisabled("Mod Menu", reason, error);
    }

    private static void disableFlashbackReplays(String reason, Throwable error) {
        flashbackReplaysAvailable = false;
        OptionalMenuIntegrations.logDisabled("Flashback replay", reason, error);
    }

    private static void disableFlashbackRecording(String reason, Throwable error) {
        flashbackRecordingAvailable = false;
        OptionalMenuIntegrations.logDisabled("Flashback recording", reason, error);
    }

    private static void logDisabled(String integration, String reason, Throwable error) {
        FastClientHUDClient.LOGGER.warn("Disabled optional {} integration because {}", new Object[]{integration, reason, OptionalMenuIntegrations.unwrap(error)});
    }

    private static Throwable unwrap(Throwable error) {
        InvocationTargetException invocation;
        if (error instanceof InvocationTargetException && (invocation = (InvocationTargetException)error).getCause() != null) {
            return invocation.getCause();
        }
        return error;
    }

    private static void reportFailure(String integration, String message, Throwable error) {
        FastClientHUDClient.LOGGER.error("{} integration failed: {}", new Object[]{integration, message, OptionalMenuIntegrations.unwrap(error)});
        class_310 minecraft = class_310.method_1551();
        class_370.method_27024((class_374)minecraft.method_1566(), (class_370.class_9037)class_370.class_9037.field_47588, (class_2561)class_2561.method_43470((String)integration), (class_2561)class_2561.method_43470((String)message));
    }

    @Environment(value=EnvType.CLIENT)
    public static enum FlashbackRecordingState {
        HIDDEN,
        READY,
        RECORDING,
        PAUSED;

    }
}

