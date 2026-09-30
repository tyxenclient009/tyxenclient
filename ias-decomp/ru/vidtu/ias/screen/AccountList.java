/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.yggdrasil.ProfileResult
 *  net.minecraft.class_1068
 *  net.minecraft.class_310
 *  net.minecraft.class_350$class_351
 *  net.minecraft.class_4280
 *  net.minecraft.class_437
 *  net.minecraft.class_8685
 *  org.jetbrains.annotations.Nullable
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package ru.vidtu.ias.screen;

import com.mojang.authlib.yggdrasil.ProfileResult;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import net.minecraft.class_1068;
import net.minecraft.class_310;
import net.minecraft.class_350;
import net.minecraft.class_4280;
import net.minecraft.class_437;
import net.minecraft.class_8685;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.account.OfflineAccount;
import ru.vidtu.ias.auth.LoginData;
import ru.vidtu.ias.config.IASStorage;
import ru.vidtu.ias.screen.AccountEntry;
import ru.vidtu.ias.screen.AccountScreen;
import ru.vidtu.ias.screen.AddPopupScreen;
import ru.vidtu.ias.screen.DeletePopupScreen;
import ru.vidtu.ias.screen.LoginPopupScreen;

final class AccountList
extends class_4280<AccountEntry> {
    private static final Map<UUID, class_8685> SKINS = new WeakHashMap<UUID, class_8685>(4);
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"IAS/AccountList");
    private final AccountScreen screen;

    AccountList(AccountScreen screen, class_310 minecraft, int width, int height, int offset, int item) {
        super(minecraft, width, height, offset, item);
        this.screen = screen;
        this.update(this.screen.search().method_1882());
    }

    public int method_25322() {
        return Math.min(super.method_25322(), this.screen.field_22789 - 190);
    }

    public void setSelected(@Nullable AccountEntry entry) {
        super.method_25313((class_350.class_351)entry);
        this.screen.updateSelected();
    }

    void update(String query) {
        if (query == null || query.isBlank()) {
            AccountEntry selected = (AccountEntry)this.method_25334();
            this.method_25314(IASStorage.ACCOUNTS.stream().map(account -> new AccountEntry(this.field_22740, this, (Account)account)).toList());
            this.setSelected(this.method_25396().contains((Object)selected) ? selected : null);
            this.screen.updateSelected();
            return;
        }
        String lowerQuery = query.toLowerCase(Locale.ROOT);
        AccountEntry selected = (AccountEntry)this.method_25334();
        this.method_25314(IASStorage.ACCOUNTS.stream().filter(account -> account.name().toLowerCase(Locale.ROOT).contains(lowerQuery)).sorted((f, s) -> Boolean.compare(s.name().toLowerCase(Locale.ROOT).startsWith(lowerQuery), f.name().toLowerCase(Locale.ROOT).startsWith(lowerQuery))).map(account -> new AccountEntry(this.field_22740, this, (Account)account)).toList());
        this.setSelected(this.method_25396().contains((Object)selected) ? selected : null);
        this.screen.updateSelected();
    }

    void login(boolean online, Runnable onComplete) {
        AccountEntry selected = (AccountEntry)this.method_25334();
        if (selected == null) {
            return;
        }
        Account account = selected.account();
        if (online && account.canLogin()) {
            LoginPopupScreen login = new LoginPopupScreen(this.screen);
            this.field_22740.method_1507((class_437)login);
            IAS.executor().execute(() -> account.login(login, onComplete));
            return;
        }
        LoginPopupScreen login = new LoginPopupScreen(this.screen);
        this.field_22740.method_1507((class_437)login);
        String name = account.name();
        LoginData data = new LoginData(name, OfflineAccount.uuid(name), "ias:offline", false);
        login.success(data, false);
        if (onComplete != null) {
            onComplete.run();
        }
    }

    void edit() {
        AccountEntry selected = (AccountEntry)this.method_25334();
        if (selected == null) {
            return;
        }
        int index = this.method_25396().indexOf((Object)selected);
        if (index < 0 || index >= IASStorage.ACCOUNTS.size()) {
            return;
        }
        this.field_22740.method_1507((class_437)new AddPopupScreen(this.screen, true, account -> {
            this.field_22740.method_1507((class_437)this.screen);
            IASStorage.ACCOUNTS.removeIf(Predicate.isEqual(account));
            if (index >= IASStorage.ACCOUNTS.size()) {
                IASStorage.ACCOUNTS.add((Account)account);
            } else {
                IASStorage.ACCOUNTS.set(index, (Account)account);
            }
            try {
                IAS.disclaimersStorage();
                IAS.saveStorage();
            }
            catch (Throwable t) {
                LOGGER.error("IAS: Unable to save storage.", t);
            }
            this.update(this.screen.search().method_1882());
        }));
    }

    void delete(boolean confirm) {
        AccountEntry selected = (AccountEntry)this.method_25334();
        if (selected == null) {
            return;
        }
        Account account = selected.account();
        if (!confirm) {
            IASStorage.ACCOUNTS.remove(account);
            try {
                IAS.disclaimersStorage();
                IAS.saveStorage();
            }
            catch (Throwable t) {
                LOGGER.error("IAS: Unable to save storage.", t);
            }
            this.update(this.screen.search().method_1882());
            return;
        }
        this.field_22740.method_1507((class_437)new DeletePopupScreen(this.screen, account, () -> {
            IASStorage.ACCOUNTS.removeIf(Predicate.isEqual(account));
            try {
                IAS.disclaimersStorage();
                IAS.saveStorage();
            }
            catch (Throwable t) {
                LOGGER.error("IAS: Unable to save storage.", t);
            }
            this.update(this.screen.search().method_1882());
        }));
    }

    void add() {
        this.field_22740.method_1507((class_437)new AddPopupScreen(this.screen, false, account -> {
            this.field_22740.method_1507((class_437)this.screen);
            IASStorage.ACCOUNTS.removeIf(Predicate.isEqual(account));
            IASStorage.ACCOUNTS.add((Account)account);
            try {
                IAS.disclaimersStorage();
                IAS.saveStorage();
            }
            catch (Throwable t) {
                LOGGER.error("IAS: Unable to save storage.", t);
            }
            this.update(this.screen.search().method_1882());
        }));
    }

    class_8685 skin(AccountEntry entry) {
        UUID uuid = entry.account().skin();
        class_8685 skin = SKINS.get(uuid);
        if (skin != null) {
            return skin;
        }
        skin = class_1068.method_4648((UUID)uuid);
        SKINS.put(uuid, skin);
        if (uuid.version() != 4) {
            return skin;
        }
        ((CompletableFuture)((CompletableFuture)CompletableFuture.supplyAsync(() -> {
            ProfileResult result = this.field_22740.method_73361().comp_837().fetchProfile(uuid, false);
            if (result == null) {
                return null;
            }
            return result.profile();
        }, IAS.executor()).thenComposeAsync(profile -> {
            if (profile == null) {
                return CompletableFuture.completedFuture(null);
            }
            return this.field_22740.method_1582().method_52863(profile);
        }, (Executor)IAS.executor())).thenAcceptAsync(loaded -> loaded.ifPresent(newSkin -> SKINS.put(uuid, (class_8685)newSkin)), (Executor)this.field_22740)).exceptionally(t -> {
            LOGGER.warn("IAS: Unable to load skin: {}", (Object)entry, t);
            return null;
        });
        return skin;
    }

    void swapUp(AccountEntry entry) {
        int idx = this.method_25396().indexOf((Object)entry);
        if (idx < 0 || idx >= IASStorage.ACCOUNTS.size()) {
            return;
        }
        int upIdx = idx - 1;
        if (upIdx < 0) {
            return;
        }
        IASStorage.ACCOUNTS.set(idx, IASStorage.ACCOUNTS.get(upIdx));
        IASStorage.ACCOUNTS.set(upIdx, entry.account());
        try {
            IAS.disclaimersStorage();
            IAS.saveStorage();
        }
        catch (Throwable t) {
            LOGGER.error("IAS: Unable to save storage.", t);
        }
        this.method_73368(idx, upIdx);
    }

    void swapDown(AccountEntry entry) {
        int idx = this.method_25396().indexOf((Object)entry);
        if (idx < 0 || idx >= IASStorage.ACCOUNTS.size()) {
            return;
        }
        int downIdx = idx + 1;
        if (downIdx >= this.method_25396().size() || downIdx >= IASStorage.ACCOUNTS.size()) {
            return;
        }
        IASStorage.ACCOUNTS.set(idx, IASStorage.ACCOUNTS.get(downIdx));
        IASStorage.ACCOUNTS.set(downIdx, entry.account());
        try {
            IAS.disclaimersStorage();
            IAS.saveStorage();
        }
        catch (Throwable t) {
            LOGGER.error("IAS: Unable to save storage.", t);
        }
        this.method_73368(idx, downIdx);
    }

    AccountScreen screen() {
        return this.screen;
    }

    public String toString() {
        return "AccountList{children=" + String.valueOf(this.method_25396()) + "}";
    }
}

