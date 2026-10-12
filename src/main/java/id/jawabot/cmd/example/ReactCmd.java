package id.jawabot.cmd.example;

import id.jawa.client.JaWaClient;
import id.jawa.feature.messaging.MessageReceiver;
import id.jawabot.cmd.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class ReactCmd implements Command {

    private static final Logger LOG = LoggerFactory.getLogger(ReactCmd.class);

    @Override
    public String name() { return "react"; }

    @Override
    public String tag() { return "example"; }

    @Override
    public String usage() { return ".react [emoji]"; }

    @Override
    public String description() { return "Kirim reaksi emoji ke pesan saat ini."; }

    @Override
    public List<String> aliases() { return List.of(".react", ".r"); }

    @Override
    public void execute(JaWaClient client, MessageReceiver.Decoded decoded, String[] args) {
        String emoji = (args.length > 0 && !args[0].isBlank()) ? args[0].trim() : "👍";
        client.messages().sendReaction(decoded, emoji)
            .thenAccept(msgId -> LOG.info("Sent reaction {} to msgId={}", emoji, decoded.msgId()))
            .exceptionally(err -> {
                LOG.error("Gagal mengirim reaksi: {}", err.getMessage());
                client.messages().sendReply(decoded, "Gagal mengirim reaksi: " + err.getMessage());
                return null;
            });
    }
}
