package gg.gianluca.allowedops.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import gg.gianluca.allowedops.AllowedOPsPlugin;
import gg.gianluca.allowedops.command.sub.AddSubcommand;
import gg.gianluca.allowedops.command.sub.ListSubcommand;
import gg.gianluca.allowedops.command.sub.ReloadSubcommand;
import gg.gianluca.allowedops.command.sub.RemoveSubcommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public final class AllowedOpsCommandRegistrar {

    private final AllowedOPsPlugin plugin;

    public AllowedOpsCommandRegistrar(final AllowedOPsPlugin plugin) {
        this.plugin = plugin;
    }

    public void register(final Commands registrar) {
        final LiteralCommandNode<CommandSourceStack> root = Commands.literal("allowedops")
                .requires(source -> CommandRequirements.canUseRoot(plugin, source))
                .then(new AddSubcommand(plugin).build(registrar))
                .then(new RemoveSubcommand(plugin).build(registrar))
                .then(new ListSubcommand(plugin).build(registrar))
                .then(new ReloadSubcommand(plugin).build(registrar))
                .build();

        registrar.register(root, "Manage the allowed operator list");
    }
}
