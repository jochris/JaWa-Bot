package id.jawabot.cmd.group;

import id.jawa.client.JaWaClient;
import id.jawa.feature.messaging.MessageReceiver;
import id.jawabot.cmd.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class GroupLockCmd implements Command {

    private static final Logger LOG = LoggerFactory.getLogger(GroupLockCmd.class);

    @Override
    public String name() { return "grouplock"; }

    @Override
    public String tag() { return "group"; }

    @Override
    public String usage() { return ".grouplock <on|off>"; }

    @Override
    public String description() { return "Kunci atau buka izin edit info grup."; }

    @Override
    public List<String> aliases() { return List.of(".grouplock", ".glock"); }

    @Override
    public void execute(JaWaClient client, MessageReceiver.Decoded decoded, String[] args) {
        if (decoded.groupJid() == null) {
            client.messages().sendReply(decoded, "Command ini hanya bisa digunakan di dalam grup");
            return;
        }

        if (args.length == 0 || (!"on".equalsIgnoreCase(args[0]) && !"off".equalsIgnoreCase(args[0]))) {
            client.messages().sendReply(decoded, "Format: .grouplock on|off");
            return;
        }

        boolean locked = "on".equalsIgnoreCase(args[0]);
        client.groups().setGroupLocked(decoded.groupJid(), locked)
            .thenRun(() -> {
                client.messages().sendReply(decoded, "Izin edit info grup diatur ke: " + (locked ? "hanya admin" : "semua peserta"));
            })
            .exceptionally(err -> {
                LOG.error("Gagal mengubah kunci grup: {}", err.getMessage());
                client.messages().sendReply(decoded, "Gagal mengubah setting grup: " + err.getMessage());
                return null;
            });
    }
}
