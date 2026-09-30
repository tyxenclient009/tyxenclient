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
package ru.vidtu.ias.account;

import com.google.errorprone.annotations.CheckReturnValue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.http.HttpTimeoutException;
import java.nio.channels.UnresolvedAddressException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.auth.LoginData;
import ru.vidtu.ias.auth.handlers.LoginHandler;
import ru.vidtu.ias.auth.microsoft.MSAuth;
import ru.vidtu.ias.crypt.Crypt;
import ru.vidtu.ias.utils.Holder;
import ru.vidtu.ias.utils.IUtils;
import ru.vidtu.ias.utils.exceptions.FriendlyException;

public final class MicrosoftAccount
implements Account {
    @NotNull
    public static final String INITIALIZING = "ias.login.initializing";
    @NotNull
    public static final String SERVER = "ias.login.server";
    @NotNull
    public static final String BROWSER = "ias.login.link";
    @NotNull
    public static final String CLIENT_BROWSER = "ias.login.linkClient";
    @NotNull
    public static final String PROCESSING = "ias.login.processing";
    @NotNull
    public static final String DECRYPTING = "ias.login.decrypting";
    @NotNull
    public static final String ENCRYPTING = "ias.login.encrypting";
    @NotNull
    public static final String SERVICES = "ias.login.services";
    @NotNull
    public static final String MSAC_TO_MSA_MSR = "ias.login.msacToMsaMsr";
    @NotNull
    public static final String MSR_TO_MSA_MSR = "ias.login.msrToMsaMsr";
    @NotNull
    public static final String MSA_TO_XBL = "ias.login.msaToXbl";
    @NotNull
    public static final String XBL_TO_XSTS = "ias.login.xblToXsts";
    @NotNull
    public static final String XSTS_TO_MCA = "ias.login.xstsToMca";
    @NotNull
    public static final String MCA_TO_MCP = "ias.login.mcaToMcp";
    @NotNull
    public static final String FINALIZING = "ias.login.finalizing";
    @NotNull
    public static final Logger LOGGER = LoggerFactory.getLogger((String)"IAS/MicrosoftAccount");
    private final boolean insecure;
    @NotNull
    private UUID uuid;
    @NotNull
    private String name;
    private byte @NotNull [] data;

    @Contract(pure=true)
    public MicrosoftAccount(boolean insecure, @NotNull UUID uuid, @NotNull String name, byte @NotNull [] data) {
        this.insecure = insecure;
        this.uuid = uuid;
        this.name = name;
        this.data = (byte[])data.clone();
    }

    @Override
    @Contract(pure=true)
    @NotNull
    public String type() {
        return "ias:microsoft_v1";
    }

    @Override
    @Contract(pure=true)
    @NotNull
    public String typeTipKey() {
        return "ias.accounts.tip.type.microsoft";
    }

    @Override
    @Contract(pure=true)
    @NotNull
    public UUID uuid() {
        return this.uuid;
    }

    @Override
    @Contract(pure=true)
    @NotNull
    public String name() {
        return this.name;
    }

    @Override
    @Contract(value="-> true", pure=true)
    public boolean canLogin() {
        return true;
    }

    @Override
    @Contract(pure=true)
    public boolean insecure() {
        return this.insecure;
    }

    @Override
    @Contract(pure=true)
    @NotNull
    public UUID skin() {
        return this.uuid;
    }

    @Override
    public void login(@NotNull LoginHandler handler, Runnable onComplete) {
        try {
            byte[] crypted;
            CompletableFuture<Crypt> future;
            if (handler.cancelled()) {
                return;
            }
            LOGGER.info("IAS: Logging (Microsoft) as {}/{}", (Object)this.uuid, (Object)this.name);
            handler.stage(INITIALIZING, new Object[0]);
            Holder crypt = new Holder();
            Holder access = new Holder();
            Holder refresh = new Holder();
            Holder<Boolean> recrypt = new Holder<Boolean>(false);
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(this.data);
                 DataInputStream in = new DataInputStream(byteIn);){
                future = Crypt.readType(in, handler::password);
                crypted = in.readAllBytes();
            }
            ((CompletableFuture)((CompletableFuture)((CompletableFuture)((CompletableFuture)future.thenApplyAsync(value -> {
                if (value == null || handler.cancelled()) {
                    return null;
                }
                LOGGER.info("IAS: Decrypting tokens...");
                handler.stage(DECRYPTING, new Object[0]);
                byte[] data = value.decrypt(crypted);
                Crypt migrate = value.migrate();
                if (migrate != null) {
                    crypt.set(migrate);
                    recrypt.set(true);
                } else {
                    crypt.set(value);
                }
                return data;
            }, (Executor)IAS.executor())).thenApplyAsync(value -> {
                if (value == null || handler.cancelled()) {
                    return false;
                }
                try (ByteArrayInputStream byteIn = new ByteArrayInputStream((byte[])value);){
                    Boolean bl;
                    try (DataInputStream in = new DataInputStream(byteIn);){
                        access.set(in.readUTF());
                        refresh.set(in.readUTF());
                        int available = in.available();
                        if (available != 0) {
                            throw new IOException("Leftover: " + in.available());
                        }
                        bl = true;
                    }
                    return bl;
                }
                catch (Throwable t) {
                    throw new RuntimeException("Unable to read the tokens.", t);
                }
            }, (Executor)IAS.executor())).thenComposeAsync(value -> {
                if (!value.booleanValue() || handler.cancelled()) {
                    return CompletableFuture.completedFuture(null);
                }
                LOGGER.info("IAS: Converting MCA to MCP... (stored)");
                handler.stage(MCA_TO_MCP, new Object[0]);
                return MSAuth.mcaToMcp((String)access.get()).exceptionallyComposeAsync(original -> {
                    if (handler.cancelled()) {
                        return CompletableFuture.completedFuture(null);
                    }
                    LOGGER.warn("IAS: MCA is (probably) expired. Refreshing...");
                    LOGGER.info("IAS: Converting MSR to MSA/MSR...");
                    handler.stage(MSR_TO_MSA_MSR, new Object[0]);
                    recrypt.set(true);
                    return ((CompletableFuture)((CompletableFuture)((CompletableFuture)((CompletableFuture)((CompletableFuture)MSAuth.msrToMsaMsr((String)refresh.get()).thenComposeAsync(ms -> {
                        if (ms == null || handler.cancelled()) {
                            return CompletableFuture.completedFuture(null);
                        }
                        refresh.set(ms.refresh());
                        LOGGER.info("IAS: Converting MSA to XBL...");
                        handler.stage(MSA_TO_XBL, new Object[0]);
                        return MSAuth.msaToXbl(ms.access());
                    }, (Executor)IAS.executor())).thenComposeAsync(xbl -> {
                        if (xbl == null || handler.cancelled()) {
                            return CompletableFuture.completedFuture(null);
                        }
                        LOGGER.info("IAS: Converting XBL to XSTS...");
                        handler.stage(XBL_TO_XSTS, new Object[0]);
                        return MSAuth.xblToXsts(xbl.token(), xbl.hash());
                    }, (Executor)IAS.executor())).thenComposeAsync(xsts -> {
                        if (xsts == null || handler.cancelled()) {
                            return CompletableFuture.completedFuture(null);
                        }
                        LOGGER.info("IAS: Converting XSTS to MCA...");
                        handler.stage(XSTS_TO_MCA, new Object[0]);
                        return MSAuth.xstsToMca(xsts.token(), xsts.hash());
                    }, (Executor)IAS.executor())).thenComposeAsync(token -> {
                        if (token == null || handler.cancelled()) {
                            return CompletableFuture.completedFuture(null);
                        }
                        access.set(token);
                        LOGGER.info("IAS: Converting MCA TO MCP... (refreshed)");
                        handler.stage(MCA_TO_MCP, new Object[0]);
                        return MSAuth.mcaToMcp(token);
                    }, (Executor)IAS.executor())).exceptionallyAsync(t -> {
                        t.addSuppressed((Throwable)original);
                        if (IUtils.anyInCausalChain(t, err -> err instanceof UnresolvedAddressException || err instanceof NoRouteToHostException || err instanceof HttpTimeoutException || err instanceof ConnectException)) {
                            throw new FriendlyException("Unable to connect to MSR servers.", (Throwable)t, "ias.error.connect");
                        }
                        throw new RuntimeException("Unable to perform MSR auth.", (Throwable)t);
                    }, (Executor)IAS.executor())).exceptionallyAsync(t -> {
                        t.addSuppressed((Throwable)original);
                        throw new RuntimeException("Unable to refresh MSR.", (Throwable)t);
                    }, (Executor)IAS.executor());
                }, (Executor)IAS.executor());
            }, (Executor)IAS.executor())).thenAcceptAsync(profile -> {
                if (profile == null || handler.cancelled()) {
                    return;
                }
                boolean saveStorage = false;
                if (((Boolean)recrypt.get()).booleanValue()) {
                    byte[] unencrypted;
                    DataOutputStream out;
                    ByteArrayOutputStream byteOut;
                    LOGGER.info("IAS: Encrypting tokens...");
                    handler.stage(ENCRYPTING, new Object[0]);
                    String accessMem = (String)access.get();
                    String refreshMem = (String)refresh.get();
                    try {
                        byteOut = new ByteArrayOutputStream(accessMem.length() + refreshMem.length() + 4);
                        try {
                            out = new DataOutputStream(byteOut);
                            try {
                                out.writeUTF(accessMem);
                                out.writeUTF(refreshMem);
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
                        byteOut = new ByteArrayOutputStream(unencrypted.length + 32);
                        try {
                            out = new DataOutputStream(byteOut);
                            try {
                                Crypt val = (Crypt)crypt.get();
                                byte[] encrypted = val.encrypt(unencrypted);
                                out.writeUTF(val.type());
                                out.write(encrypted);
                                this.data = byteOut.toByteArray();
                                saveStorage = true;
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
                }
                UUID uuid = profile.uuid();
                String name = profile.name();
                if (!this.uuid.equals(uuid) || !this.name.equals(name)) {
                    this.uuid = profile.uuid();
                    this.name = profile.name();
                    saveStorage = true;
                }
                LOGGER.info("IAS: Successful login as {}", profile);
                handler.stage(FINALIZING, new Object[0]);
                LoginData login = new LoginData(this.name, this.uuid, (String)access.get(), true);
                handler.success(login, saveStorage);
                if (onComplete != null) {
                    onComplete.run();
                }
            }, (Executor)IAS.executor())).exceptionallyAsync(t -> {
                handler.error(new RuntimeException("Unable to login as MS account", (Throwable)t));
                return null;
            }, (Executor)IAS.executor());
        }
        catch (Throwable t2) {
            handler.error(new RuntimeException("Unable to begin MS auth.", t2));
        }
    }

    @Contract(value="null -> false", pure=true)
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MicrosoftAccount)) {
            return false;
        }
        MicrosoftAccount that = (MicrosoftAccount)obj;
        return Objects.equals(this.uuid, that.uuid) && Objects.equals(this.name, that.name);
    }

    @Contract(pure=true)
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + Objects.hashCode(this.uuid);
        hash = 31 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Contract(pure=true)
    @NotNull
    public String toString() {
        return "MicrosoftAccount{uuid=" + String.valueOf(this.uuid) + ", name='" + this.name + "', data='[DATA]'}";
    }

    @Override
    public void write(@NotNull DataOutput out) throws IOException {
        out.writeBoolean(this.insecure);
        out.writeLong(this.uuid.getMostSignificantBits());
        out.writeLong(this.uuid.getLeastSignificantBits());
        out.writeUTF(this.name);
        out.writeShort(this.data.length);
        out.write(this.data);
    }

    @CheckReturnValue
    @NotNull
    public static MicrosoftAccount read(@NotNull DataInput in) throws IOException {
        boolean insecure = in.readBoolean();
        UUID uuid = new UUID(in.readLong(), in.readLong());
        String name = in.readUTF();
        int length = in.readUnsignedShort();
        byte[] data = new byte[length];
        in.readFully(data);
        return new MicrosoftAccount(insecure, uuid, name, data);
    }
}

