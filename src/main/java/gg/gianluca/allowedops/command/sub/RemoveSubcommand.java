package gg.gianluca.allowedops.command.sub;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import gg.gianluca.allowedops.AllowedOPsPlugin;
import gg.gianluca.allowedops.command.CommandRequirements;
import gg.gianluca.allowedops.command.TargetArgument;
import gg.gianluca.allowedops.config.PluginConfig;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.Map;

public final class RemoveSubcommand {

    private static final String PERMISSION = "allowedops.remove";

    private final AllowedOPsPlugin plugin;

    public RemoveSubcommand(final AllowedOPsPlugin plugin) {
        this.plugin = plugin;
    }

    public LiteralArgumentBuilder<CommandSourceStack> build(final Commands registrar) {
        return Commands.literal("remove")
                .requires(source -> CommandRequirements.hasPermission(source, PERMISSION))
                .then(TargetArgument.playerOrUuid("target", () -> plugin.repository().view())
                        .executes(this::execute));
    }

    private int execute(final CommandContext<CommandSourceStack> context) {
        final var target = CommandRequirements.resolveTarget(TargetArgument.getTargetInput(context, "target"));
        if (target.isEmpty()) {
            CommandRequirements.sendError(context.getSource(), "Unknown player or UUID.");
            return 0;
        }
        final CommandRequirements.ResolvedTarget resolved = target.get();

        if (!plugin.repository().remove(resolved.uuid())) {
            CommandRequirements.sendError(context.getSource(), resolved.displayName() + " is not on the allowed OP list.");
            return 0;
        }

        plugin.flushStorage();
        CommandRequirements.sendSuccess(context.getSource(), "Removed " + resolved.displayName() + " (" + resolved.uuidString() + ") from the allowed OP list.");

        plugin.discord().send(
                PluginConfig.DiscordAlert.REMOVE_OP,
                Map.of(
                        "target", resolved.displayName() + " (" + resolved.uuidString() + ")",
                        "executor", CommandRequirements.executorName(context.getSource())
                )
        );
        return 1;
    }
}
