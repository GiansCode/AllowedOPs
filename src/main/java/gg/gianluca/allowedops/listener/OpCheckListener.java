package gg.gianluca.allowedops.listener;

import gg.gianluca.allowedops.AllowedOPsPlugin;
import gg.gianluca.allowedops.config.PluginConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;

public final class OpCheckListener implements Listener {

    private final AllowedOPsPlugin plugin;

    public OpCheckListener(final AllowedOPsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final var player = event.getPlayer();
        if (!player.isOp()) {
            return;
        }

        if (plugin.repository().isAllowed(player.getUniqueId())) {
            plugin.discord().send(
                    PluginConfig.DiscordAlert.ALLOWED_OP_JOIN,
                    Map.of(
                            "player", player.getName(),
                            "uuid", player.getUniqueId().toString()
                    )
            );
            return;
        }

        player.kick(plugin.pluginConfig().kickMessageComponent());
        plugin.discord().send(
                PluginConfig.DiscordAlert.UNAUTHORIZED_OP,
                Map.of(
                        "player", player.getName(),
                        "uuid", player.getUniqueId().toString()
                )
        );
    }
}
