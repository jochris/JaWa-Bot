/* SPDX-License-Identifier: GPL-3.0-or-later */
package id.jawabot.cmd.owner;

import id.jawa.client.JaWaClient;
import id.jawa.feature.messaging.MessageReceiver;
import id.jawabot.cmd.Command;
import id.jawabot.config.BotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Owner command to switch bot mode between Self (Owner only) and Public (Everyone).
 */
public final class ModeCmd implements Command {

    private static final Logger LOG = LoggerFactory.getLogger(ModeCmd.class);

    @Override
    public String name() { return "self"; }

    @Override
    public String tag() { return "owner"; }

    @Override
    public String usage() { return ".self / .public"; }

    @Override
    public String description() { return "Ubah mode bot: Self (Khusus Owner) atau Public (Semua Orang)."; }

    @Override
    public List<String> aliases() { return List.of(".self", "self", ".public", "public", ".mode", "mode"); }

    @Override
    public void execute(JaWaClient client, MessageReceiver.Decoded decoded, String[] args) {
        if (!BotConfig.isOwner(decoded.senderJid())) {
            return;
        }

        String raw = decoded.text().trim().toLowerCase();
        if (raw.startsWith(".self") || raw.equals("self") || (args.length > 0 && args[0].equalsIgnoreCase("self"))) {
            BotConfig.setPublicMode(false);
            client.sendText(decoded, "🔒 *Bot Mode*: Berhasil diubah ke *SELF MODE* (Hanya merespon pesan dari Owner).");
            LOG.info("Bot mode set to SELF MODE by owner {}", decoded.senderJid());
            return;
        }

        if (raw.startsWith(".public") || raw.equals("public") || (args.length > 0 && args[0].equalsIgnoreCase("public"))) {
            BotConfig.setPublicMode(true);
            client.sendText(decoded, "🌐 *Bot Mode*: Berhasil diubah ke *PUBLIC MODE* (Merespon semua pengguna).");
            LOG.info("Bot mode set to PUBLIC MODE by owner {}", decoded.senderJid());
            return;
        }

        /* Toggle or show status */
        boolean current = BotConfig.isPublicMode();
        boolean newMode = !current;
        BotConfig.setPublicMode(newMode);
        String modeName = newMode ? "PUBLIC MODE 🌐" : "SELF MODE 🔒";
        client.sendText(decoded, "⚙️ *Bot Mode*: Berhasil diubah ke *" + modeName + "*.");
    }
}
