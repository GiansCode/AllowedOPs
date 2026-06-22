package gg.gianluca.allowedops.command;

import gg.gianluca.allowedops.AllowedOPsPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class CommandRequirements {

    private CommandRequirements() {
    }

    public static boolean canUseRoot(final AllowedOPsPlugin plugin, final CommandSourceStack source) {
        if (plugin.pluginConfig().onlyConsoleCanExecute()) {
            return source.getSender() instanceof org.bukkit.command.ConsoleCommandSender;
        }
        return true;
    }

    public static boolean hasPermission(final CommandSourceStack source, final String permission) {
        return source.getSender().hasPermission(permission);
    }

    public static void sendError(final CommandSourceStack source, final String message) {
        source.getSender().sendMessage(Component.text(message, NamedTextColor.RED));
    }

    public static void sendSuccess(final CommandSourceStack source, final String message) {
        source.getSender().sendMessage(Component.text(message, NamedTextColor.GREEN));
    }

    public static void sendInfo(final CommandSourceStack source, final String message) {
        source.getSender().sendMessage(Component.text(message, NamedTextColor.YELLOW));
    }

    public static String executorName(final CommandSourceStack source) {
        final CommandSender sender = source.getSender();
        if (sender instanceof org.bukkit.command.ConsoleCommandSender) {
            return "Console";
        }
        return sender.getName();
    }

    public static Optional<ResolvedTarget> resolveTarget(final String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }

        final String trimmed = input.trim();

        try {
            final UUID uuid = UUID.fromString(trimmed);
            return Optional.of(new ResolvedTarget(uuid, resolveName(uuid, trimmed)));
        } catch (IllegalArgumentException ignored) {
            // Not a UUID, resolve as player name.
        }

        final Player online = Bukkit.getPlayerExact(trimmed);
        if (online != null) {
            return Optional.of(new ResolvedTarget(online.getUniqueId(), online.getName()));
        }

        final OfflinePlayer cached = Bukkit.getOfflinePlayerIfCached(trimmed);
        if (cached != null) {
            final String name = cached.getName() != null ? cached.getName() : trimmed;
            return Optional.of(new ResolvedTarget(cached.getUniqueId(), name));
        }

        return Optional.empty();
    }

    private static String resolveName(final UUID uuid, final String fallback) {
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        return offlinePlayer.getName() != null ? offlinePlayer.getName() : fallback;
    }

    public record ResolvedTarget(UUID uuid, String displayName) {
        public String uuidString() {
            return uuid.toString();
        }

        public String lowerDisplayName() {
            return displayName == null ? uuidString() : displayName.toLowerCase(Locale.ROOT);
        }
    }
}
