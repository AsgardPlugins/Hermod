package com.github.asgard.hermod.stream;

import com.github.asgard.hermod.scheduler.PluginScheduler;
import com.github.asgard.hermod.scheduler.Task;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;

public final class LiveTracker {

    private final PluginScheduler scheduler;
    private final StreamStore store;
    private final Logger logger;
    private final Consumer<StreamEntry> onLive;
    private final Map<String, StreamEntry> live = Collections.synchronizedMap(new LinkedHashMap<>());

    private volatile TwitchClient client;
    private volatile boolean announceFirstPoll;
    private volatile boolean first = true;
    private volatile String lastStarted = "";
    private Task task;

    public LiveTracker(PluginScheduler scheduler, StreamStore store, Logger logger, Consumer<StreamEntry> onLive) {
        this.scheduler = scheduler;
        this.store = store;
        this.logger = logger;
        this.onLive = onLive;
    }

    public void start(TwitchClient client, long intervalTicks, boolean announceFirstPoll) {
        stop();
        this.client = client;
        this.announceFirstPoll = announceFirstPoll;
        this.first = true;
        if (!client.configured()) {
            logger.warning("Set twitch.client-id and twitch.client-secret in config.yml, no streams are checked until then");
            return;
        }
        task = scheduler.asyncTimer(this::poll, 20L, intervalTicks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public boolean running() {
        return task != null;
    }

    public void refresh() {
        if (task != null) {
            scheduler.async(this::poll);
        }
    }

    public boolean isLive(String user) {
        if (user == null) {
            return false;
        }
        return live.containsKey(user.toLowerCase(Locale.ROOT));
    }

    public List<StreamEntry> current() {
        synchronized (live) {
            return new ArrayList<>(live.values());
        }
    }

    public String lastStarted() {
        return lastStarted;
    }

    private synchronized void poll() {
        TwitchClient twitch = client;
        List<StreamEntry> tracked = store.all();
        Map<String, StreamEntry> byLogin = new HashMap<>();
        for (StreamEntry entry : tracked) {
            byLogin.put(entry.login(), entry);
        }
        Set<String> online;
        try {
            online = byLogin.isEmpty() ? Set.of() : twitch.liveLogins(byLogin.keySet());
        } catch (IOException e) {
            logger.warning("Could not check Twitch: " + e.getMessage());
            return;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        List<StreamEntry> started = new ArrayList<>();
        synchronized (live) {
            live.values().removeIf(entry -> !online.contains(entry.login()) || byLogin.get(entry.login()) == null);
            for (StreamEntry entry : tracked) {
                String key = entry.user().toLowerCase(Locale.ROOT);
                if (online.contains(entry.login()) && !live.containsKey(key)) {
                    live.put(key, entry);
                    started.add(entry);
                }
            }
        }
        boolean announce = !first || announceFirstPoll;
        first = false;
        for (StreamEntry entry : started) {
            lastStarted = entry.user();
            if (announce) {
                onLive.accept(entry);
            }
        }
    }
}
