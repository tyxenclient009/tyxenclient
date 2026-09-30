/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.class_11735
 *  net.minecraft.class_156
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 *  net.minecraft.class_5489
 *  org.jetbrains.annotations.ApiStatus$Internal
 *  org.jetbrains.annotations.ApiStatus$ScheduledForRemoval
 *  org.jetbrains.annotations.Contract
 *  org.jspecify.annotations.NullMarked
 */
package ru.vidtu.ias.platform;

import java.nio.file.Path;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_11735;
import net.minecraft.class_156;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_5489;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

@NullMarked
@ApiStatus.Internal
public final class IStonecutter {
    public static final Path GAME_DIRECTORY = FabricLoader.getInstance().getGameDir();
    public static final Path CONFIG_DIRECTORY = FabricLoader.getInstance().getConfigDir();
    public static final UUID NIL_UUID = class_156.field_25140;

    @Deprecated
    @ApiStatus.ScheduledForRemoval
    @Contract(value="-> fail", pure=true)
    private IStonecutter() {
        throw new AssertionError((Object)"IAS: No instances.");
    }

    @Contract(pure=true)
    public static class_2960 identifier(String path) {
        assert (path != null) : "IAS: Parameter 'path' is null.";
        return class_2960.method_60655((String)"ias", (String)path);
    }

    @Contract(pure=true)
    public static long internalMillisClock() {
        return class_156.method_658();
    }

    public static void renderMultilineLabelCentered(class_5489 label, class_332 graphics, int x, int y) {
        label.method_75816(class_11735.field_62010, x, y, 9, graphics.method_75788());
    }

    public static void openUrl(String url) {
        class_156.method_668().method_670(url);
    }
}

