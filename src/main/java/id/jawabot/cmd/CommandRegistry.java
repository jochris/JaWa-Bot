/* SPDX-License-Identifier: GPL-3.0-or-later */
package id.jawabot.cmd;

import id.jawa.client.JaWaClient;
import id.jawa.feature.messaging.MessageReceiver;
import id.jawabot.cmd.info.InfoServerCmd;
import id.jawabot.cmd.info.PingCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Registry holding all registered commands and routing incoming text messages.
 */
public final class CommandRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(CommandRegistry.class);
    private final Map<String, Command> commands = new LinkedHashMap<>();
    private final Map<String, Command> aliasMap = new HashMap<>();

    public CommandRegistry() {
        register(new PingCmd());
        register(new InfoServerCmd());
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

        String rawText = decoded.text().trim();
        String[] parts = rawText.split("\\s+");
        String firstWord = parts[0].toLowerCase();
        String chatJid = decoded.groupJid() != null ? decoded.groupJid() : decoded.senderJid();

        /* Auto-generate menu/help if requested */
        if (firstWord.equals(".menu") || firstWord.equals("menu") || firstWord.equals(".help") || firstWord.equals("help")) {
            client.sendText(chatJid, renderMenuText());
            return true;
        }

        Command cmd = aliasMap.get(firstWord);
        if (cmd != null) {
            String[] args = Arrays.copyOfRange(parts, 1, parts.length);
            try {
                cmd.execute(client, decoded, args);
                return true;
            } catch (Exception e) {
                LOG.error("Command execution error for {}: {}", cmd.name(), e.toString(), e);
                client.sendText(chatJid, "❌ *Error*: Gagal mengeksekusi perintah `" + cmd.name() + "`.");
            }
        }
        return false;
    }

    public String renderMenuText() {
        StringBuilder sb = new StringBuilder();
        sb.append("🤖 *JAWA-BOT COMMAND MENU*\n");
        sb.append("========================================\n\n");

        Map<String, List<Command>> byTag = new LinkedHashMap<>();
        for (Command cmd : commands.values()) {
            byTag.computeIfAbsent(cmd.tag().toUpperCase(), k -> new ArrayList<>()).add(cmd);
        }

        for (var entry : byTag.entrySet()) {
            sb.append("📁 *CATEGORY: ").append(entry.getKey()).append("*\n");
            for (Command cmd : entry.getValue()) {
                sb.append("  • *").append(cmd.usage()).append("*\n");
                sb.append("    _Desc_: ").append(cmd.description()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("========================================\n");
        sb.append("💡 *Tip*: Ketik `.ping` atau `.infoserver` untuk mencoba.");
        return sb.toString();
    }
}
