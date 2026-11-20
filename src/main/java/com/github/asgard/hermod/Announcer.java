package com.github.asgard.hermod;

import com.github.asgard.hermod.stream.LiveTracker;
import com.github.asgard.hermod.stream.StreamEntry;
import java.util.List;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class Announcer {

    private final HermodPlugin plugin;

    public Announcer(HermodPlugin plugin) {
        this.plugin = plugin;
    }

    public void streamStarted(StreamEntry entry) {
        HermodConfig config = plugin.config();
        Component message = config.message("messages.stream-started",
                Placeholder.unparsed("user", entry.user()),
                Placeholder.unparsed("link", entry.link()));
        Sound sound = config.sound("sounds.stream-started");
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.scheduler().entity(player, () -> deliver(player, message, sound));
        }
    }

    public void currentlyLive(Player player) {
        HermodConfig config = plugin.config();
        LiveTracker tracker = plugin.tracker();
        List<StreamEntry> live = tracker.current();
        if (live.isEmpty()) {
            return;
        }
        Component users = Component.empty();
        for (int i = 0; i < live.size(); i++) {
            if (i > 0) {
                users = users.append(config.message("messages.currently-live-separator"));
            }
            StreamEntry entry = live.get(i);
            users = users.append(config.message("messages.currently-live-entry",
                    Placeholder.unparsed("user", entry.user()),
                    Placeholder.unparsed("link", entry.link())));
        }
        Component message = config.message("messages.currently-live", Placeholder.component("users", users));
        deliver(player, message, config.sound("sounds.currently-live"));
    }

    private static void deliver(Player player, Component message, Sound sound) {
        if (!player.isOnline()) {
            return;
        }
        player.sendMessage(message);
        if (sound != null) {
            player.playSound(sound);
        }
    }
}
