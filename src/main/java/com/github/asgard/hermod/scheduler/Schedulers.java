package com.github.asgard.hermod.scheduler;

import org.bukkit.plugin.Plugin;

public final class Schedulers {

    private Schedulers() {
    }

    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public static PluginScheduler create(Plugin plugin) {
        if (isFolia()) {
            try {
                Class<?> type = Class.forName("com.github.asgard.hermod.scheduler.FoliaPluginScheduler");
                return (PluginScheduler) type.getConstructor(Plugin.class).newInstance(plugin);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Folia was detected but its scheduler could not be created", e);
            }
        }
        return new BukkitPluginScheduler(plugin);
    }
}
