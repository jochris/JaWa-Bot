package id.jawabot.cmd.group;

import id.jawa.client.JaWaClient;
import id.jawa.feature.messaging.MessageReceiver;
import id.jawabot.cmd.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class GroupLinkCmd implements Command {

    private static final Logger LOG = LoggerFactory.getLogger(GroupLinkCmd.class);

    @Override
    public String name() { return "link"; }

    @Override
    public String tag() { return "group"; }

    @Override
    public String usage() { return ".link [revoke]"; }

    @Override
    public String description() { return "Ambil atau reset link invite grup."; }

    @Override
    public List<String> aliases() { return List.of(".link", ".invitelink"); }

    @Override
    public void execute(JaWaClient client, MessageReceiver.Decoded decoded, String[] args) {
        if (decoded.groupJid() == null) {
            client.messages().sendReply(decoded, "Command ini hanya bisa digunakan di dalam grup");
            return;
        }

        boolean reset = args.length > 0 && "revoke".equalsIgnoreCase(args[0]);
        client.groups().getGroupInviteLink(decoded.groupJid(), reset)
            .thenAccept(link -> {
                String msg = reset ? "Link invite berhasil direset:\n" + link : "Link invite grup:\n" + link;
                client.messages().sendReply(decoded, msg);
            })
            .exceptionally(err -> {
                LOG.error("Gagal mendapatkan invite link: {}", err.getMessage());
                client.messages().sendReply(decoded, "Gagal memproses link invite: " + err.getMessage());
                return null;
            });
    }
}
