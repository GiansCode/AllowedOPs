package gg.gianluca.allowedops.command.sub;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import gg.gianluca.allowedops.AllowedOPsPlugin;
import gg.gianluca.allowedops.command.CommandRequirements;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
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

    public LiteralArgumentBuilder<CommandSourceStack> build(final Commands registrar) {
        return Commands.literal("list")
                .requires(source -> CommandRequirements.hasPermission(source, PERMISSION))
                .executes(this::execute);
    }

    private int execute(final CommandContext<CommandSourceStack> context) {
        final var allowed = plugin.repository().snapshot();
        if (allowed.isEmpty()) {
            CommandRequirements.sendInfo(context.getSource(), "The allowed OP list is empty.");
            return 1;
        }

        final List<UUID> sorted = new ArrayList<>(allowed);
        sorted.sort(Comparator.comparing(UUID::toString));

        context.getSource().getSender().sendMessage(Component.text("Allowed OPs (" + sorted.size() + "):"));
        for (final UUID uuid : sorted) {
            final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            final String name = offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown";
            context.getSource().getSender().sendMessage(Component.text("- " + name + " (" + uuid + ")"));
        }
        return 1;
    }
}
