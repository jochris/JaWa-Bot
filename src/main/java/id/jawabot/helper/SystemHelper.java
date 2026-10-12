package id.jawabot.helper;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

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

    public static String getMemoryReport() {
        Runtime runtime = Runtime.getRuntime();
        long heapTotal = runtime.totalMemory();
        long heapFree = runtime.freeMemory();
        long heapUsed = heapTotal - heapFree;
        long heapMax = runtime.maxMemory();

        java.lang.management.MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        long nonHeapUsed = memBean.getNonHeapMemoryUsage().getUsed();
        long nonHeapCommitted = memBean.getNonHeapMemoryUsage().getCommitted();

        long vmRssKb = 0;
        long vmHwmKb = 0;
        long vmSizeKb = 0;
        int threads = 0;

        try {
            java.util.List<String> lines = java.nio.file.Files.readAllLines(java.nio.file.Path.of("/proc/self/status"));
            for (String line : lines) {
                if (line.startsWith("VmRSS:")) {
                    vmRssKb = parseKb(line);
                } else if (line.startsWith("VmHWM:")) {
                    vmHwmKb = parseKb(line);
                } else if (line.startsWith("VmSize:")) {
                    vmSizeKb = parseKb(line);
                } else if (line.startsWith("Threads:")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) {
                        threads = Integer.parseInt(parts[1].trim());
                    }
                }
            }
        } catch (Exception ignored) {
        }

        long sysTotal = 0;
        long sysFree = 0;
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOs) {
            sysTotal = sunOs.getTotalMemorySize();
            sysFree = sunOs.getFreeMemorySize();
        }

        long gcCount = 0;
        long gcTimeMs = 0;
        for (java.lang.management.GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            long count = gc.getCollectionCount();
            if (count > 0) gcCount += count;
            long time = gc.getCollectionTime();
            if (time > 0) gcTimeMs += time;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Process RSS: ").append(formatBytes(vmRssKb * 1024)).append("\n");
        sb.append("Peak RSS: ").append(formatBytes(vmHwmKb * 1024)).append("\n");
        sb.append("Virtual Memory: ").append(formatBytes(vmSizeKb * 1024)).append("\n");
        sb.append("Threads: ").append(threads).append("\n");
        sb.append("JVM Heap: ").append(formatBytes(heapUsed)).append(" / ").append(formatBytes(heapTotal));
        sb.append(" (Max: ").append(formatBytes(heapMax)).append(")\n");
        sb.append("JVM Non-Heap: ").append(formatBytes(nonHeapUsed)).append(" (Committed: ").append(formatBytes(nonHeapCommitted)).append(")\n");
        sb.append("GC: ").append(gcCount).append(" runs (").append(gcTimeMs).append(" ms)\n");
        if (sysTotal > 0) {
            long sysUsed = sysTotal - sysFree;
            double pct = (double) sysUsed / (double) sysTotal * 100.0;
            sb.append("System RAM: ").append(formatBytes(sysUsed)).append(" / ").append(formatBytes(sysTotal))
              .append(String.format(java.util.Locale.US, " (%.1f%%)", pct)).append("\n");
        }
        sb.append("Uptime: ").append(getUptimeFormatted());

        return sb.toString();
    }

    private static long parseKb(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length >= 2) {
            try {
                return Long.parseLong(parts[1].trim());
            } catch (Exception ignored) {
            }
        }
        return 0;
    }

    private static String formatBytes(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        if (digitGroups >= units.length) digitGroups = units.length - 1;
        return String.format(java.util.Locale.US, "%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
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

        long vmRssKb = 0;
        try {
            java.util.List<String> lines = java.nio.file.Files.readAllLines(java.nio.file.Path.of("/proc/self/status"));
            for (String line : lines) {
                if (line.startsWith("VmRSS:")) {
                    vmRssKb = parseKb(line);
                    break;
                }
            }
        } catch (Exception ignored) {
        }

        String rssStr = vmRssKb > 0 ? formatBytes(vmRssKb * 1024) : "N/A";

        return """
            OS: %s (%s, %s)
            CPU: %d cores
            Process RSS: %s
            JVM Heap: %d MB / %d MB (Max: %d MB)
            Java Runtime: %s (%s)
            Uptime: %s""".formatted(
                osName, osArch, osVersion,
                cores,
                rssStr,
                usedMem / (1024 * 1024),
                totalMem / (1024 * 1024),
                maxMem / (1024 * 1024),
                javaVersion, javaVendor,
                getUptimeFormatted()
            );
    }
}
