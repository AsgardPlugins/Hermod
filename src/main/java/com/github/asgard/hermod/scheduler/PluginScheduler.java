package com.github.asgard.hermod.scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface PluginScheduler {

    Task global(Runnable task);

    Task globalLater(Runnable task, long delayTicks);

    Task globalTimer(Runnable task, long delayTicks, long periodTicks);

    Task region(Location location, Runnable task);

    Task entity(Entity entity, Runnable task);

    Task entityLater(Entity entity, Runnable task, long delayTicks);

    Task async(Runnable task);

    Task asyncTimer(Runnable task, long delayTicks, long periodTicks);

    void cancelAll();
}
