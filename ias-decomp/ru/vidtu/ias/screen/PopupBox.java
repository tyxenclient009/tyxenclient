/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_2561
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_342
 *  net.minecraft.class_5250
 *  org.jetbrains.annotations.NotNull
 */
package ru.vidtu.ias.screen;

import net.minecraft.class_11908;
import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_5250;
import org.jetbrains.annotations.NotNull;

final class PopupBox
extends class_342 {
    private final Runnable enterAction;
    private final boolean secure;

    PopupBox(class_327 font, int x, int y, int width, int height, PopupBox inherit, class_2561 title, Runnable enterAction, boolean secure) {
        super(font, x, y, width, height, (class_342)inherit, title);
        this.enterAction = enterAction;
        this.secure = secure;
    }

    public void method_48579(class_332 graphics, int mouseX, int mouseY, float delta) {
        int x = this.method_46426();
        int y = this.method_46427();
        int width = this.method_25368();
        int height = this.method_25364();
        graphics.method_25294(x + 1, y + 1, x + width - 1, y + height - 1, -16777216);
        graphics.method_25294(x + 1, y, x + width - 1, y + 1, -1);
        graphics.method_25294(x + 1, y + height - 1, x + width - 1, y + height, -1);
        graphics.method_25294(x, y + 1, x + 1, y + height - 1, -1);
        graphics.method_25294(x + width - 1, y + 1, x + width, y + height - 1, -1);
        super.method_48579(graphics, mouseX, mouseY, delta);
    }

    public boolean method_25404(class_11908 event) {
        int key = event.comp_4795();
        if (this.enterAction != null && this.method_37303() && this.method_25370() && (key == 257 || key == 335)) {
            this.enterAction.run();
            return true;
        }
        if (this.secure && (event.method_74242() || event.method_74244())) {
            return true;
        }
        return super.method_25404(event);
    }

    @NotNull
    protected class_5250 method_25360() {
        if (!this.secure) {
            return super.method_25360();
        }
        return class_2561.method_43469((String)"gui.narrate.editBox", (Object[])new Object[]{this.method_25369(), class_2561.method_43473()});
    }

    public boolean method_1851() {
        return false;
    }

    public String toString() {
        return "PopupBox{secure=" + this.secure + "}";
    }
}

