/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10034
 *  net.minecraft.class_10055
 *  net.minecraft.class_10186$class_10190
 *  net.minecraft.class_10192
 *  net.minecraft.class_10197
 *  net.minecraft.class_11659
 *  net.minecraft.class_1799
 *  net.minecraft.class_2960
 *  net.minecraft.class_3879
 *  net.minecraft.class_3883
 *  net.minecraft.class_3887
 *  net.minecraft.class_4587
 *  net.minecraft.class_5321
 *  net.minecraft.class_5599
 *  net.minecraft.class_5602
 *  net.minecraft.class_563
 *  net.minecraft.class_583
 *  net.minecraft.class_8685
 *  net.minecraft.class_9334
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10034;
import net.minecraft.class_10055;
import net.minecraft.class_10186;
import net.minecraft.class_10192;
import net.minecraft.class_10197;
import net.minecraft.class_11659;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_3879;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_5321;
import net.minecraft.class_5599;
import net.minecraft.class_5602;
import net.minecraft.class_563;
import net.minecraft.class_583;
import net.minecraft.class_8685;
import net.minecraft.class_9334;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class class_979<S extends class_10034, M extends class_583<S>>
extends class_3887<S, M> {
    private final class_563 field_4852;
    private final class_563 field_53215;
    private final class_10197 field_54185;

    public class_979(class_3883<S, M> arg, class_5599 arg2, class_10197 arg3) {
        super(arg);
        this.field_4852 = new class_563(arg2.method_32072(class_5602.field_27559));
        this.field_53215 = new class_563(arg2.method_32072(class_5602.field_52975));
        this.field_54185 = arg3;
    }

    public void method_17161(class_4587 arg, class_11659 arg2, int i, S arg3, float f, float g) {
        class_1799 lv = ((class_10034)arg3).field_53418;
        class_10192 lv2 = (class_10192)lv.method_58694(class_9334.field_54196);
        if (lv2 == null || lv2.comp_3176().isEmpty()) {
            return;
        }
        class_2960 lv3 = class_979.method_64084(arg3);
        class_563 lv4 = ((class_10034)arg3).field_53457 ? this.field_53215 : this.field_4852;
        arg.method_22903();
        arg.method_46416(0.0f, 0.0f, 0.125f);
        this.field_54185.method_64078(class_10186.class_10190.field_54127, (class_5321)lv2.comp_3176().get(), (class_3879)lv4, arg3, lv, arg, arg2, i, lv3, ((class_10034)arg3).field_61821, 0);
        arg.method_22909();
    }

    private static @Nullable class_2960 method_64084(class_10034 arg) {
        if (arg instanceof class_10055) {
            class_10055 lv = (class_10055)arg;
            class_8685 lv2 = lv.field_53520;
            if (lv2.comp_1628() != null) {
                return lv2.comp_1628().comp_3627();
            }
            if (lv2.comp_1627() != null && lv.field_53532) {
                return lv2.comp_1627().comp_3627();
            }
        }
        return null;
    }
}
