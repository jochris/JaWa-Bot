package id.jawabot.cmd.info;

import id.jawa.client.JaWaClient;
import id.jawa.feature.messaging.MessageReceiver;
import id.jawabot.cmd.Command;
import id.jawabot.helper.SystemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class PingCmd implements Command {

    private static final Logger LOG = LoggerFactory.getLogger(PingCmd.class);

    @Override
    public String name() { return "ping"; }

    @Override
    public String tag() { return "info"; }

    @Override
    public String usage() { return ".ping"; }

    @Override
    public String description() { return "Cek kecepatan respon bot dan durasi aktif (uptime)."; }

    @Override
    public List<String> aliases() { return List.of(".ping", ".p"); }

    @Override
    public void execute(JaWaClient client, MessageReceiver.Decoded decoded, String[] args) {
        String chatJid = decoded.groupJid() != null ? decoded.groupJid() : decoded.senderJid();
        String text = "Pong\nUptime: " + SystemHelper.getUptimeFormatted();
        client.messages().sendReply(decoded, text);
        LOG.info("Executed .ping command for chat {}", chatJid);
    }
}
