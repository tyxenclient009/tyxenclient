/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.errorprone.annotations.CheckReturnValue
 *  org.jetbrains.annotations.NotNull
 */
package ru.vidtu.ias.auth.handlers;

import com.google.errorprone.annotations.CheckReturnValue;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import ru.vidtu.ias.auth.LoginData;

public interface LoginHandler {
    public boolean cancelled();

    public void stage(@NotNull String var1, Object ... var2);

    @CheckReturnValue
    @NotNull
    public CompletableFuture<String> password();

    public void success(@NotNull LoginData var1, boolean var2);

    public void error(@NotNull Throwable var1);
}

