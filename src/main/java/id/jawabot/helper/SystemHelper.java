/* SPDX-License-Identifier: GPL-3.0-or-later */
package id.jawabot.helper;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

/**
 * Helper for extracting and formatting server metrics, OS, CPU, RAM, and Uptime.
 */
public final class SystemHelper {

    private static final long START_TIME = System.currentTimeMillis();

    private SystemHelper() {}

    public static String getUptimeFormatted() {
        long uptimeMs = System.currentTimeMillis() - START_TIME;
        long seconds = (uptimeMs / 1000) % 60;
        long minutes = (uptimeMs / (1000 * 60)) % 60;
        long hours = (uptimeMs / (1000 * 60 * 60)) % 24;
        long days = uptimeMs / (1000 * 60 * 60 * 24);

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0 || days > 0) sb.append(hours).append("h ");
        if (minutes > 0 || hours > 0 || days > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");

        return sb.toString();
    }

    public static String getServerReport() {
        Runtime runtime = Runtime.getRuntime();
        long totalMem = runtime.totalMemory();
        long freeMem = runtime.freeMemory();
        long usedMem = totalMem - freeMem;
        long maxMem = runtime.maxMemory();

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

        int cores = osBean.getAvailableProcessors();
        String osName = osBean.getName();
        String osArch = osBean.getArch();
        String osVersion = osBean.getVersion();

        String javaVendor = runtimeBean.getVmVendor();
        String javaVersion = System.getProperty("java.version");

        return """
            🖥️ *SERVER & SYSTEM METRICS*
            ----------------------------------------
            • *OS*: %s (%s, %s)
            • *CPU Cores*: %d Cores
            • *RAM Usage*: %d MB / %d MB (Max: %d MB)
            • *Java Runtime*: %s (%s)
            • *Bot Uptime*: %s
            ----------------------------------------
            """.formatted(
                osName, osArch, osVersion,
                cores,
                usedMem / (1024 * 1024),
                totalMem / (1024 * 1024),
                maxMem / (1024 * 1024),
                javaVersion, javaVendor,
                getUptimeFormatted()
            );
    }
}
