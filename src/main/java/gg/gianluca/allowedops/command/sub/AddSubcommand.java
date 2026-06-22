package gg.gianluca.allowedops.command.sub;

import gg.gianluca.allowedops.AllowedOPsPlugin;
import gg.gianluca.allowedops.command.CommandRequirements;
import gg.gianluca.allowedops.config.PluginConfig;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.Map;

public final class AddSubcommand {

    private static final String PERMISSION = "allowedops.add";

    private final AllowedOPsPlugin plugin;

    public AddSubcommand(final AllowedOPsPlugin plugin) {
        this.plugin = plugin;
    }

    public void execute(final CommandSourceStack source, final String targetInput) {
        if (!CommandRequirements.hasPermission(source, PERMISSION)) {
            CommandRequirements.sendError(source, "You do not have permission to do that.");
            return;
        }

        final var target = CommandRequirements.resolveTarget(targetInput);
        if (target.isEmpty()) {
            CommandRequirements.sendError(source, "Unknown player or UUID.");
            return;
        }

        final CommandRequirements.ResolvedTarget resolved = target.get();
        if (!plugin.repository().add(resolved.uuid())) {
            CommandRequirements.sendError(source, resolved.displayName() + " is already on the allowed OP list.");
            return;
        }

        plugin.flushStorage();
        CommandRequirements.sendSuccess(source, "Added " + resolved.displayName() + " (" + resolved.uuidString() + ") to the allowed OP list.");

        plugin.discord().send(
                PluginConfig.DiscordAlert.ADD_OP,
                Map.of(
                        "target", resolved.displayName() + " (" + resolved.uuidString() + ")",
                        "executor", CommandRequirements.executorName(source)
                )
        );
    }
}
