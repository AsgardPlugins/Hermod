package com.github.asgard.hermod;

import com.github.asgard.hermod.command.HermodCommand;
import com.github.asgard.hermod.listener.JoinListener;
import com.github.asgard.hermod.placeholder.HermodExpansion;
import com.github.asgard.hermod.scheduler.PluginScheduler;
import com.github.asgard.hermod.scheduler.Schedulers;
import com.github.asgard.hermod.stream.LiveTracker;
import com.github.asgard.hermod.stream.StreamStore;
import com.github.asgard.hermod.stream.DecapiSource;
import com.github.asgard.hermod.stream.HelixSource;
import com.github.asgard.hermod.stream.LiveSource;
import java.io.File;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class HermodPlugin extends JavaPlugin {

    private PluginScheduler scheduler;
    private volatile HermodConfig config;
    private StreamStore store;
    private LiveTracker tracker;
    private Announcer announcer;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = new HermodConfig(getConfig());
        scheduler = Schedulers.create(this);
        store = new StreamStore(new File(getDataFolder(), "streams.yml"));
        store.load();
        announcer = new Announcer(this);
        tracker = new LiveTracker(scheduler, store, getLogger(), announcer::streamStarted);
        startTracking();

        PluginCommand command = getCommand("hermod");
        HermodCommand handler = new HermodCommand(this);
        command.setExecutor(handler);
        command.setTabCompleter(handler);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            HermodExpansion.register(this);
        }
    }

    @Override
    public void onDisable() {
        if (tracker != null) {
            tracker.stop();
        }
        if (scheduler != null) {
            scheduler.cancelAll();
        }
    }

    public void reloadAll() {
        reloadConfig();
        config = new HermodConfig(getConfig());
        store.load();
        startTracking();
    }

    private void startTracking() {
        boolean app = !config.twitchClientId().isEmpty() && !config.twitchClientSecret().isEmpty();
        LiveSource source = app ? new HelixSource(config.twitchClientId(), config.twitchClientSecret()) : new DecapiSource();
        getLogger().info(app ? "Checking streams with your Twitch app" : "Checking streams through decapi.me, set a Twitch app in config.yml for something more reliable");
        tracker.start(source, config.checkIntervalTicks(), config.announceOnStartup());
    }

    public HermodConfig config() {
        return config;
    }

    public PluginScheduler scheduler() {
        return scheduler;
    }

    public StreamStore store() {
        return store;
    }

    public LiveTracker tracker() {
        return tracker;
    }

    public Announcer announcer() {
        return announcer;
    }
}
