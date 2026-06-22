package gg.gianluca.allowedops.command.sub;

import gg.gianluca.allowedops.AllowedOPsPlugin;
import gg.gianluca.allowedops.command.CommandRequirements;
import gg.gianluca.allowedops.config.PluginConfig;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.Map;

public final class ReloadSubcommand {

    private static final String PERMISSION = "allowedops.reload";

    private final AllowedOPsPlugin plugin;

    public ReloadSubcommand(final AllowedOPsPlugin plugin) {
        this.plugin = plugin;
    }

    public void execute(final CommandSourceStack source) {
        if (!CommandRequirements.hasPermission(source, PERMISSION)) {
            CommandRequirements.sendError(source, "You do not have permission to do that.");
            return;
        }

        plugin.reloadLocalState();
        plugin.discord().updateConfig(plugin.pluginConfig());

        CommandRequirements.sendSuccess(source, "AllowedOPs configuration and data reloaded.");
        plugin.discord().send(
                PluginConfig.DiscordAlert.RELOAD,
                Map.of("executor", CommandRequirements.executorName(source))
        );
    }
}
