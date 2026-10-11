package id.jawabot.cmd.owner;

import id.jawa.client.JaWaClient;
import id.jawa.domain.store.SqliteAuthStore;
import id.jawa.feature.messaging.MessageReceiver;
import id.jawabot.cmd.Command;
import id.jawabot.config.BotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public final class DbMaintCmd implements Command {

    private static final Logger LOG = LoggerFactory.getLogger(DbMaintCmd.class);
    private final SqliteAuthStore store;

    public DbMaintCmd(SqliteAuthStore store) {
        this.store = store;
    }

    @Override
    public String name() { return "db"; }

    @Override
    public String tag() { return "owner"; }

    @Override
    public String usage() { return ".db [stats|vacuum|check]"; }

    @Override
    public String description() { return "Cek statistik, integritas, dan optimasi database SQLite (Khusus Owner)."; }

    @Override
    public List<String> aliases() { return List.of(".db", "db", ".dbstats", "dbstats", ".vacuum", "vacuum"); }

    @Override
    public void execute(JaWaClient client, MessageReceiver.Decoded decoded, String[] args) {
        if (!BotConfig.isOwnerOrSelf(client, decoded.senderJid())) {
            return;
        }

        String sub = args.length > 0 ? args[0].toLowerCase() : "stats";

        if (sub.equals("vacuum")) {
            store.vacuum();
            client.messages().sendText(decoded, "🧹 *SQLite Maintenance*: Database berhasil divacuum & ukuran disk dioptimasi.");
            LOG.info("Executed SQLite VACUUM by owner/self {}", decoded.senderJid());
            return;
        }

        boolean intact = store.checkIntegrity();
        Map<String, Integer> stats = store.getStats();

        StringBuilder sb = new StringBuilder();
        sb.append("🗄️ *SQLITE SESSION DATABASE STATS*\n");
        sb.append("----------------------------------------\n");
        sb.append("• *Database Health*: ").append(intact ? "OK (Healthy) ✅" : "CORRUPT ❌").append("\n");
        for (var entry : stats.entrySet()) {
            sb.append("• *").append(entry.getKey()).append("*: ").append(entry.getValue()).append(" rows\n");
        }
        sb.append("----------------------------------------\n");
        sb.append("💡 _Gunakan `.db vacuum` untuk merapikan disk._");

        client.messages().sendText(decoded, sb.toString());
        LOG.info("Reported SQLite database stats to owner/self {}", decoded.senderJid());
    }
}
