package com.github.asgard.hermod.placeholder;

import com.github.asgard.hermod.HermodConfig;
import com.github.asgard.hermod.HermodPlugin;
import com.github.asgard.hermod.stream.StreamEntry;
import java.util.List;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public final class HermodExpansion extends PlaceholderExpansion {

    private final HermodPlugin plugin;

    public HermodExpansion(HermodPlugin plugin) {
        this.plugin = plugin;
    }

    public static void register(HermodPlugin plugin) {
        new HermodExpansion(plugin).register();
    }

    @Override
    public String getIdentifier() {
        return "hermod";
    }

    @Override
    public String getAuthor() {
        return "Akis";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        HermodConfig config = plugin.config();
        switch (params.toLowerCase()) {
            case "live":
                return player != null && plugin.tracker().isLive(player.getName()) ? config.placeholderLive() : "";
            case "live_compact":
                return player != null && plugin.tracker().isLive(player.getName()) ? config.placeholderLiveCompact() : "";
            case "user_live":
                return plugin.tracker().lastStarted();
            case "users_live":
                List<StreamEntry> live = plugin.tracker().current();
                return String.join(config.placeholderSeparator(), live.stream().map(StreamEntry::user).toList());
            default:
                return null;
        }
    }
}
