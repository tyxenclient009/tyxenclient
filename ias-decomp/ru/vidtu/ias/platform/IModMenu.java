/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.errorprone.annotations.DoNotCall
 *  com.terraformersmc.modmenu.api.ConfigScreenFactory
 *  com.terraformersmc.modmenu.api.ModMenuApi
 *  org.jetbrains.annotations.ApiStatus$Internal
 *  org.jetbrains.annotations.Contract
 *  org.jspecify.annotations.NullMarked
 */
package ru.vidtu.ias.platform;

import com.google.errorprone.annotations.DoNotCall;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.ias.screen.ConfigScreen;

@NullMarked
@ApiStatus.Internal
public final class IModMenu
implements ModMenuApi {
    @Contract(pure=true)
    public IModMenu() {
    }

    @DoNotCall(value="Called by ModMenu")
    @Contract(pure=true)
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreen::new;
    }

    @Contract(pure=true)
    public String toString() {
        return "IAS/IModMenu{}";
    }
}

