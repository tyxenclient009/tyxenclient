/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.errorprone.annotations.CheckReturnValue
 *  org.jetbrains.annotations.Contract
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package ru.vidtu.ias.auth.microsoft;

import com.google.errorprone.annotations.CheckReturnValue;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.http.HttpTimeoutException;
import java.nio.channels.UnresolvedAddressException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.account.MicrosoftAccount;
import ru.vidtu.ias.auth.handlers.CreateHandler;
import ru.vidtu.ias.auth.microsoft.MSAuth;
import ru.vidtu.ias.auth.microsoft.fields.DeviceAuth;
import ru.vidtu.ias.auth.microsoft.fields.MSTokens;
import ru.vidtu.ias.crypt.Crypt;
import ru.vidtu.ias.utils.Holder;
import ru.vidtu.ias.utils.IUtils;
import ru.vidtu.ias.utils.exceptions.DevicePendingException;
import ru.vidtu.ias.utils.exceptions.FriendlyException;

public final class MSAuthClient
implements Closeable {
    @NotNull
    public static final Logger LOGGER = LoggerFactory.getLogger((String)"IAS/MSAuthClient");
    @NotNull
    private final Crypt crypt;
    @NotNull
    private final CreateHandler handler;
    private DeviceAuth auth;
    @Nullable
    private Instant expire;
    @Nullable
    private Instant poll;
    @Nullable
    private ScheduledFuture<?> task;

    @Contract(pure=true)
    public MSAuthClient(@NotNull Crypt crypt, @NotNull CreateHandler handler) {
        this.crypt = crypt;
        this.handler = handler;
    }

    @CheckReturnValue
    @NotNull
    public CompletableFuture<DeviceAuth> start() {
        if (this.handler.cancelled()) {
            return CompletableFuture.completedFuture(null);
        }
        return MSAuth.requestDac().thenApplyAsync(auth -> {
            if (this.handler.cancelled()) {
                return null;
            }
            LOGGER.info("IAS: Got device auth code.");
            Duration interval = auth.interval();
            this.auth = auth;
            this.expire = Instant.now().plus(auth.expire());
            this.poll = Instant.now().plus(interval);
            this.close();
            this.task = IAS.executor().scheduleWithFixedDelay(this::tick, interval.toMillis(), interval.toMillis(), TimeUnit.MILLISECONDS);
            LOGGER.info("IAS: HTTP polling started with delay of {}.", (Object)interval);
            return auth;
        }, (Executor)IAS.executor());
    }

    private void tick() {
        try {
            MSTokens ms;
            if (this.handler.cancelled()) {
                this.close();
                return;
            }
            try {
                ms = MSAuth.dacToMsaMsr(this.auth.device());
            }
            catch (Throwable t2) {
                if (IUtils.anyInCausalChain(t2, DevicePendingException.class::isInstance)) {
                    return;
                }
                this.close();
                this.handler.error(new RuntimeException("HTTP polling error.", t2));
                return;
            }
            this.close();
            if (this.handler.cancelled()) {
                return;
            }
            LOGGER.info("IAS: Processing response...");
            this.handler.stage("ias.login.processing", new Object[0]);
            Holder access = new Holder();
            Holder data = new Holder();
            ((CompletableFuture)((CompletableFuture)((CompletableFuture)((CompletableFuture)((CompletableFuture)((CompletableFuture)((CompletableFuture)CompletableFuture.completedFuture(null).thenComposeAsync(ignored -> {
                if (this.handler.cancelled()) {
                    return CompletableFuture.completedFuture(null);
                }
                LOGGER.info("IAS: Converting MSA to XBL...");
                this.handler.stage("ias.login.msaToXbl", new Object[0]);
                return MSAuth.msaToXbl(ms.access());
            }, (Executor)IAS.executor())).thenComposeAsync(xbl -> {
                if (xbl == null || this.handler.cancelled()) {
                    return CompletableFuture.completedFuture(null);
                }
                LOGGER.info("IAS: Converting XBL to XSTS...");
                this.handler.stage("ias.login.xblToXsts", new Object[0]);
                return MSAuth.xblToXsts(xbl.token(), xbl.hash());
            }, (Executor)IAS.executor())).thenComposeAsync(xsts -> {
                if (xsts == null || this.handler.cancelled()) {
                    return CompletableFuture.completedFuture(null);
                }
                LOGGER.info("IAS: Converting XSTS to MCA...");
                this.handler.stage("ias.login.xstsToMca", new Object[0]);
                return MSAuth.xstsToMca(xsts.token(), xsts.hash());
            }, (Executor)IAS.executor())).thenComposeAsync(token -> {
                if (token == null || this.handler.cancelled()) {
                    return CompletableFuture.completedFuture(null);
                }
                access.set(token);
                LOGGER.info("IAS: Converting MCA to MCP...");
                this.handler.stage("ias.login.mcaToMcp", new Object[0]);
                return MSAuth.mcaToMcp(token);
            }, (Executor)IAS.executor())).exceptionallyAsync(t -> {
                if (IUtils.anyInCausalChain(t, err -> err instanceof UnresolvedAddressException || err instanceof NoRouteToHostException || err instanceof HttpTimeoutException || err instanceof ConnectException)) {
                    throw new FriendlyException("Unable to connect to MS servers.", (Throwable)t, "ias.error.connect");
                }
                throw new RuntimeException("Unable to perform MS auth.", (Throwable)t);
            }, (Executor)IAS.executor())).thenApplyAsync(profile -> {
                byte[] unencrypted;
                DataOutputStream out;
                ByteArrayOutputStream byteOut;
                if (profile == null || this.handler.cancelled()) {
                    return null;
                }
                LOGGER.info("IAS: Encrypting tokens...");
                this.handler.stage("ias.login.encrypting", new Object[0]);
                try {
                    byteOut = new ByteArrayOutputStream();
                    try {
                        out = new DataOutputStream(byteOut);
                        try {
                            out.writeUTF((String)access.get());
                            out.writeUTF(ms.refresh());
                            unencrypted = byteOut.toByteArray();
                        }
                        finally {
                            out.close();
                        }
                    }
                    finally {
                        byteOut.close();
                    }
                }
                catch (Throwable t) {
                    throw new RuntimeException("Unable to write the tokens.", t);
                }
                try {
                    byteOut = new ByteArrayOutputStream();
                    try {
                        out = new DataOutputStream(byteOut);
                        try {
                            byte[] encrypted = this.crypt.encrypt(unencrypted);
                            out.writeUTF(this.crypt.type());
                            out.write(encrypted);
                            data.set(byteOut.toByteArray());
                        }
                        finally {
                            out.close();
                        }
                    }
                    finally {
                        byteOut.close();
                    }
                }
                catch (Throwable t) {
                    throw new RuntimeException("Unable to encrypt the tokens.", t);
                }
                return profile;
            }, (Executor)IAS.executor())).thenAcceptAsync(profile -> {
                if (profile == null || this.handler.cancelled()) {
                    return;
                }
                UUID uuid = profile.uuid();
                String name = profile.name();
                LOGGER.info("IAS: Successfully added {}", profile);
                this.handler.stage("ias.login.finalizing", new Object[0]);
                MicrosoftAccount account = new MicrosoftAccount(this.crypt.insecure(), uuid, name, (byte[])data.get());
                this.handler.success(account);
            }, (Executor)IAS.executor())).exceptionallyAsync(t -> {
                this.handler.error(new RuntimeException("Unable to create an MS account.", (Throwable)t));
                return null;
            }, (Executor)IAS.executor());
        }
        catch (Throwable t3) {
            this.handler.error(new RuntimeException("Unable to finalize MS auth.", t3));
        }
    }

    @Override
    public void close() {
        if (this.task != null) {
            this.task.cancel(false);
            this.task = null;
            LOGGER.info("IAS: HTTP polling stopped.");
        }
    }

    @Contract(pure=true)
    @NotNull
    public String toString() {
        return "MSAuthClient{crypt=" + String.valueOf(this.crypt) + ", expire=" + String.valueOf(this.expire) + ", poll=" + String.valueOf(this.poll) + "}";
    }
}

