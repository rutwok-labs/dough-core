package io.github.bakedlibs.dough.common;

import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DoughTimings {

    private static final DecimalFormat FORMAT = new DecimalFormat("0.########");

    private final String name;
    private final List<TimingEntry> steps = new ArrayList<>();
    private final Logger logger;

    private record TimingEntry(String label, long nanos) {}

    public DoughTimings(@Nonnull Plugin plugin, @Nonnull String name) {
        Validate.notNull(name, "Name cannot be null");
        Validate.notNull(plugin, "Plugin cannot be null");
        this.name = name;
        this.logger = plugin.getLogger();
    }

    public void step() {
        step("Step " + (steps.size() + 1));
    }

    public void step(@Nonnull String label) {
        this.steps.add(new TimingEntry(label, System.nanoTime()));
    }

    public @Nonnull String buildTimings(boolean clearTimings) {
        return buildTimings(clearTimings, Level.INFO);
    }

    public @Nonnull String buildTimings(boolean clearTimings, Level level) {
        final StringBuilder sb = new StringBuilder("-- Timings " + this.name
                + " (" + this.steps.size() + ") --");

        int step = 1;
        long lastStep = 0;
        if (this.steps.size() >= 2) {
            for (TimingEntry entry : steps) {
                long nanos = entry.nanos();

                if (step != 1) {
                    sb.append("\n  ").append(entry.label()).append(". ").append(nanos - lastStep)
                            .append("ns (").append(FORMAT.format((nanos - lastStep) / 1e6)).append("ms)");
                }
                lastStep = nanos;
                step++;
            }
        }
        final long totalNs = this.steps.get(this.steps.size() - 1).nanos() - this.steps.get(0).nanos();
        sb.append("\n  Total: ").append(totalNs)
                .append("ns (").append(FORMAT.format(totalNs / 1e6)).append("ms)");

        this.logger.log(level, sb.toString());

        if (clearTimings) {
            this.steps.clear();
        }
        return sb.toString();
    }

    public void logTimings() {
        this.logTimings(true, Level.INFO);
    }

    public void logTimings(boolean clearTimings) {
        this.logTimings(clearTimings, Level.INFO);
    }

    public void logTimings(boolean clearTimings, @Nonnull Level level) {
        this.logTimings(clearTimings, msg -> logger.log(level, msg), level);
    }

    public void logTimings(boolean clearTimings, @Nonnull CommandSender sender) {
        this.logTimings(clearTimings, sender::sendMessage, Level.INFO);
    }

    private void logTimings(boolean clearTimings, @Nonnull Consumer<String> printer, @Nonnull Level level) {
        printer.accept(buildTimings(clearTimings, level));
    }
}
