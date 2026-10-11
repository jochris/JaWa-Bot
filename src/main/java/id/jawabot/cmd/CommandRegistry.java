package id.jawabot.cmd;

import id.jawa.client.JaWaClient;
import id.jawa.domain.store.SqliteAuthStore;
import id.jawa.feature.messaging.MessageReceiver;
import id.jawabot.cmd.info.InfoServerCmd;
import id.jawabot.cmd.info.PingCmd;
import id.jawabot.cmd.owner.DbMaintCmd;
import id.jawabot.cmd.owner.ExecCmd;
import id.jawabot.cmd.owner.ModeCmd;
import id.jawabot.config.BotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class CommandRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(CommandRegistry.class);
    private final Map<String, Command> commands = new LinkedHashMap<>();
    private final Map<String, Command> aliasMap = new HashMap<>();

    public CommandRegistry(SqliteAuthStore store) {
        register(new PingCmd());
        register(new InfoServerCmd());
        register(new ExecCmd());
        register(new ModeCmd());
        register(new DbMaintCmd(store));
    }

    public void register(Command cmd) {
        commands.put(cmd.name().toLowerCase(), cmd);
        aliasMap.put(cmd.name().toLowerCase(), cmd);
        for (String alias : cmd.aliases()) {
            aliasMap.put(alias.toLowerCase(), cmd);
        }
        LOG.info("Registered command: {} [tag={}, aliases={}]", cmd.name(), cmd.tag(), cmd.aliases());
    }

    public boolean dispatch(JaWaClient client, MessageReceiver.Decoded decoded) {
        if (decoded == null || decoded.text() == null || decoded.text().isBlank()) {
            return false;
        }

        if (!BotConfig.isPublicMode() && !BotConfig.isOwnerOrSelf(client, decoded.senderJid())) {
            return false;
        }

        String rawText = decoded.text().trim();

        if (rawText.startsWith(">") || rawText.startsWith("=>") || rawText.startsWith("$")) {
            Command execCmd = aliasMap.get("exec");
            if (execCmd != null) {
                try {
                    execCmd.execute(client, decoded, new String[0]);
                    return true;
                } catch (Exception e) {
                    LOG.error("Exec command execution error: {}", e.toString(), e);
                    client.messages().sendText(decoded, "❌ *Error*: Gagal mengeksekusi shell command.");
                }
            }
        }

        String[] parts = rawText.split("\\s+");
        String firstWord = parts[0].toLowerCase();

        if (firstWord.equals(".menu") || firstWord.equals(".help")) {
            client.messages().sendText(decoded, renderMenuText());
            return true;
        }

        if (!firstWord.startsWith(".")) {
            return false;
        }

        Command cmd = aliasMap.get(firstWord);
        if (cmd == null) {
            cmd = aliasMap.get(firstWord.substring(1));
        }

        if (cmd != null) {
            String[] args = Arrays.copyOfRange(parts, 1, parts.length);
            try {
                cmd.execute(client, decoded, args);
                return true;
            } catch (Exception e) {
                LOG.error("Command execution error for {}: {}", cmd.name(), e.toString(), e);
                client.messages().sendText(decoded, "❌ *Error*: Gagal mengeksekusi perintah `" + cmd.name() + "`.");
            }
        }
        return false;
    }

    public String renderMenuText() {
        StringBuilder sb = new StringBuilder();
        sb.append("🤖 *JaWa-Bot*\n\n");

        Map<String, List<Command>> byTag = new LinkedHashMap<>();
        for (Command cmd : commands.values()) {
            byTag.computeIfAbsent(cmd.tag().toLowerCase(), k -> new ArrayList<>()).add(cmd);
        }

        for (var entry : byTag.entrySet()) {
            String categoryName = entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1);
            sb.append("📁 *").append(categoryName).append("*\n");
            for (Command cmd : entry.getValue()) {
                String firstAlias = "";
                if (cmd.aliases() != null) {
                    for (String a : cmd.aliases()) {
                        String clean = a.startsWith(".") ? a.substring(1) : a;
                        if (!clean.equalsIgnoreCase(cmd.name())) {
                            firstAlias = " (" + a + ")";
                            break;
                        }
                    }
                }
                sb.append("• *").append(cmd.name()).append("*").append(firstAlias).append("\n");
            }
            sb.append("\n");
        }

        sb.append("💡 _Ketik nama perintah di atas._");
        return sb.toString();
    }
}
