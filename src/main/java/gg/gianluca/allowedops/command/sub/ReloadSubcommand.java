package gg.gianluca.allowedops.command.sub;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import gg.gianluca.allowedops.AllowedOPsPlugin;
import gg.gianluca.allowedops.command.CommandRequirements;
import gg.gianluca.allowedops.config.PluginConfig;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.Map;

public final class ReloadSubcommand {

    private static final String PERMISSION = "allowedops.reload";

    private final AllowedOPsPlugin plugin;

    public ReloadSubcommand(final AllowedOPsPlugin plugin) {
        this.plugin = plugin;
    }

    public LiteralArgumentBuilder<CommandSourceStack> build(final Commands registrar) {
        return Commands.literal("reload")
                .requires(source -> CommandRequirements.hasPermission(source, PERMISSION))
                .executes(this::execute);
    }

    private int execute(final CommandContext<CommandSourceStack> context) {
        plugin.reloadLocalState();
        plugin.discord().updateConfig(plugin.pluginConfig());

        CommandRequirements.sendSuccess(context.getSource(), "AllowedOPs configuration and data reloaded.");
        plugin.discord().send(
                PluginConfig.DiscordAlert.RELOAD,
                Map.of("executor", CommandRequirements.executorName(context.getSource()))
        );
        return 1;
    }
}
