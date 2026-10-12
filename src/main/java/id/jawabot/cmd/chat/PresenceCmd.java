package id.jawabot.cmd.chat;

import id.jawa.client.JaWaClient;
import id.jawa.feature.messaging.MessageReceiver;
import id.jawabot.cmd.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class PresenceCmd implements Command {

    private static final Logger LOG = LoggerFactory.getLogger(PresenceCmd.class);

    @Override
    public String name() { return "presence"; }

    @Override
    public String tag() { return "chat"; }

    @Override
    public String usage() { return ".presence <online|offline|sub <jid>>"; }

    @Override
    public String description() { return "Atur status online bot atau subscribe presence kontak."; }

    @Override
    public List<String> aliases() { return List.of(".presence"); }

    @Override
    public void execute(JaWaClient client, MessageReceiver.Decoded decoded, String[] args) {
        if (args.length == 0) {
            client.messages().sendReply(decoded, "Format:\n.presence online\n.presence offline\n.presence sub <nomor/jid>");
            return;
        }

        String subAction = args[0].toLowerCase();
        if ("online".equals(subAction)) {
            client.chats().sendPresence(true);
            client.messages().sendReply(decoded, "Status kehadiran bot diatur ke online");
        } else if ("offline".equals(subAction)) {
            client.chats().sendPresence(false);
            client.messages().sendReply(decoded, "Status kehadiran bot diatur ke offline");
        } else if ("sub".equals(subAction) && args.length > 1) {
            String target = args[1].trim();
            if (!target.contains("@")) {
                target = target + "@s.whatsapp.net";
            }
            client.chats().subscribePresence(target);
            client.messages().sendReply(decoded, "Berhasil subscribe presence untuk: " + target);
        } else {
            client.messages().sendReply(decoded, "Format tidak valid. Gunakan: .presence online|offline|sub <jid>");
        }
    }
}
