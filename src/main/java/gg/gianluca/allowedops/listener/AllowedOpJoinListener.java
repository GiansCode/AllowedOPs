package gg.gianluca.allowedops.listener;

import gg.gianluca.allowedops.AllowedOPsPlugin;
import gg.gianluca.allowedops.config.PluginConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;

public final class AllowedOpJoinListener implements Listener {

    private final AllowedOPsPlugin plugin;

    public AllowedOpJoinListener(final AllowedOPsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final var player = event.getPlayer();
        if (!player.isOp() || !plugin.repository().isAllowed(player.getUniqueId())) {
            return;
        }

        plugin.discord().send(
                PluginConfig.DiscordAlert.ALLOWED_OP_JOIN,
                Map.of(
                        "player", player.getName(),
                        "uuid", player.getUniqueId().toString()
                )
        );
    }
}
