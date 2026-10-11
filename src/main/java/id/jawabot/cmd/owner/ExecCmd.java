package id.jawabot.cmd.owner;

import id.jawa.client.JaWaClient;
import id.jawa.feature.messaging.MessageReceiver;
import id.jawabot.cmd.Command;
import id.jawabot.config.BotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class ExecCmd implements Command {

    private static final Logger LOG = LoggerFactory.getLogger(ExecCmd.class);

    @Override
    public String name() { return "exec"; }

    @Override
    public String tag() { return "owner"; }

    @Override
    public String usage() { return "> <shell_cmd>"; }

    @Override
    public String description() { return "Eksekusi perintah terminal/shell (Khusus Owner)."; }

    @Override
    public List<String> aliases() { return List.of(">", "=>", "$", ".exec", "exec"); }

    @Override
    public void execute(JaWaClient client, MessageReceiver.Decoded decoded, String[] args) {
        if (!BotConfig.isOwnerOrSelf(client, decoded.senderJid())) {
            return;
        }

        String commandLine;
        String rawText = decoded.text().trim();

        if (rawText.startsWith("=>")) {
            commandLine = rawText.substring(2).trim();
        } else if (rawText.startsWith(">")) {
            commandLine = rawText.substring(1).trim();
        } else if (rawText.startsWith("$")) {
            commandLine = rawText.substring(1).trim();
        } else {
            commandLine = String.join(" ", args).trim();
        }

        if (commandLine.isBlank()) {
            client.messages().sendText(decoded, "⚠️ *Penggunaan*: `> <perintah_shell>` (contoh: `> ls -la` atau `> uptime`)");
            return;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", commandLine);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(15, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                output.append("\n⚠️ *Timeout*: Perintah dihentikan karena melebihi 15 detik.");
            }

            String result = output.toString().trim();
            if (result.isEmpty()) {
                result = "[Proses selesai tanpa output]";
            }

            client.messages().sendText(decoded, "💻 *Exec Output*:\n```\n" + result + "\n```");
            LOG.info("Executed shell command by owner/self {}: {}", decoded.senderJid(), commandLine);

        } catch (Exception e) {
            LOG.error("Failed executing shell command: {}", commandLine, e);
            client.messages().sendText(decoded, "❌ *Exec Error*: " + e.getMessage());
        }
    }
}
