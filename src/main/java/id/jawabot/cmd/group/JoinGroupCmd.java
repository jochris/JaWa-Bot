package id.jawabot.cmd.group;

import id.jawa.client.JaWaClient;
import id.jawa.feature.messaging.MessageReceiver;
import id.jawabot.cmd.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class JoinGroupCmd implements Command {

    private static final Logger LOG = LoggerFactory.getLogger(JoinGroupCmd.class);

    @Override
    public String name() { return "join"; }

    @Override
    public String tag() { return "group"; }

    @Override
    public String usage() { return ".join <link>"; }

    @Override
    public String description() { return "Bergabung ke grup WhatsApp menggunakan link invite."; }

    @Override
    public List<String> aliases() { return List.of(".join"); }

    @Override
    public void execute(JaWaClient client, MessageReceiver.Decoded decoded, String[] args) {
        if (args.length == 0) {
            client.messages().sendReply(decoded, "Format: .join <link_atau_kode_invite>");
            return;
        }

        String link = args[0].trim();
        client.groups().joinGroupWithLink(link)
            .thenAccept(joinedJid -> {
                client.messages().sendReply(decoded, "Berhasil bergabung ke grup: " + joinedJid);
            })
            .exceptionally(err -> {
                LOG.error("Gagal bergabung ke grup: {}", err.getMessage());
                client.messages().sendReply(decoded, "Gagal bergabung ke grup: " + err.getMessage());
                return null;
            });
    }
}
