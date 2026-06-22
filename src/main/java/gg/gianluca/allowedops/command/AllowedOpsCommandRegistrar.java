package gg.gianluca.allowedops.command;

import gg.gianluca.allowedops.AllowedOPsPlugin;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

public final class AllowedOpsCommandRegistrar {

    private final AllowedOPsPlugin plugin;

    public AllowedOpsCommandRegistrar(final AllowedOPsPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register(
                        "allowedops",
                        "Manage the allowed operator list",
                        new AllowedOpsCommand(plugin)
                ));
    }
}
