package com.github.asgard.hermod.command;

import com.github.asgard.hermod.HermodPlugin;
import com.github.asgard.hermod.stream.StreamEntry;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public final class HermodCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUB = List.of("add", "remove", "list", "reload");

    private final HermodPlugin plugin;

    public HermodCommand(HermodPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "add" -> add(sender, args);
            case "remove" -> remove(sender, args);
            case "list" -> list(sender);
            case "reload" -> {
                plugin.reloadAll();
                send(sender, "command-messages.reloaded");
                if (!plugin.tracker().running()) {
                    send(sender, "command-messages.no-credentials");
                }
            }
            default -> send(sender, "command-messages.usage");
        }
        return true;
    }

    private void add(CommandSender sender, String[] args) {
        if (args.length < 3) {
            send(sender, "command-messages.usage");
            return;
        }
        StreamEntry entry = StreamEntry.parse(args[1], args[2]);
        if (entry == null) {
            send(sender, "command-messages.invalid-link");
            return;
        }
        boolean replaced = plugin.store().put(entry);
        save();
        plugin.tracker().refresh();
        send(sender, replaced ? "command-messages.updated" : "command-messages.added",
                Placeholder.unparsed("user", entry.user()), Placeholder.unparsed("link", entry.link()));
    }

    private void remove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            send(sender, "command-messages.usage");
            return;
        }
        StreamEntry removed = plugin.store().remove(args[1]);
        if (removed == null) {
            send(sender, "command-messages.not-tracked", Placeholder.unparsed("user", args[1]));
            return;
        }
        save();
        plugin.tracker().refresh();
        send(sender, "command-messages.removed", Placeholder.unparsed("user", removed.user()));
    }

    private void list(CommandSender sender) {
        List<StreamEntry> all = plugin.store().all();
        if (all.isEmpty()) {
            send(sender, "command-messages.list-empty");
            return;
        }
        send(sender, "command-messages.list-header");
        for (StreamEntry entry : all) {
            String state = plugin.tracker().isLive(entry.user()) ? "command-messages.state-live" : "command-messages.state-offline";
            send(sender, "command-messages.list-entry",
                    Placeholder.unparsed("user", entry.user()),
                    Placeholder.unparsed("link", entry.link()),
                    Placeholder.component("state", plugin.config().message(state)));
        }
    }

    private void save() {
        try {
            plugin.store().save();
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save streams.yml: " + e.getMessage());
        }
    }

    private void send(CommandSender sender, String path, TagResolver... resolvers) {
        sender.sendMessage(plugin.config().message(path, resolvers));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            SUB.stream().filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT))).forEach(out::add);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            plugin.store().all().stream().map(StreamEntry::user)
                    .filter(u -> u.toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT))).forEach(out::add);
        }
        return out;
    }
}
