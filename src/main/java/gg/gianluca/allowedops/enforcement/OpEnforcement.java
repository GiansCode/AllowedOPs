package gg.gianluca.allowedops.enforcement;

import gg.gianluca.allowedops.AllowedOPsPlugin;
import gg.gianluca.allowedops.config.PluginConfig;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public final class OpEnforcement {

    private OpEnforcement() {
    }

    public static boolean shouldKick(final AllowedOPsPlugin plugin, final UUID uuid, final boolean op) {
        return op && !plugin.repository().isAllowed(uuid);
    }

    public static void enforceOnlineOp(final AllowedOPsPlugin plugin, final Player player) {
        if (!shouldKick(plugin, player.getUniqueId(), player.isOp())) {
            return;
        }

        final var kickMessage = plugin.pluginConfig().kickMessageComponent();
        player.getScheduler().run(
                plugin,
                task -> kickIfStillUnauthorized(plugin, player, kickMessage),
                () -> plugin.getServer().getGlobalRegionScheduler().execute(
                        plugin,
                        () -> kickIfStillUnauthorized(plugin, player, kickMessage)
                )
        );
    }

    private static void kickIfStillUnauthorized(
            final AllowedOPsPlugin plugin,
            final Player player,
            final net.kyori.adventure.text.Component kickMessage
    ) {
        if (!player.isOnline() || !shouldKick(plugin, player.getUniqueId(), player.isOp())) {
            return;
        }

        player.kick(kickMessage);
        notifyUnauthorizedOp(plugin, player.getName(), player.getUniqueId());
    }

    public static void notifyUnauthorizedOp(final AllowedOPsPlugin plugin, final String name, final UUID uuid) {
        plugin.discord().send(
                PluginConfig.DiscordAlert.UNAUTHORIZED_OP,
                Map.of(
                        "player", name,
                        "uuid", uuid.toString()
                )
        );
    }
}
