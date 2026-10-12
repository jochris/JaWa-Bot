package id.jawabot.cmd.chat;

import id.jawa.client.JaWaClient;
import id.jawa.feature.messaging.MessageReceiver;
import id.jawabot.cmd.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class ChatActionCmd implements Command {

    private static final Logger LOG = LoggerFactory.getLogger(ChatActionCmd.class);

    @Override
    public String name() { return "chat"; }

    @Override
    public String tag() { return "chat"; }

    @Override
    public String usage() { return ".chat <pin|unpin|archive|unarchive|mute <detik>>"; }

    @Override
    public String description() { return "Kelola status pin, arsip, dan bisukan chat."; }

    @Override
    public List<String> aliases() { return List.of(".chat"); }

    @Override
    public void execute(JaWaClient client, MessageReceiver.Decoded decoded, String[] args) {
        if (args.length == 0) {
            client.messages().sendReply(decoded, "Format:\n.chat pin\n.chat unpin\n.chat archive\n.chat unarchive\n.chat mute <detik>\n.chat unmute");
            return;
        }

        String targetJid = decoded.groupJid() != null ? decoded.groupJid() : decoded.senderJid();
        String action = args[0].toLowerCase();

        switch (action) {
            case "pin" -> client.chats().pinChat(targetJid, true)
                .thenRun(() -> client.messages().sendReply(decoded, "Chat berhasil disematkan (pin)"))
                .exceptionally(err -> {
                    client.messages().sendReply(decoded, "Gagal menyematkan chat: " + err.getMessage());
                    return null;
                });
            case "unpin" -> client.chats().pinChat(targetJid, false)
                .thenRun(() -> client.messages().sendReply(decoded, "Sematkan chat dicopot (unpin)"))
                .exceptionally(err -> {
                    client.messages().sendReply(decoded, "Gagal mencopot sematan chat: " + err.getMessage());
                    return null;
                });
            case "archive" -> client.chats().archiveChat(targetJid, true)
                .thenRun(() -> client.messages().sendReply(decoded, "Chat berhasil diarsipkan"))
                .exceptionally(err -> {
                    client.messages().sendReply(decoded, "Gagal mengarsipkan chat: " + err.getMessage());
                    return null;
                });
            case "unarchive" -> client.chats().archiveChat(targetJid, false)
                .thenRun(() -> client.messages().sendReply(decoded, "Chat dikeluarkan dari arsip"))
                .exceptionally(err -> {
                    client.messages().sendReply(decoded, "Gagal mengeluarkan chat dari arsip: " + err.getMessage());
                    return null;
                });
            case "mute" -> {
                long duration = args.length > 1 ? Long.parseLong(args[1]) : 8 * 3600;
                client.chats().muteChat(targetJid, duration)
                    .thenRun(() -> client.messages().sendReply(decoded, "Chat dibisukan selama " + duration + " detik"))
                    .exceptionally(err -> {
                        client.messages().sendReply(decoded, "Gagal membisukan chat: " + err.getMessage());
                        return null;
                    });
            }
            case "unmute" -> client.chats().muteChat(targetJid, 0)
                .thenRun(() -> client.messages().sendReply(decoded, "Bisukan chat dimatikan (unmute)"))
                .exceptionally(err -> {
                    client.messages().sendReply(decoded, "Gagal mengaktifkan suara chat: " + err.getMessage());
                    return null;
                });
            default -> client.messages().sendReply(decoded, "Aksi tidak dikenal: " + action);
        }
    }
}
