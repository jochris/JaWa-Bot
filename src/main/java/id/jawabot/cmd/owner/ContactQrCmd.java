package id.jawabot.cmd.owner;

import id.jawa.client.JaWaClient;
import id.jawa.feature.messaging.MessageReceiver;
import id.jawabot.cmd.Command;
import id.jawabot.config.BotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class ContactQrCmd implements Command {

    private static final Logger LOG = LoggerFactory.getLogger(ContactQrCmd.class);

    @Override
    public String name() { return "contactlink"; }

    @Override
    public String tag() { return "owner"; }

    @Override
    public String usage() { return ".contactlink [revoke | resolve <code>]"; }

    @Override
    public String description() { return "Dapatkan link share kontak WhatsApp bot atau selesaikan kode link kontak."; }

    @Override
    public List<String> aliases() { return List.of(".contactlink", ".myqr", "myqr", "contactlink"); }

    @Override
    public void execute(JaWaClient client, MessageReceiver.Decoded decoded, String[] args) {
        if (!BotConfig.isOwnerOrSelf(client, decoded.senderJid())) {
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("resolve")) {
            if (args.length < 2) {
                client.messages().sendReply(decoded, "Format: .contactlink resolve <code / link>");
                return;
            }
            client.profile().resolveContactQrLink(args[1]).whenComplete((target, err) -> {
                if (err != null) {
                    LOG.error("Failed to resolve contact link", err);
                    client.messages().sendReply(decoded, "Error: kode link kontak tidak valid");
                    return;
                }
                String text = "Contact QR Info:\nJID: " + target.jid()
                    + "\nNama: " + (target.pushName() != null ? target.pushName() : "-")
                    + "\nTipe: " + target.type();
                client.messages().sendReply(decoded, text);
            });
            return;
        }

        boolean revoke = args.length > 0 && args[0].equalsIgnoreCase("revoke");
        client.profile().getContactQrLink(revoke).whenComplete((url, err) -> {
            if (err != null) {
                LOG.error("Failed to fetch contact QR link", err);
                client.messages().sendReply(decoded, "Error: gagal mengambil link kontak");
                return;
            }
            String text = (revoke ? "Link kontak di-reset:\n" : "Link kontak WhatsApp:\n") + url;
            client.messages().sendReply(decoded, text);
        });
    }
}
