/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Contract
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 *  org.jetbrains.annotations.Unmodifiable
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package ru.vidtu.ias.crypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.crypt.Crypt;

public final class HardwareCrypt
implements Crypt {
    @NotNull
    public static final HardwareCrypt INSTANCE_V2 = new HardwareCrypt(2, "ias:hardware_crypt_v2", null);
    @NotNull
    public static final HardwareCrypt INSTANCE_V1 = new HardwareCrypt(1, "ias:hardware_crypt_v1", INSTANCE_V2);
    @NotNull
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"IAS/HardwareCrypt");
    private static final byte @NotNull [] EMPTY_MAC = new byte[0];
    @NotNull
    private static final @Unmodifiable List<String> ENV = List.of("COMPUTERNAME", "PROCESSOR_ARCHITECTURE", "PROCESSOR_REVISION", "PROCESSOR_IDENTIFIER", "PROCESSOR_LEVEL", "NUMBER_OF_PROCESSORS", "OS", "USERNAME", "USERDOMAIN", "USERDOMAIN_ROAMINGPROFILE", "APPDATA", "HOMEPATH", "LOGONSERVER", "LOCALAPPDATA", "TEMP", "TMP", "MINECRAFT_IN_GAME_ACCOUNT_SWITCHER_VERY_NERDY_SYSTEM_ENV");
    @NotNull
    private static final @Unmodifiable List<String> PROPS = List.of("java.io.tmpdir", "native.encoding", "user.name", "user.home", "user.country", "sun.io.unicode.encoding", "stderr.encoding", "sun.cpu.endian", "sun.cpu.isalist", "sun.jnu.encoding", "stdout.encoding", "sun.arch.data.model", "user.language", "user.variant", "minecraft.inGameAccountSwitcher.veryNerdySystemProperty");
    private final int version;
    @NotNull
    private final String type;
    @Nullable
    private final HardwareCrypt migrate;

    @Contract(pure=true)
    private HardwareCrypt(int version, @NotNull String type, @Nullable HardwareCrypt migrate) {
        this.version = version;
        this.type = type;
        this.migrate = migrate;
    }

    @Override
    @Contract(pure=true)
    @NotNull
    public String type() {
        return this.type;
    }

    @Override
    @Contract(pure=true)
    @Nullable
    public HardwareCrypt migrate() {
        return this.migrate;
    }

    @Override
    @Contract(pure=true)
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
            String pwd = this.hardwarePassword();
            byte[] data = Crypt.pbkdfAesEncrypt(decrypted, pwd, salt, iv);
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
                throw new RuntimeException("Unable to encrypt using HardwareCrypt.", t);
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
            String pwd = this.hardwarePassword();
            byte[] data = in.readAllBytes();
            byArray = Crypt.pbkdfAesDecrypt(data, pwd, salt, iv);
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
                throw new RuntimeException("Unable to decrypt using HardwareCrypt.", t);
            }
        }
        in.close();
        return byArray;
    }

    @Contract(value="null -> false", pure=true)
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HardwareCrypt)) {
            return false;
        }
        HardwareCrypt that = (HardwareCrypt)obj;
        return this.version == that.version && Objects.equals(this.type, that.type) && Objects.equals(this.migrate, that.migrate);
    }

    @Contract(pure=true)
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + Integer.hashCode(this.version);
        hash = 31 * hash + Objects.hashCode(this.type);
        hash = 31 * hash + Objects.hashCode(this.migrate);
        return hash;
    }

    @Contract(pure=true)
    @NotNull
    public String toString() {
        return "HardwareCrypt{version=" + this.version + ", type='" + this.type + "', migrate=" + String.valueOf(this.migrate) + "}";
    }

    /*
     * Enabled aggressive exception aggregation
     */
    @Contract(pure=true)
    @NotNull
    private String hardwarePassword() {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();){
            String string;
            try (DataOutputStream out = new DataOutputStream(byteOut);){
                List<NetworkInterface> nets;
                String value;
                OperatingSystemMXBean system = ManagementFactory.getOperatingSystemMXBean();
                out.write(system.getArch().getBytes(StandardCharsets.UTF_8));
                out.writeInt(system.getAvailableProcessors());
                out.writeShort(File.separatorChar);
                out.writeShort(File.pathSeparatorChar);
                out.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
                for (String key : PROPS) {
                    value = System.getProperty(key);
                    if (value == null) continue;
                    out.write(value.getBytes(StandardCharsets.UTF_8));
                }
                for (String key : ENV) {
                    value = System.getenv(key);
                    if (value == null) continue;
                    out.write(value.getBytes(StandardCharsets.UTF_8));
                }
                try {
                    nets = NetworkInterface.networkInterfaces().toList();
                }
                catch (SocketException ignored) {
                    nets = List.of();
                }
                for (NetworkInterface net : nets) {
                    if (net.isVirtual() || net.isLoopback()) continue;
                    out.write(net.getName().getBytes(StandardCharsets.UTF_8));
                    String displayName = net.getDisplayName();
                    if (displayName != null) {
                        out.write(displayName.getBytes(StandardCharsets.UTF_8));
                    }
                    byte[] mac = EMPTY_MAC;
                    try {
                        mac = Objects.requireNonNullElse(net.getHardwareAddress(), EMPTY_MAC);
                    }
                    catch (SocketException e) {
                        LOGGER.trace("Unable to get MAC: {}", (Object)net, (Object)e);
                    }
                    out.write(mac);
                    try {
                        out.writeInt(net.getMTU());
                    }
                    catch (SocketException e) {
                        LOGGER.trace("Unable to get MTU: {}", (Object)net, (Object)e);
                    }
                }
                try {
                    Class<?> sysInfoClass = Class.forName("oshi.SystemInfo");
                    Class<?> osClass = Class.forName("oshi.software.os.OperatingSystem");
                    Class<?> hwLayerClass = Class.forName("oshi.hardware.HardwareAbstractionLayer");
                    Object sysInfo = sysInfoClass.getConstructor(new Class[0]).newInstance(new Object[0]);
                    Object os = sysInfoClass.getMethod("getOperatingSystem", new Class[0]).invoke(sysInfo, new Object[0]);
                    Object hwLayer = sysInfoClass.getMethod("getHardware", new Class[0]).invoke(sysInfo, new Object[0]);
                    Class<?> sysClass = Class.forName("oshi.hardware.ComputerSystem");
                    Object sys = hwLayerClass.getMethod("getComputerSystem", new Class[0]).invoke(hwLayer, new Object[0]);
                    Class<?> boardClass = Class.forName("oshi.hardware.Baseboard");
                    Object board = sysClass.getMethod("getBaseboard", new Class[0]).invoke(sys, new Object[0]);
                    Class<?> firmwareClass = Class.forName("oshi.hardware.Firmware");
                    Object firmware = sysClass.getMethod("getFirmware", new Class[0]).invoke(sys, new Object[0]);
                    Class<?> diskClass = Class.forName("oshi.hardware.HWDiskStore");
                    List disks = (List)hwLayerClass.getMethod("getDiskStores", new Class[0]).invoke(hwLayer, new Object[0]);
                    Method diskNameMethod = diskClass.getMethod("getName", new Class[0]);
                    Method diskModelMethod = diskClass.getMethod("getModel", new Class[0]);
                    Method diskSerialMethod = diskClass.getMethod("getSerial", new Class[0]);
                    Method diskSizeMethod = diskClass.getMethod("getSize", new Class[0]);
                    Class<?> cardClass = Class.forName("oshi.hardware.GraphicsCard");
                    List cards = (List)hwLayerClass.getMethod("getGraphicsCards", new Class[0]).invoke(hwLayer, new Object[0]);
                    Method cardNameMethod = cardClass.getMethod("getName", new Class[0]);
                    Method cardIdMethod = cardClass.getMethod("getDeviceId", new Class[0]);
                    Method cardVendorMethod = cardClass.getMethod("getVendor", new Class[0]);
                    Method cardRamMethod = cardClass.getMethod("getVRam", new Class[0]);
                    int osBit = (Integer)osClass.getMethod("getBitness", new Class[0]).invoke(os, new Object[0]);
                    String osFamily = (String)osClass.getMethod("getFamily", new Class[0]).invoke(os, new Object[0]);
                    String osManufacturer = (String)osClass.getMethod("getManufacturer", new Class[0]).invoke(os, new Object[0]);
                    out.writeInt(osBit);
                    out.write(osFamily.getBytes(StandardCharsets.UTF_8));
                    out.write(osManufacturer.getBytes(StandardCharsets.UTF_8));
                    String hwUid = (String)sysClass.getMethod("getHardwareUUID", new Class[0]).invoke(sys, new Object[0]);
                    String hwManufacturer = (String)sysClass.getMethod("getManufacturer", new Class[0]).invoke(sys, new Object[0]);
                    String hwModel = (String)sysClass.getMethod("getModel", new Class[0]).invoke(sys, new Object[0]);
                    out.write(hwUid.getBytes(StandardCharsets.UTF_8));
                    out.write(hwManufacturer.getBytes(StandardCharsets.UTF_8));
                    out.write(hwModel.getBytes(StandardCharsets.UTF_8));
                    String boardSerial = (String)boardClass.getMethod("getSerialNumber", new Class[0]).invoke(board, new Object[0]);
                    String boardManufacturer = (String)boardClass.getMethod("getManufacturer", new Class[0]).invoke(board, new Object[0]);
                    String boardModel = (String)boardClass.getMethod("getModel", new Class[0]).invoke(board, new Object[0]);
                    String boardVersion = (String)boardClass.getMethod("getVersion", new Class[0]).invoke(board, new Object[0]);
                    out.write(boardSerial.getBytes(StandardCharsets.UTF_8));
                    out.write(boardManufacturer.getBytes(StandardCharsets.UTF_8));
                    out.write(boardModel.getBytes(StandardCharsets.UTF_8));
                    out.write(boardVersion.getBytes(StandardCharsets.UTF_8));
                    String firmwareName = (String)firmwareClass.getMethod("getName", new Class[0]).invoke(firmware, new Object[0]);
                    String firmwareDescription = (String)firmwareClass.getMethod("getDescription", new Class[0]).invoke(firmware, new Object[0]);
                    String firmwareManufacturer = (String)firmwareClass.getMethod("getManufacturer", new Class[0]).invoke(firmware, new Object[0]);
                    out.write(firmwareName.getBytes(StandardCharsets.UTF_8));
                    out.write(firmwareDescription.getBytes(StandardCharsets.UTF_8));
                    out.write(firmwareManufacturer.getBytes(StandardCharsets.UTF_8));
                    for (Object disk : disks) {
                        String diskName = (String)diskNameMethod.invoke(disk, new Object[0]);
                        String diskModel = (String)diskModelMethod.invoke(disk, new Object[0]);
                        String diskSerial = (String)diskSerialMethod.invoke(disk, new Object[0]);
                        long diskSize = (Long)diskSizeMethod.invoke(disk, new Object[0]);
                        out.write(diskName.getBytes(StandardCharsets.UTF_8));
                        out.write(diskModel.getBytes(StandardCharsets.UTF_8));
                        out.write(diskSerial.getBytes(StandardCharsets.UTF_8));
                        out.writeLong(diskSize);
                    }
                    if (this.version < 2) {
                        for (Object card : cards) {
                            String cardName = (String)cardNameMethod.invoke(card, new Object[0]);
                            String cardId = (String)cardIdMethod.invoke(card, new Object[0]);
                            String cardVendor = (String)cardVendorMethod.invoke(card, new Object[0]);
                            long cardRam = (Long)cardRamMethod.invoke(card, new Object[0]);
                            out.write(cardName.getBytes(StandardCharsets.UTF_8));
                            out.write(cardId.getBytes(StandardCharsets.UTF_8));
                            out.write(cardVendor.getBytes(StandardCharsets.UTF_8));
                            out.writeLong(cardRam);
                        }
                    }
                }
                catch (Throwable t) {
                    LOGGER.trace("Unable to write OSHI data.", t);
                }
                string = Base64.getEncoder().encodeToString(byteOut.toByteArray());
            }
            return string;
        }
        catch (Throwable t) {
            throw new RuntimeException("Unable to create a hardware password.", t);
        }
    }
}

