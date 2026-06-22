package gg.gianluca.allowedops.command.sub;

import gg.gianluca.allowedops.AllowedOPsPlugin;
import gg.gianluca.allowedops.command.CommandRequirements;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class ListSubcommand {

    private static final String PERMISSION = "allowedops.list";

    private final AllowedOPsPlugin plugin;

    public ListSubcommand(final AllowedOPsPlugin plugin) {
        this.plugin = plugin;
    }

    public void execute(final CommandSourceStack source) {
        if (!CommandRequirements.hasPermission(source, PERMISSION)) {
            CommandRequirements.sendError(source, "You do not have permission to do that.");
            return;
        }

        final var allowed = plugin.repository().snapshot();
        if (allowed.isEmpty()) {
            CommandRequirements.sendInfo(source, "The allowed OP list is empty.");
            return;
        }

        final List<UUID> sorted = new ArrayList<>(allowed);
        sorted.sort(Comparator.comparing(UUID::toString));

        source.getSender().sendMessage(Component.text("Allowed OPs (" + sorted.size() + "):"));
        for (final UUID uuid : sorted) {
            final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            final String name = offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown";
            source.getSender().sendMessage(Component.text("- " + name + " (" + uuid + ")"));
        }
    }
}
