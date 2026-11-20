package com.github.asgard.hermod.stream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class StreamStore {

    private final File file;
    private final Map<String, StreamEntry> entries = new ConcurrentHashMap<>();

    public StreamStore(File file) {
        this.file = file;
    }

    private static String key(String user) {
        return user.toLowerCase(Locale.ROOT);
    }

    public void load() {
        entries.clear();
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("streams");
        if (section == null) {
            return;
        }
        for (String user : section.getKeys(false)) {
            StreamEntry entry = StreamEntry.parse(user, section.getString(user, ""));
            if (entry != null) {
                entries.put(key(user), entry);
            }
        }
    }

    public synchronized void save() throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        for (StreamEntry entry : all()) {
            yaml.set("streams." + entry.user(), entry.link());
        }
        yaml.save(file);
    }

    public boolean put(StreamEntry entry) {
        return entries.put(key(entry.user()), entry) != null;
    }

    public StreamEntry remove(String user) {
        return entries.remove(key(user));
    }

    public StreamEntry get(String user) {
        return entries.get(key(user));
    }

    public List<StreamEntry> all() {
        List<StreamEntry> list = new ArrayList<>(entries.values());
        list.sort((a, b) -> a.user().compareToIgnoreCase(b.user()));
        return list;
    }
}
