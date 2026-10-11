package id.jawabot;

import id.jawa.client.JaWaClient;
import id.jawa.domain.store.SqliteAuthStore;
import id.jawa.feature.messaging.MessageReceiver;
import id.jawabot.cmd.CommandRegistry;
import id.jawabot.config.BotConfig;
import id.jawabot.helper.JidHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public final class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        System.out.println("🤖 Starting JaWa-Bot (WhatsApp Client Bot)");

        Path dbPath = Path.of("sessions/jawabot.db");
        SqliteAuthStore store = new SqliteAuthStore(dbPath);

        boolean intact = store.checkIntegrity();
        LOG.info("SQLite database integrity check: {}", intact ? "PASSED" : "FAILED");
        LOG.info("Active SQLite records: {}", store.getStats());

        if (store.isPaired()) {
            var creds = store.load();
            if (creds != null) {
                if (creds.meJid != null) {
                    BotConfig.setBotNumber(JidHelper.extractPhoneNumber(creds.meJid));
                }
                if (creds.meLid != null && creds.meJid != null) {
                    String lidBare = creds.meLid.split("@")[0].split(":")[0] + "@lid";
                    String pnBare = creds.meJid.split("@")[0].split(":")[0] + "@s.whatsapp.net";
                    JaWaClient.LID_TO_PN_MAP.put(lidBare, pnBare);
                }
            }
        }

        CommandRegistry registry = new CommandRegistry(store);

        JaWaClient client = new JaWaClient(store)
                .autoReconnect(true)
                .autoReadReceipt(true)
                .autoComposingPresence(true)
                .autoPreKeyUpload(true);

        client.listener(new JaWaClient.Listener() {
            @Override
            public void onQr(List<String> qrs) {
                try {
                    if (store.isPaired()) return;
                } catch (Exception ignored) {}

                String phone = System.getProperty("jawa.phone");
                if (phone == null || phone.isBlank()) {
                    System.out.println("\n📱 Enter Phone Number for Pairing (E.164 without +, e.g. 628123456789):");
                    System.out.print("> ");
                    Scanner scanner = new Scanner(System.in);
                    if (scanner.hasNextLine()) {
                        phone = scanner.nextLine().trim().replaceAll("[^0-9]", "");
                    }
                }

                if (phone != null && !phone.isBlank()) {
                    final String targetPhone = phone;
                    client.requestPairingCode(targetPhone, null).whenComplete((code, err) -> {
                        if (err != null) {
                            LOG.error("Failed requesting pairing code", err);
                            return;
                        }
                        String formattedCode = code.length() == 8
                            ? code.substring(0, 4) + "-" + code.substring(4)
                            : code;
                        System.out.println("📱 Target Phone Number: +" + targetPhone);
                        System.out.println("🔑 Pairing Code      : " + formattedCode);
                    });
                }
            }

            @Override
            public void onConnected() {
                System.out.println("🎉 JaWa-Bot connected successfully & listening!");
                if (client.creds() != null) {
                    if (client.creds().meJid != null) {
                        BotConfig.setBotNumber(JidHelper.extractPhoneNumber(client.creds().meJid));
                    }
                    if (client.creds().meLid != null && client.creds().meJid != null) {
                        String lidBare = client.creds().meLid.split("@")[0].split(":")[0] + "@lid";
                        String pnBare = client.creds().meJid.split("@")[0].split(":")[0] + "@s.whatsapp.net";
                        JaWaClient.LID_TO_PN_MAP.put(lidBare, pnBare);
                    }
                }
                client.groups().queryJoinedGroups();
            }

            @Override
            public void onMessage(MessageReceiver.Decoded decoded) {
                registry.dispatch(client, decoded);
            }

            @Override
            public void onError(Throwable t) {
                LOG.error("JaWa-Bot error encountered", t);
            }
        });

        client.connect();
        client.join();
    }
}
