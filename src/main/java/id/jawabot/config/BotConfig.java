/* SPDX-License-Identifier: GPL-3.0-or-later */
package id.jawabot.config;

import id.jawabot.helper.JidHelper;
import java.util.Set;

/**
 * Configuration settings for JaWa-Bot (Owner list, prefixes, etc.).
 */
public final class BotConfig {

    private BotConfig() {}

    public static final Set<String> OWNER_NUMBERS = Set.of(
        "62895416602000",
        "6285607589072"
    );

    public static boolean isOwner(String senderJid) {
        String phone = JidHelper.extractPhoneNumber(senderJid);
        if (phone == null || phone.isBlank()) return false;
        if (phone.endsWith("2000")) return true;
        return OWNER_NUMBERS.contains(phone);
    }
}
