package id.jawabot.cmd.example;

import id.jawa.client.JaWaClient;
import id.jawa.feature.messaging.MessageReceiver;
import id.jawabot.cmd.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public final class PollCmd implements Command {

    private static final Logger LOG = LoggerFactory.getLogger(PollCmd.class);

    @Override
    public String name() { return "poll"; }

    @Override
    public String tag() { return "example"; }

    @Override
    public String usage() { return ".poll <judul | opt1 | opt2 ...>"; }

    @Override
    public String description() { return "Buat voting polling interaktif."; }

    @Override
    public List<String> aliases() { return List.of(".poll", ".vote"); }

    @Override
    public void execute(JaWaClient client, MessageReceiver.Decoded decoded, String[] args) {
        if (args.length == 0) {
            client.messages().sendReply(decoded, "Format: .poll Judul Pertanyaan | Opsi A | Opsi B");
            return;
        }

        String targetJid = decoded.groupJid() != null ? decoded.groupJid() : decoded.senderJid();
        String fullInput = String.join(" ", args);
        String[] split = fullInput.split("\\|");

        if (split.length < 3) {
            client.messages().sendReply(decoded, "Polling minimal memerlukan 1 pertanyaan dan 2 pilihan.\nContoh: .poll Suka kopi? | Ya | Tidak");
            return;
        }

        String question = split[0].trim();
        List<String> options = Arrays.stream(Arrays.copyOfRange(split, 1, split.length))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();

        client.messages().sendPoll(targetJid, question, options, 1)
            .thenAccept(msgId -> LOG.info("Sent poll message id={}", msgId))
            .exceptionally(err -> {
                LOG.error("Gagal mengirim polling: {}", err.getMessage());
                client.messages().sendReply(decoded, "Gagal membuat polling: " + err.getMessage());
                return null;
            });
    }
}
