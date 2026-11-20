package com.github.asgard.hermod;

import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public final class HermodConfig {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private final FileConfiguration file;

    public HermodConfig(FileConfiguration file) {
        this.file = file;
    }

    public String twitchClientId() {
        return file.getString("twitch.client-id", "").trim();
    }

    public String twitchClientSecret() {
        return file.getString("twitch.client-secret", "").trim();
    }

    public long checkIntervalTicks() {
        return Math.max(15, file.getLong("twitch.check-interval-seconds", 60)) * 20L;
    }

    public boolean announceOnStartup() {
        return file.getBoolean("announce-on-startup", false);
    }

    public boolean currentlyLiveEnabled() {
        return file.getBoolean("currently-live.enabled", true);
    }

    public long currentlyLiveDelayTicks() {
        return Math.max(1, file.getLong("currently-live.delay-ticks", 200));
    }

    public String placeholderLive() {
        return legacy(file.getString("placeholders.live", "Live 🔴"));
    }

    public String placeholderLiveCompact() {
        return legacy(file.getString("placeholders.live-compact", "🔴"));
    }

    public String placeholderSeparator() {
        return legacy(file.getString("placeholders.users-separator", ", "));
    }

    public Component message(String path, TagResolver... resolvers) {
        return MINI.deserialize(file.getString(path, ""), resolvers);
    }

    public Sound sound(String path) {
        if (!file.getBoolean(path + ".enabled", true)) {
            return null;
        }
        try {
            Key key = Key.key(file.getString(path + ".sound", "entity.experience_orb.pickup"));
            return Sound.sound(key, Sound.Source.MASTER, (float) file.getDouble(path + ".volume", 1.0), (float) file.getDouble(path + ".pitch", 1.0));
        } catch (InvalidKeyException e) {
            return null;
        }
    }

    private static String legacy(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
