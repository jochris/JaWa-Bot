package id.jawabot.cmd.chat;

import id.jawa.client.JaWaClient;
import id.jawa.feature.messaging.MessageReceiver;
import id.jawabot.cmd.Command;
import id.jawabot.helper.JidHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class UserInfoCmd implements Command {

    private static final Logger LOG = LoggerFactory.getLogger(UserInfoCmd.class);

    @Override
    public String name() { return "whois"; }

    @Override
    public String tag() { return "chat"; }

    @Override
    public String usage() { return ".whois [nomor / reply]"; }

    @Override
    public String description() { return "Cek info detail user WhatsApp (status, picture ID, device, LID)."; }

    @Override
    public List<String> aliases() { return List.of(".whois", "whois", ".userinfo", "userinfo"); }

    @Override
    public void execute(JaWaClient client, MessageReceiver.Decoded decoded, String[] args) {
        String targetJid = null;
        if (args.length > 0) {
            String clean = args[0].replaceAll("[^0-9]", "");
            if (!clean.isBlank()) {
                targetJid = clean + "@s.whatsapp.net";
            }
        }

        if (targetJid == null && decoded.message() != null && decoded.message().hasExtendedTextMessage()) {
            var ctx = decoded.message().getExtendedTextMessage().getContextInfo();
            if (ctx.hasParticipant() && !ctx.getParticipant().isBlank()) {
                targetJid = ctx.getParticipant();
            }
        }

        if (targetJid == null) {
            targetJid = decoded.senderJid();
        }

        final String finalTarget = targetJid;
        client.profile().getUserInfo(finalTarget).whenComplete((info, err) -> {
            if (err != null || info == null) {
                LOG.error("Failed to query userinfo for {}", finalTarget, err);
                client.messages().sendReply(decoded, "Error: gagal mengambil info user " + finalTarget);
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("User Info:\n");
            sb.append("JID: ").append(info.jid()).append("\n");
            if (info.lid() != null && !info.lid().isBlank()) {
                sb.append("LID: ").append(info.lid()).append("\n");
            }
            if (info.verifiedName() != null && !info.verifiedName().isBlank()) {
                sb.append("Verified Name: ").append(info.verifiedName()).append("\n");
            }
            sb.append("About/Status: ").append(info.status() != null && !info.status().isBlank() ? info.status() : "-").append("\n");
            sb.append("Foto ID: ").append(info.pictureId() != null ? info.pictureId() : "-").append("\n");
            sb.append("Perangkat Terdaftar: ").append(info.devices() != null ? info.devices().size() : 0);

            client.messages().sendReply(decoded, sb.toString());
        });
    }
}
