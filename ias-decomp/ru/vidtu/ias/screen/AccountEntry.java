/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10799
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_320
 *  net.minecraft.class_332
 *  net.minecraft.class_4280$class_4281
 *  net.minecraft.class_5244
 *  net.minecraft.class_5481
 *  net.minecraft.class_7532
 *  net.minecraft.class_8666
 *  net.minecraft.class_8685
 *  org.jetbrains.annotations.NotNull
 */
package ru.vidtu.ias.screen;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.class_10799;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_320;
import net.minecraft.class_332;
import net.minecraft.class_4280;
import net.minecraft.class_5244;
import net.minecraft.class_5481;
import net.minecraft.class_7532;
import net.minecraft.class_8666;
import net.minecraft.class_8685;
import org.jetbrains.annotations.NotNull;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.config.IASConfig;
import ru.vidtu.ias.platform.IStonecutter;
import ru.vidtu.ias.screen.AccountList;

final class AccountEntry
extends class_4280.class_4281<AccountEntry> {
    private static final class_8666 UP = new class_8666(IStonecutter.identifier("up_plain"), IStonecutter.identifier("up_disabled"), IStonecutter.identifier("up_focus"));
    private static final class_8666 DOWN = new class_8666(IStonecutter.identifier("down_plain"), IStonecutter.identifier("down_disabled"), IStonecutter.identifier("down_focus"));
    private static final class_8666 WARNING = new class_8666(IStonecutter.identifier("warning_off"), IStonecutter.identifier("warning_on"));
    private final class_310 minecraft;
    private final AccountList list;
    private final Account account;
    private final List<class_5481> tooltip;
    private long clicked = IStonecutter.internalMillisClock();
    private long lastFree = System.nanoTime();

    AccountEntry(class_310 minecraft, AccountList list, Account account) {
        this.minecraft = minecraft;
        this.list = list;
        this.account = account;
        this.tooltip = Stream.of(class_5244.method_32700((class_2561)class_2561.method_43471((String)"ias.accounts.tip.nick"), (class_2561)class_2561.method_43470((String)this.account.name())), class_5244.method_32700((class_2561)class_2561.method_43471((String)"ias.accounts.tip.uuid"), (class_2561)class_2561.method_43470((String)this.account.uuid().toString())), class_5244.method_32700((class_2561)class_2561.method_43471((String)"ias.accounts.tip.type"), (class_2561)class_2561.method_43471((String)this.account.typeTipKey()))).map(class_2561::method_30937).toList();
    }

    public void method_25343(class_332 graphics, int mouseX, int mouseY, boolean hovered, float delta) {
        if (hovered) {
            if (System.nanoTime() - this.lastFree >= 500000000L) {
                graphics.method_71274(this.tooltip, mouseX, mouseY);
            }
        } else {
            this.lastFree = System.nanoTime();
        }
        class_8685 skin = this.list.skin(this);
        int x = this.method_73380();
        int y = this.method_73382();
        int width = this.method_73387();
        int height = this.method_73384();
        class_7532.method_52722((class_332)graphics, (class_8685)skin, (int)x, (int)y, (int)8);
        class_320 user = this.minecraft.method_1548();
        int color = user == null || !this.account.name().equalsIgnoreCase(user.method_1676()) ? -1 : (this.account.uuid().equals(user.method_44717()) ? -16711936 : (this.account.name().equals(user.method_1676()) ? -256 : Short.MIN_VALUE));
        graphics.method_25303(this.minecraft.field_1772, this.account.name(), x + 10, y, color);
        if (this.account.insecure()) {
            boolean warning = System.nanoTime() / 1000000000L % 2L == 0L;
            graphics.method_52706(class_10799.field_56883, warning ? WARNING.comp_1604() : WARNING.comp_1606(), x - 6, y - 1, 2, 10);
            if (mouseX >= x - 10 && mouseX <= x && mouseY >= y && mouseY <= y + height) {
                graphics.method_71276((class_2561)class_2561.method_43471((String)"ias.accounts.tip.insecure"), mouseX, mouseY);
            }
        }
        if (this.equals(this.list.method_25336()) || this.equals(this.list.method_25334())) {
            int upX = x + width - 28;
            class_2960 upTexture = this == this.list.method_25396().getFirst() ? UP.comp_1605() : (mouseX >= upX && mouseY >= y && mouseX <= upX + 11 && mouseY <= y + height ? UP.comp_1606() : UP.comp_1604());
            graphics.method_52706(class_10799.field_56883, upTexture, upX, y, 11, 7);
            int downX = x + width - 15;
            class_2960 downTexture = this == this.list.method_25396().getLast() ? DOWN.comp_1605() : (mouseX >= downX && mouseY >= y && mouseX <= downX + 11 && mouseY <= y + height ? DOWN.comp_1606() : DOWN.comp_1604());
            graphics.method_52706(class_10799.field_56883, downTexture, downX, y, 11, 7);
        }
    }

    public boolean method_25402(class_11909 event, boolean doubleClick) {
        double mouseX = event.comp_4798();
        if (this.equals(this.list.method_25336()) || this.equals(this.list.method_25334())) {
            int right = this.list.method_31383();
            int upX = right - 28;
            if (mouseX >= (double)upX && mouseX <= (double)(upX + 11)) {
                this.list.swapUp(this);
                return true;
            }
            int downX = right - 15;
            if (mouseX >= (double)downX && mouseX <= (double)(downX + 11)) {
                this.list.swapDown(this);
                return true;
            }
        }
        if (IStonecutter.internalMillisClock() - this.clicked < 250L) {
            this.list.login(!event.method_74239(), IASConfig.closeOnLogin ? () -> this.minecraft.method_1507(this.list.screen().parent()) : null);
        }
        this.clicked = IStonecutter.internalMillisClock();
        return true;
    }

    @NotNull
    public class_2561 method_37006() {
        return class_2561.method_43470((String)this.account.name());
    }

    Account account() {
        return this.account;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AccountEntry)) {
            return false;
        }
        AccountEntry that = (AccountEntry)((Object)obj);
        return Objects.equals(this.account, that.account);
    }

    public int hashCode() {
        return Objects.hashCode(this.account);
    }

    public String toString() {
        return "AccountEntry{account=" + String.valueOf(this.account) + "}";
    }
}

