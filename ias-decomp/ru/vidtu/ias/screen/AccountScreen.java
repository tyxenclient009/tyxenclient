/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1068
 *  net.minecraft.class_11908
 *  net.minecraft.class_124
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_342
 *  net.minecraft.class_364
 *  net.minecraft.class_403
 *  net.minecraft.class_4185
 *  net.minecraft.class_437
 *  net.minecraft.class_5244
 *  net.minecraft.class_7919
 *  net.minecraft.class_8765
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package ru.vidtu.ias.screen;

import java.time.Duration;
import java.util.UUID;
import net.minecraft.class_1068;
import net.minecraft.class_11908;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_403;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_5244;
import net.minecraft.class_7919;
import net.minecraft.class_8765;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.config.IASConfig;
import ru.vidtu.ias.config.IASStorage;
import ru.vidtu.ias.platform.IStonecutter;
import ru.vidtu.ias.screen.AccountEntry;
import ru.vidtu.ias.screen.AccountList;

public final class AccountScreen
extends class_437 {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"IAS/AccountScreen");
    private final class_437 parent;
    private class_342 search;
    private AccountList list;
    private class_8765 skin;
    private class_4185 login;
    private class_4185 offlineLogin;
    private class_4185 edit;
    private class_4185 delete;

    public AccountScreen(class_437 parent) {
        super((class_2561)class_2561.method_43471((String)"ias.accounts"));
        this.parent = parent;
    }

    protected void method_25426() {
        assert (this.field_22787 != null);
        if (IAS.disabled()) {
            this.field_22787.method_1507((class_437)new class_403(this::method_25419, (class_2561)class_2561.method_43471((String)"ias.disabled.title").method_27692(class_124.field_1061), (class_2561)class_2561.method_43471((String)"ias.disabled.text"), class_5244.field_24339, true));
            return;
        }
        if (!IASStorage.gameDisclaimerShown) {
            this.field_22787.method_1507((class_437)new class_403(() -> {
                try {
                    IAS.gameDisclaimerShownStorage();
                }
                catch (Throwable t) {
                    LOGGER.error("Unable to set or write game disclaimer state.", t);
                }
                this.field_22787.method_1507((class_437)this);
            }, (class_2561)class_2561.method_43471((String)"ias.disclaimer.title").method_27692(class_124.field_1054), (class_2561)class_2561.method_43471((String)"ias.disclaimer.text"), class_5244.field_41873, false));
            return;
        }
        this.search = new class_342(this.field_22793, this.field_22789 / 2 - 75, 11, 150, 20, this.search, (class_2561)class_2561.method_43471((String)"ias.accounts.search"));
        this.search.method_47404((class_2561)this.search.method_25369().method_27661().method_27692(class_124.field_1063));
        this.method_37063((class_364)this.search);
        if (this.skin == null) {
            this.skin = new class_8765(85, 120, this.field_22787.method_31974(), () -> {
                if (this.list == null) {
                    return class_1068.method_4648((UUID)IStonecutter.NIL_UUID);
                }
                AccountEntry selected = (AccountEntry)this.list.method_25334();
                if (selected == null) {
                    return class_1068.method_4648((UUID)IStonecutter.NIL_UUID);
                }
                return this.list.skin(selected);
            });
        }
        this.skin.method_48229(5, this.field_22790 / 2 - 60);
        this.method_37063((class_364)this.skin);
        this.login = class_4185.method_46430((class_2561)class_2561.method_43471((String)"ias.accounts.login"), btn -> this.list.login(true, IASConfig.closeOnLogin ? () -> this.field_22787.method_1507(this.parent) : null)).method_46434(this.field_22789 / 2 - 50 - 100 - 4, this.field_22790 - 24 - 24, 100, 20).method_46431();
        this.method_37063((class_364)this.login);
        this.offlineLogin = class_4185.method_46430((class_2561)class_2561.method_43471((String)"ias.accounts.offlineLogin"), btn -> this.list.login(false, IASConfig.closeOnLogin ? () -> this.field_22787.method_1507(this.parent) : null)).method_46434(this.field_22789 / 2 - 50 - 100 - 4, this.field_22790 - 24, 100, 20).method_46431();
        this.method_37063((class_364)this.offlineLogin);
        this.edit = class_4185.method_46430((class_2561)class_2561.method_43471((String)"ias.accounts.edit"), btn -> this.list.edit()).method_46434(this.field_22789 / 2 - 50, this.field_22790 - 24 - 24, 100, 20).method_46431();
        this.method_37063((class_364)this.edit);
        this.delete = class_4185.method_46430((class_2561)class_2561.method_43471((String)"ias.accounts.delete"), btn -> this.list.delete(!this.field_22787.method_74187())).method_46434(this.field_22789 / 2 - 50, this.field_22790 - 24, 100, 20).method_46431();
        this.method_37063((class_364)this.delete);
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43471((String)"ias.accounts.add"), btn -> this.list.add()).method_46434(this.field_22789 / 2 + 50 + 4, this.field_22790 - 24 - 24, 100, 20).method_46431());
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_5244.field_24339, btn -> this.field_22787.method_1507(this.parent)).method_46434(this.field_22789 / 2 + 50 + 4, this.field_22790 - 24, 100, 20).method_46431());
        if (this.list != null) {
            this.list.method_55444(this.field_22789, this.field_22790 - 24 - 24 - 4 - 34, 0, 34);
        } else {
            this.list = new AccountList(this, this.field_22787, this.field_22789, this.field_22790 - 24 - 24 - 4 - 34, 34, 12);
        }
        this.method_37063((class_364)this.list);
        this.search.method_1863(this.list::update);
        this.list.update(this.search.method_1882());
        this.updateSelected();
    }

    public void method_25419() {
        assert (this.field_22787 != null);
        this.field_22787.method_1507(this.parent);
    }

    public void method_25394(class_332 graphics, int mouseX, int mouseY, float delta) {
        super.method_25394(graphics, mouseX, mouseY, delta);
        graphics.method_27534(this.field_22793, this.field_22785, this.field_22789 / 2, 1, -1);
    }

    class_342 search() {
        return this.search;
    }

    void updateSelected() {
        AccountEntry selected;
        AccountEntry accountEntry = selected = this.list != null ? (AccountEntry)this.list.method_25334() : null;
        if (selected == null) {
            this.delete.field_22763 = false;
            this.edit.field_22763 = false;
            this.offlineLogin.field_22763 = false;
            this.login.field_22763 = false;
            this.login.method_47400(null);
            this.skin.field_22764 = false;
            return;
        }
        this.delete.field_22763 = true;
        this.edit.field_22763 = true;
        this.offlineLogin.field_22763 = true;
        if (selected.account().canLogin()) {
            this.login.field_22763 = true;
            this.login.method_47400(null);
        } else {
            this.login.field_22763 = false;
            this.login.method_47400(class_7919.method_47407((class_2561)class_2561.method_43471((String)"ias.accounts.login.offline")));
            this.login.method_47402(Duration.ZERO);
        }
        this.skin.field_22764 = true;
    }

    public boolean method_25404(class_11908 event) {
        AccountEntry selected;
        int key = event.comp_4795();
        boolean shift = event.method_74239();
        boolean control = event.method_74240();
        boolean select = event.method_74229();
        assert (this.field_22787 != null);
        if (key == 264 && shift || key == 267) {
            this.list.swapDown((AccountEntry)this.list.method_25334());
            return true;
        }
        if (key == 265 && shift || key == 266) {
            this.list.swapUp((AccountEntry)this.list.method_25334());
            return true;
        }
        if (key == 67 && control && (selected = (AccountEntry)this.list.method_25334()) != null) {
            Account account = selected.account();
            this.field_22787.field_1774.method_1455(shift ? account.uuid().toString() : account.name());
            return true;
        }
        if (super.method_25404(event)) {
            return true;
        }
        if (select) {
            this.list.login(!shift, IASConfig.closeOnLogin ? () -> this.field_22787.method_1507(this.parent) : null);
            return true;
        }
        if (key == 261 || key == 333) {
            this.list.delete(!shift);
            return true;
        }
        if (key == 78 && control || key == 334) {
            this.list.add();
            return true;
        }
        if (key == 82 && control || key == 332) {
            this.list.edit();
            return true;
        }
        return false;
    }

    class_437 parent() {
        return this.parent;
    }

    public String toString() {
        return "AccountScreen{list=" + String.valueOf((Object)this.list) + "}";
    }
}

