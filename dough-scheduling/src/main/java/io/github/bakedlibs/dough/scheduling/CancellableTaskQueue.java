package io.github.bakedlibs.dough.scheduling;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class CancellableTaskQueue extends TaskQueue {

    private final List<Integer> taskIds = new ArrayList<>();

    @Override
    protected void scheduleTask(@Nonnull Plugin plugin, @Nonnull TaskNode node, int index, @Nonnull Runnable runnable) {
        int id = node.isAsynchronous()
                ? (node.getDelay() > 0
                        ? Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, node.getDelay()).getTaskId()
                        : Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable).getTaskId())
                : (node.getDelay() > 0
                        ? Bukkit.getScheduler().runTaskLater(plugin, runnable, node.getDelay()).getTaskId()
                        : Bukkit.getScheduler().runTask(plugin, runnable).getTaskId());

        taskIds.add(id);
    }

    public void cancel() {
        taskIds.forEach(Bukkit.getScheduler()::cancelTask);
        taskIds.clear();
    }
}
