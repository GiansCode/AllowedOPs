package gg.gianluca.allowedops.command;

import gg.gianluca.allowedops.AllowedOPsPlugin;
import gg.gianluca.allowedops.command.sub.AddSubcommand;
import gg.gianluca.allowedops.command.sub.ListSubcommand;
import gg.gianluca.allowedops.command.sub.ReloadSubcommand;
import gg.gianluca.allowedops.command.sub.RemoveSubcommand;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.Locale;

public final class AllowedOpsCommand implements BasicCommand {

    private final AllowedOPsPlugin plugin;
    private final AddSubcommand addSubcommand;
    private final RemoveSubcommand removeSubcommand;
    private final ListSubcommand listSubcommand;
    private final ReloadSubcommand reloadSubcommand;

    public AllowedOpsCommand(final AllowedOPsPlugin plugin) {
        this.plugin = plugin;
        this.addSubcommand = new AddSubcommand(plugin);
        this.removeSubcommand = new RemoveSubcommand(plugin);
        this.listSubcommand = new ListSubcommand(plugin);
        this.reloadSubcommand = new ReloadSubcommand(plugin);
    }

    @Override
    public void execute(final CommandSourceStack source, final String[] args) {
        if (!CommandRequirements.canUseRoot(plugin, source)) {
            CommandRequirements.sendError(source, "Only the console may run this command.");
            return;
        }

        if (args.length == 0) {
            sendUsage(source);
            return;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "add" -> {
                if (args.length < 2) {
                    CommandRequirements.sendError(source, "Usage: /allowedops add <player|uuid>");
                    return;
                }
                addSubcommand.execute(source, args[1]);
            }
            case "remove" -> {
                if (args.length < 2) {
                    CommandRequirements.sendError(source, "Usage: /allowedops remove <player|uuid>");
                    return;
                }
                removeSubcommand.execute(source, args[1]);
            }
            case "list" -> listSubcommand.execute(source);
            case "reload" -> reloadSubcommand.execute(source);
            default -> sendUsage(source);
        }
    }

    @Override
    public Collection<String> suggest(final CommandSourceStack source, final String[] args) {
        return CommandSuggestions.suggest(plugin, source, args);
    }

    @Override
    public boolean canUse(final CommandSender sender) {
        return CommandRequirements.canUseRoot(plugin, sender);
    }

    private static void sendUsage(final CommandSourceStack source) {
        CommandRequirements.sendInfo(source, "Usage: /allowedops <add|remove|list|reload>");
    }
}
