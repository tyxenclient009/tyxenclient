/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Contract
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package ru.vidtu.ias.crypt;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.vidtu.ias.crypt.Crypt;

public final class DummyCrypt
implements Crypt {
    @NotNull
    public static final DummyCrypt INSTANCE = new DummyCrypt();

    @Contract(pure=true)
    private DummyCrypt() {
    }

    @Override
    @Contract(pure=true)
    @NotNull
    public String type() {
        return "ias:dummy_crypt_v1";
    }

    @Override
    @Contract(value="-> null", pure=true)
    @Nullable
    public Crypt migrate() {
        return null;
    }

    @Override
    @Contract(value="-> true", pure=true)
    public boolean insecure() {
        return true;
    }

    @Override
    @Contract(value="_ -> param1", pure=true)
    public byte @NotNull [] encrypt(byte @NotNull [] decrypted) {
        return decrypted;
    }

    @Override
    @Contract(value="_ -> param1", pure=true)
    public byte @NotNull [] decrypt(byte @NotNull [] encrypted) {
        return encrypted;
    }

    @Contract(value="null -> false", pure=true)
    public boolean equals(@Nullable Object obj) {
        return obj instanceof DummyCrypt;
    }

    @Contract(pure=true)
    public int hashCode() {
        return 158798543;
    }

    @Contract(pure=true)
    @NotNull
    public String toString() {
        return "DummyCrypt{}";
    }
}

