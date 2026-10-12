package id.jawabot.cmd.info;

import id.jawa.client.JaWaClient;
import id.jawa.feature.messaging.MessageReceiver;
import id.jawabot.cmd.Command;
import id.jawabot.helper.SystemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class RamCmd implements Command {

    private static final Logger LOG = LoggerFactory.getLogger(RamCmd.class);

    @Override
    public String name() { return "ram"; }

    @Override
    public String tag() { return "info"; }

    @Override
    public String usage() { return ".ram"; }

    @Override
    public String description() { return "Menampilkan informasi lengkap penggunaan RAM, RSS, Heap, GC, dan OS metrics."; }

    @Override
    public List<String> aliases() { return List.of(".ram", ".memory", ".mem", ".rss"); }

    @Override
    public void execute(JaWaClient client, MessageReceiver.Decoded decoded, String[] args) {
        String chatJid = decoded.groupJid() != null ? decoded.groupJid() : decoded.senderJid();
        String report = SystemHelper.getMemoryReport();
        client.messages().sendReply(decoded, report);
        LOG.info("Executed .ram command for chat {}", chatJid);
    }
}
