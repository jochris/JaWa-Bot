package id.jawabot.cmd;

import id.jawa.client.JaWaClient;
import id.jawa.feature.messaging.MessageReceiver;
import java.util.List;

public interface Command {

    String name();

    String tag();

    String usage();

    String description();

    default List<String> aliases() {
        return List.of();
    }

    void execute(JaWaClient client, MessageReceiver.Decoded decoded, String[] args);
}
