/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10017
 *  net.minecraft.class_10039
 *  net.minecraft.class_10428
 *  net.minecraft.class_10442
 *  net.minecraft.class_10444
 *  net.minecraft.class_11659
 *  net.minecraft.class_12075
 *  net.minecraft.class_1297
 *  net.minecraft.class_1542
 *  net.minecraft.class_238
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 *  net.minecraft.class_4608
 *  net.minecraft.class_5617$class_5618
 *  net.minecraft.class_5819
 *  net.minecraft.class_7833
 *  net.minecraft.class_897
 *  org.joml.Quaternionfc
 */
package net.minecraft;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10017;
import net.minecraft.class_10039;
import net.minecraft.class_10428;
import net.minecraft.class_10442;
import net.minecraft.class_10444;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_1297;
import net.minecraft.class_1542;
import net.minecraft.class_238;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4608;
import net.minecraft.class_5617;
import net.minecraft.class_5819;
import net.minecraft.class_7833;
import net.minecraft.class_897;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class class_916
extends class_897<class_1542, class_10039> {
    private static final float field_56954 = 0.0625f;
    private static final float field_32924 = 0.15f;
    private static final float field_56955 = 0.0625f;
    private final class_10442 field_55293;
    private final class_5819 field_4725 = class_5819.method_43047();

    public class_916(class_5617.class_5618 arg) {
        super(arg);
        this.field_55293 = arg.method_65566();
        this.field_4673 = 0.15f;
        this.field_4672 = 0.75f;
    }

    public class_10039 method_62469() {
        return new class_10039();
    }

    public void method_62470(class_1542 arg, class_10039 arg2, float f) {
        super.method_62354((class_1297)arg, (class_10017)arg2, f);
        arg2.field_53435 = arg.field_7203;
        arg2.method_65581((class_1297)arg, arg.method_6983(), this.field_55293);
    }

    public void method_3996(class_10039 arg, class_4587 arg2, class_11659 arg3, class_12075 arg4) {
        if (arg.field_55310.method_65606()) {
            return;
        }
        arg2.method_22903();
        class_238 lv = arg.field_55310.method_72173();
        float f = -((float)lv.field_1322) + 0.0625f;
        float g = class_3532.method_15374((double)(arg.field_53328 / 10.0f + arg.field_53435)) * 0.1f + 0.1f;
        arg2.method_46416(0.0f, g + f, 0.0f);
        float h = class_1542.method_27314((float)arg.field_53328, (float)arg.field_53435);
        arg2.method_22907((Quaternionfc)class_7833.field_40716.rotation(h));
        class_916.method_72986(arg2, arg3, arg.field_61820, (class_10428)arg, this.field_4725, lv);
        arg2.method_22909();
        super.method_3936((class_10017)arg, arg2, arg3, arg4);
    }

    public static void method_67984(class_4587 arg, class_11659 arg2, int i, class_10428 arg3, class_5819 arg4) {
        class_916.method_72986(arg, arg2, i, arg3, arg4, arg3.field_55310.method_72173());
    }

    public static void method_72986(class_4587 arg, class_11659 arg2, int i, class_10428 arg3, class_5819 arg4, class_238 arg5) {
        int j = arg3.field_55311;
        if (j == 0) {
            return;
        }
        arg4.method_43052((long)arg3.field_55312);
        class_10444 lv = arg3.field_55310;
        float f = (float)arg5.method_17941();
        if (f > 0.0625f) {
            lv.method_65604(arg, arg2, i, class_4608.field_21444, arg3.field_61821);
            for (int k = 1; k < j; ++k) {
                arg.method_22903();
                float g = (arg4.method_43057() * 2.0f - 1.0f) * 0.15f;
                float h = (arg4.method_43057() * 2.0f - 1.0f) * 0.15f;
                float l = (arg4.method_43057() * 2.0f - 1.0f) * 0.15f;
                arg.method_46416(g, h, l);
                lv.method_65604(arg, arg2, i, class_4608.field_21444, arg3.field_61821);
                arg.method_22909();
            }
        } else {
            float m = f * 1.5f;
            arg.method_46416(0.0f, 0.0f, -(m * (float)(j - 1) / 2.0f));
            lv.method_65604(arg, arg2, i, class_4608.field_21444, arg3.field_61821);
            arg.method_46416(0.0f, 0.0f, m);
            for (int n = 1; n < j; ++n) {
                arg.method_22903();
                float h = (arg4.method_43057() * 2.0f - 1.0f) * 0.15f * 0.5f;
                float l = (arg4.method_43057() * 2.0f - 1.0f) * 0.15f * 0.5f;
                arg.method_46416(h, l, 0.0f);
                lv.method_65604(arg, arg2, i, class_4608.field_21444, arg3.field_61821);
                arg.method_22909();
                arg.method_46416(0.0f, 0.0f, m);
            }
        }
    }

    public static void method_56858(class_4587 arg, class_11659 arg2, int i, class_10428 arg3, class_5819 arg4) {
        class_238 lv = arg3.field_55310.method_72173();
        int j = arg3.field_55311;
        if (j == 0) {
            return;
        }
        arg4.method_43052((long)arg3.field_55312);
        class_10444 lv2 = arg3.field_55310;
        float f = (float)lv.method_17941();
        if (f > 0.0625f) {
            lv2.method_65604(arg, arg2, i, class_4608.field_21444, arg3.field_61821);
            for (int k = 1; k < j; ++k) {
                arg.method_22903();
                float g = (arg4.method_43057() * 2.0f - 1.0f) * 0.15f;
                float h = (arg4.method_43057() * 2.0f - 1.0f) * 0.15f;
                float l = (arg4.method_43057() * 2.0f - 1.0f) * 0.15f;
                arg.method_46416(g, h, l);
                lv2.method_65604(arg, arg2, i, class_4608.field_21444, arg3.field_61821);
                arg.method_22909();
            }
        } else {
            float m = f * 1.5f;
            arg.method_46416(0.0f, 0.0f, -(m * (float)(j - 1) / 2.0f));
            lv2.method_65604(arg, arg2, i, class_4608.field_21444, arg3.field_61821);
            arg.method_46416(0.0f, 0.0f, m);
            for (int n = 1; n < j; ++n) {
                arg.method_22903();
                float h = (arg4.method_43057() * 2.0f - 1.0f) * 0.15f * 0.5f;
                float l = (arg4.method_43057() * 2.0f - 1.0f) * 0.15f * 0.5f;
                arg.method_46416(h, l, 0.0f);
                lv2.method_65604(arg, arg2, i, class_4608.field_21444, arg3.field_61821);
                arg.method_22909();
                arg.method_46416(0.0f, 0.0f, m);
            }
        }
    }

    public /* synthetic */ class_10017 method_55269() {
        return this.method_62469();
    }
}
