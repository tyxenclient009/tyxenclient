/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package ru.vidtu.ias.auth.handlers;

import org.jetbrains.annotations.NotNull;
import ru.vidtu.ias.account.MicrosoftAccount;

public interface CreateHandler {
    public boolean cancelled();

    public void stage(@NotNull String var1, Object ... var2);

    public void success(@NotNull MicrosoftAccount var1);

    public void error(@NotNull Throwable var1);
}

