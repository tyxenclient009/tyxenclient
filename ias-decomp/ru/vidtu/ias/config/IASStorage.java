/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Contract
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Unmodifiable
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package ru.vidtu.ias.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vidtu.ias.account.Account;

public final class IASStorage {
    @NotNull
    private static final String DISCLAIMER = "> ENGLISH\nNotification about security of accounts stored in the \"In-Game Account Switcher\" mod:\nUNDER NO CIRCUMSTANCES SHOULD YOU SEND THIS FOLDER TO *ANYONE* (INCLUDING DEVELOPERS OF THIS MOD),\nEVEN IF IT APPEARS THAT THIS FOLDER IS FULLY EMPTY.\nIF YOU ACCIDENTALLY SENT THIS FOLDER TO ANYONE, PLEASE, VISIT THE FOLLOWING WEBSITE:\nhttps://account.microsoft.com/security\nAND CHANGE YOUR PASSWORD, THEN VISIT THE FOLLOWING WEBSITE:\nhttps://account.live.com/consent/manage\nAND REVOKE THE PERMISSIONS (ACCESS) TO THE \"In-Game Account Switcher\" APPLICATION,\nAND/OR ANY OTHER THAT YOU DO CAN'T RECOGNIZE OR YOU SUSPECT IT COULD ACCESS YOUR GAME ACCOUNT.\nAFTER REVOKING ACCESS YOU SHOULD *NOT* USE THIS MODIFICATION FOR 31 DAYS.\n(If you suspect someone has got access to your game account, revoke ALL permissions\nfor ALL applications and do *NOT* launch the game for 31 days at all)\n\n\n\n> \u0420\u0423\u0421\u0421\u041a\u0418\u0419 (RUSSIAN)\n\u0423\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u0435 \u043e \u0431\u0435\u0437\u043e\u043f\u0430\u0441\u043d\u043e\u0441\u0442\u0438 \u0430\u043a\u043a\u0430\u0443\u043d\u0442\u043e\u0432 \u0438\u0437 \u043c\u043e\u0434\u0430 \"In-Game Account Switcher\":\n\u041d\u0418 \u041f\u0420\u0418 \u041a\u0410\u041a\u0418\u0425 \u041e\u0411\u0421\u0422\u041e\u042f\u0422\u0415\u041b\u042c\u0421\u0422\u0412\u0410\u0425 \u041d\u0415 \u041e\u0422\u041f\u0420\u0410\u0412\u041b\u042f\u0419\u0422\u0415 \u042d\u0422\u0423 \u041f\u0410\u041f\u041a\u0423 *\u041a\u041e\u041c\u0423-\u041b\u0418\u0411\u041e* (\u0412 \u0422\u041e\u041c \u0427\u0418\u0421\u041b\u0415 \u0418 \u0420\u0410\u0417\u0420\u0410\u0411\u041e\u0422\u0427\u0418\u041a\u0410\u041c \u042d\u0422\u041e\u0413\u041e \u041c\u041e\u0414\u0410),\n\u0414\u0410\u0416\u0415 \u0415\u0421\u041b\u0418 \u0412\u0410\u041c \u041a\u0410\u0416\u0415\u0422\u0421\u042f, \u0427\u0422\u041e \u042d\u0422\u0410 \u041f\u0410\u041f\u041a\u0410 \u041f\u041e\u041b\u041d\u041e\u0421\u0422\u042c\u042e \u041f\u0423\u0421\u0422\u0410\u042f.\n\u0415\u0421\u041b\u0418 \u0412\u042b \u0421\u041b\u0423\u0427\u0410\u0419\u041d\u041e \u041e\u0422\u041f\u0420\u0410\u0412\u0418\u041b\u0418 \u042d\u0422\u0423 \u041f\u0410\u041f\u041a\u0423 \u041a\u041e\u041c\u0423-\u041b\u0418\u0411\u041e, \u041f\u041e\u0416\u0410\u041b\u0423\u0419\u0421\u0422\u0410, \u0417\u0410\u0419\u0414\u0418\u0422\u0415 \u041d\u0410 \u0421\u041b\u0415\u0414\u0423\u042e\u0429\u0418\u0419 \u0412\u0415\u0411\u0421\u0410\u0419\u0422:\nhttps://account.microsoft.com/security\n\u0418 \u0421\u041c\u0415\u041d\u0418\u0422\u0415 \u0421\u0412\u041e\u0419 \u041f\u0410\u0420\u041e\u041b\u042c, \u041f\u041e\u0422\u041e\u041c \u0417\u0410\u0419\u0414\u0418\u0422\u0415 \u041d\u0410 \u0421\u041b\u0415\u0414\u0423\u042e\u0429\u0418\u0419 \u0412\u0415\u0411\u0421\u0410\u0419\u0422:\nhttps://account.live.com/consent/manage\n\u0418 \u041e\u0422\u0417\u041e\u0412\u0418\u0422\u0415 \u0420\u0410\u0417\u0420\u0415\u0428\u0415\u041d\u0418\u042f (\u0414\u041e\u0421\u0422\u0423\u041f) \u041a \u041f\u0420\u0418\u041b\u041e\u0416\u0415\u041d\u0418\u042e \"In-Game Account Switcher\"\n\u0418/\u0418\u041b\u0418 \u041b\u042e\u0411\u041e\u041c\u0423 \u0414\u0420\u0423\u0413\u041e\u041c\u0423, \u041a\u041e\u0422\u041e\u0420\u041e\u0415 \u0412\u042b \u041d\u0415 \u041c\u041e\u0416\u0415\u0422\u0415 \u041e\u041f\u041e\u0417\u041d\u0410\u0422\u042c \u0418\u041b\u0418 \u041f\u041e\u0414\u041e\u0417\u0420\u0415\u0412\u0410\u0415\u0422\u0415, \u0427\u0422\u041e \u041e\u041d\u041e \u041c\u041e\u0416\u0415\u0422\n\u041f\u041e\u041b\u0423\u0427\u0418\u0422\u042c \u0414\u041e\u0421\u0422\u0423\u041f \u041a \u0412\u0410\u0428\u0415\u041c\u0423 \u0418\u0413\u0420\u041e\u0412\u041e\u041c\u0423 \u0410\u041a\u041a\u0410\u0423\u041d\u0422\u0423.\n\u041f\u041e\u0421\u041b\u0415 \u041e\u0422\u0417\u042b\u0412\u0410 \u0414\u041e\u0421\u0422\u0423\u041f\u0410 \u0412\u042b *\u041d\u0415* \u0414\u041e\u041b\u0416\u041d\u042b \u0418\u0421\u041f\u041e\u041b\u042c\u0417\u041e\u0412\u0410\u0422\u042c \u042d\u0422\u0423 \u041c\u041e\u0414\u0418\u0424\u0418\u041a\u0410\u0426\u0418\u042e \u041a\u0410\u041a \u041c\u0418\u041d\u0418\u041c\u0423\u041c 31 \u0414\u0415\u041d\u042c.\n(\u0415\u0441\u043b\u0438 \u0432\u044b \u043f\u043e\u0434\u043e\u0437\u0440\u0435\u0432\u0430\u0435\u0442\u0435, \u0447\u0442\u043e \u043a\u0442\u043e-\u0442\u043e \u043f\u043e\u043b\u0443\u0447\u0438\u043b \u0434\u043e\u0441\u0442\u0443\u043f \u043a \u0432\u0430\u0448\u0435\u043c\u0443 \u0438\u0433\u0440\u043e\u0432\u043e\u043c\u0443 \u0430\u043a\u043a\u0430\u0443\u043d\u0442\u0443, \u043e\u0442\u0437\u043e\u0432\u0438\u0442\u0435 \u0412\u0421\u0415 \u0440\u0430\u0437\u0440\u0435\u0448\u0435\u043d\u0438\u044f\n\u0434\u043b\u044f \u0412\u0421\u0415\u0425 \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u0439 \u0438 *\u041d\u0415* \u0437\u0430\u043f\u0443\u0441\u043a\u0430\u0439\u0442\u0435 \u0438\u0433\u0440\u0443 \u0432\u043e\u043e\u0431\u0449\u0435 \u043a\u0430\u043a \u043c\u0438\u043d\u0438\u043c\u0443\u043c 31 \u0434\u0435\u043d\u044c)\n";
    @NotNull
    private static final @Unmodifiable List<String> DISCLAIMER_FILE_NAMES = List.of("READ_ME_IMPORTANT.txt", "\u041f\u0420\u041e\u0427\u0422\u0418_\u041c\u0415\u041d\u042f_\u0412\u0410\u0416\u041d\u041e.txt");
    @NotNull
    public static final Logger LOGGER = LoggerFactory.getLogger((String)"IAS/IASStorage");
    @NotNull
    public static final List<Account> ACCOUNTS = new ArrayList<Account>(0);
    public static boolean gameDisclaimerShown = false;

    @Contract(value="-> fail", pure=true)
    private IASStorage() {
        throw new AssertionError((Object)"No instances.");
    }

    public static void disclaimers(@NotNull Path path) {
        try {
            LOGGER.debug("IAS: Writing disclaimers into {}...", (Object)path);
            path = path.resolve("_IAS_ACCOUNTS_DO_NOT_SEND_TO_ANYONE");
            Files.createDirectories(path, new FileAttribute[0]);
            for (String name : DISCLAIMER_FILE_NAMES) {
                try {
                    Path file = path.resolve(name);
                    Files.writeString(file, (CharSequence)DISCLAIMER, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.SYNC, StandardOpenOption.DSYNC, LinkOption.NOFOLLOW_LINKS);
                }
                catch (Throwable t) {
                    if (!name.equals("READ_ME_IMPORTANT.txt")) continue;
                    throw t;
                }
            }
            LOGGER.debug("IAS: Disclaimers ({}) written to {}.", DISCLAIMER_FILE_NAMES, (Object)path);
        }
        catch (Throwable t) {
            LOGGER.error("Unable to write IAS disclaimers.", t);
        }
    }

    public static void load(@NotNull Path path) {
        try {
            LOGGER.debug("IAS: Loading storage for {}...", (Object)path);
            Path folder = path.resolve("_IAS_ACCOUNTS_DO_NOT_SEND_TO_ANYONE/.hidden");
            Path file = folder.resolve("accounts_v1.do_not_send_to_anyone");
            gameDisclaimerShown = Files.isRegularFile(folder.resolve("game_disclaimer_shown"), LinkOption.NOFOLLOW_LINKS);
            if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
                LOGGER.debug("IAS: Storage not found. Saving...");
                IASStorage.save(path);
                return;
            }
            file = file.toRealPath(LinkOption.NOFOLLOW_LINKS);
            byte[] data = Files.readAllBytes(file);
            try (DataInputStream in = new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(data)));){
                int length = in.readUnsignedShort();
                ArrayList<Account> list = new ArrayList<Account>(length);
                for (int i = 0; i < length; ++i) {
                    list.add(Account.readTyped(in));
                }
                ACCOUNTS.addAll(list);
                HashSet set = new HashSet(ACCOUNTS.size());
                ACCOUNTS.removeIf(Predicate.not(set::add));
                LOGGER.debug("IAS: Loaded {} (currently: {}) accounts from {}.", new Object[]{list.size(), ACCOUNTS.size(), file});
            }
        }
        catch (Throwable t) {
            throw new RuntimeException("Unable to load IAS storage.", t);
        }
    }

    public static void save(@NotNull Path path) {
        try {
            byte[] data;
            Account[] list;
            LOGGER.debug("IAS: Saving storage into {}...", (Object)path);
            Path file = path.resolve("_IAS_ACCOUNTS_DO_NOT_SEND_TO_ANYONE/.hidden/accounts_v1.do_not_send_to_anyone");
            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 DeflaterOutputStream defOut = new DeflaterOutputStream(byteOut);
                 DataOutputStream out = new DataOutputStream(defOut);){
                list = (Account[])ACCOUNTS.toArray(Account[]::new);
                out.writeShort(list.length);
                for (Account account : list) {
                    Account.writeTyped(out, account);
                }
                defOut.finish();
                data = byteOut.toByteArray();
            }
            Files.createDirectories(file.getParent(), new FileAttribute[0]);
            try {
                Files.setAttribute(file.getParent(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            try {
                Files.setAttribute(file.getParent(), "dos:system", true, LinkOption.NOFOLLOW_LINKS);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            Files.write(file, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.SYNC, StandardOpenOption.DSYNC, LinkOption.NOFOLLOW_LINKS);
            LOGGER.debug("IAS: Saved {} accounts to {}.", (Object)list.length, (Object)file);
        }
        catch (Throwable t) {
            throw new RuntimeException("Unable to save IAS storage.", t);
        }
    }

    public static void gameDisclaimerShown(@NotNull Path path) {
        try {
            LOGGER.debug("IAS: Marking in-game disclaimers as shown into {}...", (Object)path);
            gameDisclaimerShown = true;
            Path file = path.resolve("_IAS_ACCOUNTS_DO_NOT_SEND_TO_ANYONE/.hidden/game_disclaimer_shown");
            Files.createDirectories(file.getParent(), new FileAttribute[0]);
            Files.createFile(file, new FileAttribute[0]);
            LOGGER.debug("IAS: Marked in-game disclaimers as shown to {}.", (Object)file);
        }
        catch (Throwable t) {
            throw new RuntimeException("Unable to mark game disclaimer as shown.", t);
        }
    }
}

