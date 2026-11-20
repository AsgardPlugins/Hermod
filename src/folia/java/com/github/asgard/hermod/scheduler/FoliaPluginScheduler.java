package com.github.asgard.hermod.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public final class FoliaPluginScheduler implements PluginScheduler {

    private static final long TICK_MILLIS = 50L;

    private final Plugin plugin;

    public FoliaPluginScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    private static Task wrap(ScheduledTask task) {
        return task == null ? () -> { } : task::cancel;
    }

    private static long atLeastOne(long ticks) {
        return Math.max(1L, ticks);
    }

    @Override
    public Task global(Runnable task) {
        return wrap(Bukkit.getGlobalRegionScheduler().run(plugin, scheduled -> task.run()));
    }

    @Override
    public Task globalLater(Runnable task, long delayTicks) {
        return wrap(Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduled -> task.run(), atLeastOne(delayTicks)));
    }

    @Override
    public Task globalTimer(Runnable task, long delayTicks, long periodTicks) {
        return wrap(Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduled -> task.run(), atLeastOne(delayTicks), atLeastOne(periodTicks)));
    }

    @Override
    public Task region(Location location, Runnable task) {
        return wrap(Bukkit.getRegionScheduler().run(plugin, location, scheduled -> task.run()));
    }

    @Override
    public Task entity(Entity entity, Runnable task) {
        return wrap(entity.getScheduler().run(plugin, scheduled -> task.run(), null));
    }

    @Override
    public Task entityLater(Entity entity, Runnable task, long delayTicks) {
        return wrap(entity.getScheduler().runDelayed(plugin, scheduled -> task.run(), null, atLeastOne(delayTicks)));
    }

    @Override
    public Task async(Runnable task) {
        return wrap(Bukkit.getAsyncScheduler().runNow(plugin, scheduled -> task.run()));
    }

    @Override
    public Task asyncTimer(Runnable task, long delayTicks, long periodTicks) {
        return wrap(Bukkit.getAsyncScheduler().runAtFixedRate(plugin, scheduled -> task.run(), atLeastOne(delayTicks) * TICK_MILLIS, atLeastOne(periodTicks) * TICK_MILLIS, TimeUnit.MILLISECONDS));
    }

    @Override
    public void cancelAll() {
        Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
        Bukkit.getAsyncScheduler().cancelTasks(plugin);
    }
}
