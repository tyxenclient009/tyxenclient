/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.errorprone.annotations.CheckReturnValue
 *  org.jetbrains.annotations.Contract
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package ru.vidtu.ias.crypt;

import com.google.errorprone.annotations.CheckReturnValue;
import java.io.DataInput;
import java.security.Key;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.crypt.DummyCrypt;
import ru.vidtu.ias.crypt.HardwareCrypt;
import ru.vidtu.ias.crypt.PasswordCrypt;
import ru.vidtu.ias.utils.exceptions.FriendlyException;

public sealed interface Crypt
permits DummyCrypt, HardwareCrypt, PasswordCrypt {
    @Contract(pure=true)
    public boolean insecure();

    @Contract(pure=true)
    @NotNull
    public String type();

    @Contract(pure=true)
    @Nullable
    public Crypt migrate();

    @Contract(pure=true)
    public byte @NotNull [] encrypt(byte @NotNull [] var1);

    @Contract(pure=true)
    public byte @NotNull [] decrypt(byte @NotNull [] var1);

    @CheckReturnValue
    @NotNull
    public static CompletableFuture<Crypt> readType(@NotNull DataInput input, @NotNull Supplier<CompletableFuture<String>> password) {
        try {
            String type;
            return switch (type = input.readUTF()) {
                case "ias:dummy_crypt_v1" -> CompletableFuture.completedFuture(DummyCrypt.INSTANCE);
                case "ias:hardware_crypt_v1" -> CompletableFuture.completedFuture(HardwareCrypt.INSTANCE_V1);
                case "ias:hardware_crypt_v2" -> CompletableFuture.completedFuture(HardwareCrypt.INSTANCE_V2);
                case "ias:password_crypt_v1" -> password.get().thenApplyAsync(pass -> pass == null ? null : new PasswordCrypt((String)pass), (Executor)IAS.executor());
                default -> CompletableFuture.failedFuture(new IllegalArgumentException("Unknown crypt type: " + type));
            };
        }
        catch (Throwable t) {
            return CompletableFuture.failedFuture(new RuntimeException("Unable to read typed crypt.", t));
        }
    }

    @Contract(pure=true)
    public static byte @NotNull [] pbkdfAesEncrypt(byte @NotNull [] decrypted, @NotNull String password, byte @NotNull [] salt, byte @NotNull [] iv) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 500000, 256);
            byte[] secret = factory.generateSecret(spec).getEncoded();
            SecretKeySpec key = new SecretKeySpec(secret, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(1, (Key)key, new GCMParameterSpec(128, iv));
            return cipher.doFinal(decrypted);
        }
        catch (Throwable t) {
            throw new RuntimeException("Unable to encrypt data using AES via PBKDF2-hashed password.", t);
        }
    }

    @Contract(pure=true)
    public static byte @NotNull [] pbkdfAesDecrypt(byte @NotNull [] encrypted, @NotNull String password, byte @NotNull [] salt, byte @NotNull [] iv) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 500000, 256);
            byte[] secret = factory.generateSecret(spec).getEncoded();
            SecretKeySpec key = new SecretKeySpec(secret, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(2, (Key)key, new GCMParameterSpec(128, iv));
            try {
                return cipher.doFinal(encrypted);
            }
            catch (AEADBadTagException e) {
                throw new FriendlyException("Unable to do AES final decrypt.", e, "ias.error.decrypt");
            }
        }
        catch (Throwable t) {
            throw new RuntimeException("Unable to decrypt data using AES via PBKDF2-hashed password.", t);
        }
    }
}

