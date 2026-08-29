/* SPDX-License-Identifier: GPL-3.0-or-later */
package id.jawabot.cmd;

import id.jawa.client.JaWaClient;
import id.jawa.feature.messaging.MessageReceiver;
import java.util.List;

/**
 * Contract for all bot commands carrying rich metadata (name, tag/category, usage, description, aliases).
 */
public interface Command {

    /** Command primary name (e.g. "ping", "infoserver") */
    String name();

    /** Category tag (e.g. "info", "general", "group") */
    String tag();

    /** Usage syntax example (e.g. ".ping", ".infoserver") */
    String usage();

    /** Short human-readable description of what the command does */
    String description();

    /** Command aliases (e.g. [".p", "ping"]) */
    default List<String> aliases() {
        return List.of();
    }

    /**
     * Execute the command logic.
     *
     * @param client  JaWaClient instance
     * @param decoded decoded inbound message event
     * @param args    split command arguments
     */
    void execute(JaWaClient client, MessageReceiver.Decoded decoded, String[] args);
}
