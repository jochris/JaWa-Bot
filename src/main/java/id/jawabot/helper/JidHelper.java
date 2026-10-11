package id.jawabot.helper;

import id.jawa.client.JaWaClient;

public final class JidHelper {

    private JidHelper() {}

    public static String extractPhoneNumber(String jid) {
        if (jid == null || jid.isBlank()) return "";

        String userPart = jid.split("@")[0].split(":")[0];
        String mappedPn = JaWaClient.LID_TO_PN_MAP.get(userPart + "@lid");
        if (mappedPn != null && !mappedPn.isEmpty()) {
            return mappedPn.split("@")[0].split(":")[0].replaceAll("[^0-9]", "");
        }
        return userPart.replaceAll("[^0-9]", "");
    }
}
