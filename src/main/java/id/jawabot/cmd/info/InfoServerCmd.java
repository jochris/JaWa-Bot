package id.jawabot.cmd.info;

import id.jawa.client.JaWaClient;
import id.jawa.feature.messaging.MessageReceiver;
import id.jawabot.cmd.Command;
import id.jawabot.helper.SystemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class InfoServerCmd implements Command {

    private static final Logger LOG = LoggerFactory.getLogger(InfoServerCmd.class);

    @Override
    public String name() { return "infoserver"; }

    @Override
    public String tag() { return "info"; }

    @Override
    public String usage() { return ".infoserver"; }

    @Override
    public String description() { return "Menampilkan informasi detail spesifikasi server, OS, CPU, RAM, dan Java Runtime."; }

    @Override
    public List<String> aliases() { return List.of(".infoserver", ".info"); }

    @Override
    public void execute(JaWaClient client, MessageReceiver.Decoded decoded, String[] args) {
        String chatJid = decoded.groupJid() != null ? decoded.groupJid() : decoded.senderJid();
        String report = SystemHelper.getServerReport() + "\n🤖 *JaWa-Bot v1.0.1* (Powered by JaWa SDK 0.0.4)";
        client.messages().sendReply(decoded, report);
        LOG.info("Executed .infoserver command for chat {}", chatJid);
    }
}
