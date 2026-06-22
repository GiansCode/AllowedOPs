package gg.gianluca.allowedops.listener;

import gg.gianluca.allowedops.AllowedOPsPlugin;
import gg.gianluca.allowedops.config.PluginConfig;
import io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;

public final class OpValidateLoginListener implements Listener {

    private final AllowedOPsPlugin plugin;

    public OpValidateLoginListener(final AllowedOPsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onValidateLogin(final PlayerConnectionValidateLoginEvent event) {
        if (!event.isAllowed()) {
            return;
        }

        final var profile = LoginProfiles.fromConnection(event.getConnection());
        if (profile.isEmpty()) {
            return;
        }

        final var snapshot = profile.get();
        if (!Bukkit.getOfflinePlayer(snapshot.uuid()).isOp()) {
            return;
        }

        if (plugin.repository().isAllowed(snapshot.uuid())) {
            return;
        }

        event.kickMessage(plugin.pluginConfig().kickMessageComponent());
        plugin.discord().send(
                PluginConfig.DiscordAlert.UNAUTHORIZED_OP,
                Map.of(
                        "player", snapshot.name(),
                        "uuid", snapshot.uuid().toString()
                )
        );
    }
}
