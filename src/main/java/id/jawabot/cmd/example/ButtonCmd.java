package id.jawabot.cmd.example;

import id.jawa.client.JaWaClient;
import id.jawa.feature.messaging.MessageEncoder;
import id.jawa.feature.messaging.MessageReceiver;
import id.jawabot.cmd.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class ButtonCmd implements Command {

    private static final Logger LOG = LoggerFactory.getLogger(ButtonCmd.class);

    @Override
    public String name() { return "button"; }

    @Override
    public String tag() { return "example"; }

    @Override
    public String usage() { return ".button"; }

    @Override
    public String description() { return "Contoh pengiriman tombol interaktif CTA."; }

    @Override
    public List<String> aliases() { return List.of(".button", ".cta"); }

    @Override
    public void execute(JaWaClient client, MessageReceiver.Decoded decoded, String[] args) {
        String targetJid = decoded.groupJid() != null ? decoded.groupJid() : decoded.senderJid();

        var buttons = List.of(
            MessageEncoder.CtaButton.url("GitHub Repository", "https://github.com/jochris/JaWa"),
            MessageEncoder.CtaButton.copy("Salin Kode", "JAWA-LIGHTWEIGHT")
        );

        client.messages().sendCtaButtons(targetJid, "Ini adalah contoh tombol interaktif menggunakan JaWa library", "JaWa Bot", buttons)
            .thenAccept(msgId -> LOG.info("Sent CTA buttons id={}", msgId))
            .exceptionally(err -> {
                LOG.error("Gagal mengirim CTA buttons: {}", err.getMessage());
                client.messages().sendReply(decoded, "Gagal mengirim tombol: " + err.getMessage());
                return null;
            });
    }
}
