package gg.gianluca.allowedops.command.sub;

import gg.gianluca.allowedops.AllowedOPsPlugin;
import gg.gianluca.allowedops.command.CommandRequirements;
import gg.gianluca.allowedops.config.PluginConfig;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.Map;

public final class RemoveSubcommand {

    private static final String PERMISSION = "allowedops.remove";

    private final AllowedOPsPlugin plugin;

    public RemoveSubcommand(final AllowedOPsPlugin plugin) {
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
        if (!plugin.repository().remove(resolved.uuid())) {
            CommandRequirements.sendError(source, resolved.displayName() + " is not on the allowed OP list.");
            return;
        }

        plugin.flushStorage();
        CommandRequirements.sendSuccess(source, "Removed " + resolved.displayName() + " (" + resolved.uuidString() + ") from the allowed OP list.");

        plugin.discord().send(
                PluginConfig.DiscordAlert.REMOVE_OP,
                Map.of(
                        "target", resolved.displayName() + " (" + resolved.uuidString() + ")",
                        "executor", CommandRequirements.executorName(source)
                )
        );
    }
}
