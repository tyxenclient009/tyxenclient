/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Contract
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package ru.vidtu.ias.crypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.security.SecureRandom;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.vidtu.ias.crypt.Crypt;

public final class PasswordCrypt
implements Crypt {
    @NotNull
    private final String password;

    @Contract(pure=true)
    public PasswordCrypt(@NotNull String password) {
        if (password.isBlank()) {
            throw new IllegalArgumentException("Password is blank.");
        }
        this.password = password;
    }

    @Override
    @Contract(pure=true)
    @NotNull
    public String type() {
        return "ias:password_crypt_v1";
    }

    @Override
    @Contract(value="-> null", pure=true)
    @Nullable
    public Crypt migrate() {
        return null;
    }

    @Override
    @Contract(value="-> false", pure=true)
    public boolean insecure() {
        return false;
    }

    @Override
    @Contract(pure=true)
    public byte @NotNull [] encrypt(byte @NotNull [] decrypted) {
        byte[] byArray;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            SecureRandom random = SecureRandom.getInstanceStrong();
            byte[] salt = new byte[128];
            random.nextBytes(salt);
            out.write(salt);
            byte[] iv = new byte[16];
            random.nextBytes(iv);
            out.write(iv);
            byte[] data = Crypt.pbkdfAesEncrypt(decrypted, this.password, salt, iv);
            out.write(data);
            byArray = out.toByteArray();
        }
        catch (Throwable throwable) {
            try {
                try {
                    out.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (Throwable t) {
                throw new RuntimeException("Unable to encrypt using PasswordCrypt.", t);
            }
        }
        out.close();
        return byArray;
    }

    @Override
    @Contract(pure=true)
    public byte @NotNull [] decrypt(byte @NotNull [] encrypted) {
        byte[] byArray;
        ByteArrayInputStream in = new ByteArrayInputStream(encrypted);
        try {
            byte[] salt = new byte[128];
            int read = in.read(salt);
            if (read != 128) {
                throw new EOFException("Not enough salt bytes: " + read);
            }
            byte[] iv = new byte[16];
            read = in.read(iv);
            if (read != 16) {
                throw new EOFException("Not enough IV bytes: " + read);
            }
            byte[] data = in.readAllBytes();
            byArray = Crypt.pbkdfAesDecrypt(data, this.password, salt, iv);
        }
        catch (Throwable throwable) {
            try {
                try {
                    in.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (Throwable t) {
                throw new RuntimeException("Unable to decrypt using PasswordCrypt.", t);
            }
        }
        in.close();
        return byArray;
    }

    @Contract(pure=true)
    @NotNull
    public String toString() {
        return "PasswordCrypt{password='[PASSWORD]'}";
    }
}

