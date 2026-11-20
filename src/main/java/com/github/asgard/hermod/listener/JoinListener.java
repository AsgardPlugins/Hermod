package com.github.asgard.hermod.listener;

import com.github.asgard.hermod.HermodConfig;
import com.github.asgard.hermod.HermodPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class JoinListener implements Listener {

    private final HermodPlugin plugin;

    public JoinListener(HermodPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        HermodConfig config = plugin.config();
        if (!config.currentlyLiveEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        plugin.scheduler().entityLater(player, () -> plugin.announcer().currentlyLive(player), config.currentlyLiveDelayTicks());
    }
}
