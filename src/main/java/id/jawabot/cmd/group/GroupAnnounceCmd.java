package id.jawabot.cmd.group;

import id.jawa.client.JaWaClient;
import id.jawa.feature.messaging.MessageReceiver;
import id.jawabot.cmd.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class GroupAnnounceCmd implements Command {

    private static final Logger LOG = LoggerFactory.getLogger(GroupAnnounceCmd.class);

    @Override
    public String name() { return "announce"; }

    @Override
    public String tag() { return "group"; }

    @Override
    public String usage() { return ".announce <on|off>"; }

    @Override
    public String description() { return "Atur mode pengumuman grup (hanya admin kirim pesan)."; }

    @Override
    public List<String> aliases() { return List.of(".announce", ".gannounce"); }

    @Override
    public void execute(JaWaClient client, MessageReceiver.Decoded decoded, String[] args) {
        if (decoded.groupJid() == null) {
            client.messages().sendReply(decoded, "Command ini hanya bisa digunakan di dalam grup");
            return;
        }

        if (args.length == 0 || (!"on".equalsIgnoreCase(args[0]) && !"off".equalsIgnoreCase(args[0]))) {
            client.messages().sendReply(decoded, "Format: .announce on|off");
            return;
        }

        boolean ann = "on".equalsIgnoreCase(args[0]);
        client.groups().setGroupAnnounce(decoded.groupJid(), ann)
            .thenRun(() -> {
                client.messages().sendReply(decoded, "Mode pengumuman grup diatur ke: " + (ann ? "hanya admin" : "semua peserta"));
            })
            .exceptionally(err -> {
                LOG.error("Gagal mengubah mode pengumuman: {}", err.getMessage());
                client.messages().sendReply(decoded, "Gagal mengubah mode pengumuman: " + err.getMessage());
                return null;
            });
    }
}
