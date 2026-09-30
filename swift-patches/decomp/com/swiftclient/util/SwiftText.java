/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11719
 *  net.minecraft.class_11719$class_11721
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 */
package com.swiftclient.util;

import net.minecraft.class_11719;
import net.minecraft.class_2561;
import net.minecraft.class_2960;

public final class SwiftText {
    private static final class_11719.class_11721 FONT = new class_11719.class_11721(class_2960.method_60655((String)"swiftclient", (String)"swift"));
    private static final class_11719.class_11721 FONT_BOLD = new class_11719.class_11721(class_2960.method_60655((String)"swiftclient", (String)"swiftbold"));

    private SwiftText() {
    }

    public static class_2561 of(String s) {
        return class_2561.method_43470((String)s).method_27694(style -> style.method_27704((class_11719)FONT));
    }

    public static class_2561 bold(String s) {
        return class_2561.method_43470((String)s).method_27694(style -> style.method_27704((class_11719)FONT_BOLD));
    }
}

