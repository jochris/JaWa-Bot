package id.jawabot.cmd.group;

import id.jawa.client.JaWaClient;
import id.jawa.feature.messaging.MessageReceiver;
import id.jawabot.cmd.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class EphemeralCmd implements Command {

    private static final Logger LOG = LoggerFactory.getLogger(EphemeralCmd.class);

    @Override
    public String name() { return "ephemeral"; }

    @Override
    public String tag() { return "group"; }

    @Override
    public String usage() { return ".ephemeral [detik]"; }

    @Override
    public String description() { return "Atur timer pesan sementara (0 = nonaktif, 86400 = 24 jam)."; }

    @Override
    public List<String> aliases() { return List.of(".ephemeral", ".timer"); }

    @Override
    public void execute(JaWaClient client, MessageReceiver.Decoded decoded, String[] args) {
        String targetJid = decoded.groupJid() != null ? decoded.groupJid() : decoded.senderJid();

        if (args.length == 0) {
            int current = client.chats().getDisappearingTimer(targetJid);
            client.messages().sendReply(decoded, "Timer pesan sementara saat ini: " + current + " detik");
            return;
        }

        try {
            int seconds = Integer.parseInt(args[0].trim());
            client.chats().setDisappearingTimer(targetJid, seconds)
                .thenRun(() -> {
                    String msg = seconds <= 0
                        ? "Pesan sementara berhasil dinonaktifkan"
                        : "Pesan sementara diatur ke " + seconds + " detik";
                    client.messages().sendReply(decoded, msg);
                })
                .exceptionally(err -> {
                    LOG.error("Gagal mengatur pesan sementara: {}", err.getMessage());
                    client.messages().sendReply(decoded, "Gagal mengatur pesan sementara: " + err.getMessage());
                    return null;
                });
        } catch (NumberFormatException e) {
            client.messages().sendReply(decoded, "Format: .ephemeral <jumlah_detik>\nContoh: .ephemeral 86400 (24 jam) atau .ephemeral 0 (matikan)");
        }
    }
}
