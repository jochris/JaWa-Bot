package id.jawabot.cmd.owner;

import id.jawa.client.JaWaClient;
import id.jawa.domain.model.PrivacySettings;
import id.jawa.feature.messaging.MessageReceiver;
import id.jawabot.cmd.Command;
import id.jawabot.config.BotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class PrivacyCmd implements Command {

    private static final Logger LOG = LoggerFactory.getLogger(PrivacyCmd.class);

    @Override
    public String name() { return "privacy"; }

    @Override
    public String tag() { return "owner"; }

    @Override
    public String usage() { return ".privacy [category value | timer <sec>]"; }

    @Override
    public String description() { return "Lihat atau atur setelan privasi WhatsApp bot."; }

    @Override
    public List<String> aliases() { return List.of(".privacy", "privacy"); }

    @Override
    public void execute(JaWaClient client, MessageReceiver.Decoded decoded, String[] args) {
        if (!BotConfig.isOwnerOrSelf(client, decoded.senderJid())) {
            return;
        }

        if (args.length == 0) {
            client.privacy().getPrivacySettings().whenComplete((settings, err) -> {
                if (err != null) {
                    LOG.error("Failed to fetch privacy settings", err);
                    client.messages().sendReply(decoded, "Error: gagal mengambil setelan privasi");
                    return;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("Pengaturan Privasi WhatsApp:\n");
                sb.append("Group Add: ").append(settings.groupAdd()).append("\n");
                sb.append("Last Seen: ").append(settings.lastSeen()).append("\n");
                sb.append("Status: ").append(settings.status()).append("\n");
                sb.append("Profile Photo: ").append(settings.profilePhoto()).append("\n");
                sb.append("Read Receipts: ").append(settings.readReceipts()).append("\n");
                sb.append("Online: ").append(settings.online()).append("\n");
                sb.append("Call Add: ").append(settings.callAdd());
                client.messages().sendReply(decoded, sb.toString());
            });
            return;
        }

        if (args[0].equalsIgnoreCase("timer") || args[0].equalsIgnoreCase("disappearing")) {
            if (args.length < 2) {
                client.messages().sendReply(decoded, "Format: .privacy timer <detik (0/86400/604800/7776000)>");
                return;
            }
            try {
                int sec = Integer.parseInt(args[1]);
                client.privacy().setDefaultDisappearingTimer(sec).whenComplete((res, err) -> {
                    if (err != null) {
                        LOG.error("Failed to set disappearing timer", err);
                        client.messages().sendReply(decoded, "Error: gagal mengatur default disappearing timer");
                    } else {
                        client.messages().sendReply(decoded, "Default disappearing timer diatur ke " + sec + " detik");
                    }
                });
            } catch (NumberFormatException e) {
                client.messages().sendReply(decoded, "Error: durasi harus berupa angka detik");
            }
            return;
        }

        if (args.length >= 2) {
            String category = args[0].toLowerCase();
            String value = args[1].toLowerCase();
            client.privacy().setPrivacySetting(category, value).whenComplete((res, err) -> {
                if (err != null) {
                    LOG.error("Failed to set privacy category {} to {}", category, value, err);
                    client.messages().sendReply(decoded, "Error: gagal mengubah privasi " + category);
                } else {
                    client.messages().sendReply(decoded, "Privasi " + category + " diubah menjadi " + value);
                }
            });
            return;
        }

        client.messages().sendReply(decoded, "Format: .privacy atau .privacy <category> <value> atau .privacy timer <detik>");
    }
}
