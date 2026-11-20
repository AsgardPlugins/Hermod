package com.github.asgard.hermod.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class BukkitPluginScheduler implements PluginScheduler {

    private final Plugin plugin;

    public BukkitPluginScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    private static Task wrap(BukkitTask task) {
        return task::cancel;
    }

    @Override
    public Task global(Runnable task) {
        return wrap(Bukkit.getScheduler().runTask(plugin, task));
    }

    @Override
    public Task globalLater(Runnable task, long delayTicks) {
        return wrap(Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks));
    }

    @Override
    public Task globalTimer(Runnable task, long delayTicks, long periodTicks) {
        return wrap(Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks));
    }

    @Override
    public Task region(Location location, Runnable task) {
        return global(task);
    }

    @Override
    public Task entity(Entity entity, Runnable task) {
        return global(task);
    }

    @Override
    public Task entityLater(Entity entity, Runnable task, long delayTicks) {
        return globalLater(task, delayTicks);
    }

    @Override
    public Task async(Runnable task) {
        return wrap(Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }

    @Override
    public Task asyncTimer(Runnable task, long delayTicks, long periodTicks) {
        return wrap(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks));
    }

    @Override
    public void cancelAll() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }
}
