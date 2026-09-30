/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.errorprone.annotations.DoNotCall
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
 *  net.fabricmc.fabric.api.client.screen.v1.Screens
 *  net.minecraft.class_327
 *  net.minecraft.class_437
 *  net.minecraft.class_442
 *  net.minecraft.class_500
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  org.jetbrains.annotations.ApiStatus$Internal
 *  org.jetbrains.annotations.Contract
 *  org.jspecify.annotations.NullMarked
 */
package ru.vidtu.ias.platform;

import com.google.errorprone.annotations.DoNotCall;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.class_327;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_500;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.IASMinecraft;

@NullMarked
@ApiStatus.Internal
public final class IFabric
implements ClientModInitializer {
    private static final Logger LOGGER = LogManager.getLogger((String)"IAS/IFabric");

    @Contract(pure=true)
    public IFabric() {
    }

    @DoNotCall(value="Called by Fabric")
    public void onInitializeClient() {
        long start = System.nanoTime();
        LOGGER.info("IAS: Loading... (platform: fabric)");
        IASMinecraft.init();
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> IAS.close());
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            IASMinecraft.onInit(client, screen, Screens.getButtons((class_437)screen)::add);
            if (screen instanceof class_442 || screen instanceof class_500) {
                class_327 font = client.field_1772;
                ScreenEvents.afterRender((class_437)screen).register((scr, graphics, mouseX, mouseY, delta) -> IASMinecraft.onDraw(scr, font, graphics));
            }
        });
        LOGGER.info("IAS: Loaded. ({} ms)", (Object)((System.nanoTime() - start) / 1000000L));
    }

    @Contract(pure=true)
    public String toString() {
        return "IAS/IFabric{}";
    }
}

