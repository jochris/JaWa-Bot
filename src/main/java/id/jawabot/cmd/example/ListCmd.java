package id.jawabot.cmd.example;

import id.jawa.client.JaWaClient;
import id.jawa.feature.messaging.MessageEncoder;
import id.jawa.feature.messaging.MessageReceiver;
import id.jawabot.cmd.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class ListCmd implements Command {

    private static final Logger LOG = LoggerFactory.getLogger(ListCmd.class);

    @Override
    public String name() { return "list"; }

    @Override
    public String tag() { return "example"; }

    @Override
    public String usage() { return ".list"; }

    @Override
    public String description() { return "Contoh menu daftar pilihan interaktif."; }

    @Override
    public List<String> aliases() { return List.of(".list", ".menu_list"); }

    @Override
    public void execute(JaWaClient client, MessageReceiver.Decoded decoded, String[] args) {
        String targetJid = decoded.groupJid() != null ? decoded.groupJid() : decoded.senderJid();

        var rows = List.of(
            new MessageEncoder.ListRow("ping_row", ".ping", "Cek kecepatan respon bot"),
            new MessageEncoder.ListRow("ram_row", ".ram", "Cek penggunaan memori JVM"),
            new MessageEncoder.ListRow("info_row", ".infoserver", "Cek spesifikasi server")
        );
        var sections = List.of(new MessageEncoder.ListSection("Pilihan Perintah", rows));

        client.messages().sendListMessage(targetJid, "Daftar Menu Interaktif", "Pilih menu perintah di bawah ini:", "JaWa Bot", "Buka Menu", sections)
            .thenAccept(msgId -> LOG.info("Sent list message id={}", msgId))
            .exceptionally(err -> {
                LOG.error("Gagal mengirim list message: {}", err.getMessage());
                client.messages().sendReply(decoded, "Gagal mengirim menu: " + err.getMessage());
                return null;
            });
    }
}
