package gg.gianluca.allowedops.listener;

import gg.gianluca.allowedops.AllowedOPsPlugin;
import gg.gianluca.allowedops.enforcement.OpEnforcement;
import io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

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
        if (!OpEnforcement.shouldKick(plugin, snapshot.uuid(), Bukkit.getOfflinePlayer(snapshot.uuid()).isOp())) {
            return;
        }

        event.kickMessage(plugin.pluginConfig().kickMessageComponent());
        OpEnforcement.notifyUnauthorizedOp(plugin, snapshot.name(), snapshot.uuid());
    }
}
