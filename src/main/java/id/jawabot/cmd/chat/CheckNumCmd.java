package id.jawabot.cmd.chat;

import id.jawa.client.JaWaClient;
import id.jawa.domain.model.ContactStatus;
import id.jawa.feature.messaging.MessageReceiver;
import id.jawabot.cmd.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public final class CheckNumCmd implements Command {

    private static final Logger LOG = LoggerFactory.getLogger(CheckNumCmd.class);

    @Override
    public String name() { return "check"; }

    @Override
    public String tag() { return "chat"; }

    @Override
    public String usage() { return ".check <nomor>"; }

    @Override
    public String description() { return "Cek apakah nomor terdaftar di WhatsApp."; }

    @Override
    public List<String> aliases() { return List.of(".check", ".onwa"); }

    @Override
    public void execute(JaWaClient client, MessageReceiver.Decoded decoded, String[] args) {
        if (args.length == 0) {
            client.messages().sendReply(decoded, "Format: .check <nomor>\nContoh: .check 628123456789");
            return;
        }

        String phone = args[0].replaceAll("[^0-9]", "");
        client.chats().isOnWhatsApp(List.of(phone))
            .thenAccept(results -> {
                ContactStatus status = results.get(phone);
                if (status != null && status.isOnWhatsApp()) {
                    client.messages().sendReply(decoded, "Nomor " + phone + " terdaftar di WhatsApp.\nJID: " + status.jid());
                } else {
                    client.messages().sendReply(decoded, "Nomor " + phone + " tidak terdaftar di WhatsApp.");
                }
            })
            .exceptionally(err -> {
                LOG.error("Gagal memeriksa nomor: {}", err.getMessage());
                client.messages().sendReply(decoded, "Gagal memeriksa nomor: " + err.getMessage());
                return null;
            });
    }
}
