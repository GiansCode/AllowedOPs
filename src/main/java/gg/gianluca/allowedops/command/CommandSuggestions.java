package gg.gianluca.allowedops.command;

import gg.gianluca.allowedops.AllowedOPsPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class CommandSuggestions {

    private static final List<Subcommand> SUBCOMMANDS = List.of(
            new Subcommand("add", "allowedops.add"),
            new Subcommand("remove", "allowedops.remove"),
            new Subcommand("list", "allowedops.list"),
            new Subcommand("reload", "allowedops.reload")
    );

    private CommandSuggestions() {
    }

    public static List<String> suggest(
            final AllowedOPsPlugin plugin,
            final CommandSourceStack source,
            final String[] args
    ) {
        if (!CommandRequirements.canUseRoot(plugin, source.getSender())) {
            return List.of();
        }

        if (args.length <= 1) {
            final String prefix = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
            return SUBCOMMANDS.stream()
                    .filter(subcommand -> CommandRequirements.hasPermission(source.getSender(), subcommand.permission()))
                    .map(Subcommand::name)
                    .filter(name -> name.startsWith(prefix))
                    .toList();
        }

        if (args.length == 2) {
            final String subcommand = args[0].toLowerCase(Locale.ROOT);
            final String prefix = args[1].toLowerCase(Locale.ROOT);

            if ("add".equals(subcommand) && CommandRequirements.hasPermission(source.getSender(), "allowedops.add")) {
                return suggestTargets(plugin, prefix, false);
            }

            if ("remove".equals(subcommand) && CommandRequirements.hasPermission(source.getSender(), "allowedops.remove")) {
                return suggestTargets(plugin, prefix, true);
            }
        }

        return List.of();
    }

    private static List<String> suggestTargets(
            final AllowedOPsPlugin plugin,
            final String prefix,
            final boolean allowedOnly
    ) {
        final List<String> suggestions = new ArrayList<>();

        if (allowedOnly) {
            for (final UUID uuid : plugin.repository().view()) {
                addTargetSuggestion(suggestions, prefix, uuid);
            }
        }

        for (final var player : Bukkit.getOnlinePlayers()) {
            addNameSuggestion(suggestions, prefix, player.getName());
        }

        for (final OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName() != null) {
                addNameSuggestion(suggestions, prefix, offlinePlayer.getName());
            }
        }

        if (looksLikePartialUuid(prefix)) {
            for (final OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                addUuidSuggestion(suggestions, prefix, offlinePlayer.getUniqueId());
            }
        }

        return suggestions;
    }

    private static void addTargetSuggestion(final List<String> suggestions, final String prefix, final UUID uuid) {
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (offlinePlayer.getName() != null) {
            addNameSuggestion(suggestions, prefix, offlinePlayer.getName());
        }
        addUuidSuggestion(suggestions, prefix, uuid);
    }

    private static void addNameSuggestion(final List<String> suggestions, final String prefix, final String name) {
        if (name.toLowerCase(Locale.ROOT).startsWith(prefix)) {
            suggestions.add(name);
        }
    }

    private static void addUuidSuggestion(final List<String> suggestions, final String prefix, final UUID uuid) {
        final String uuidString = uuid.toString();
        if (uuidString.startsWith(prefix)) {
            suggestions.add(uuidString);
        }
    }

    private static boolean looksLikePartialUuid(final String value) {
        if (value.isEmpty()) {
            return false;
        }
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            if ((character < '0' || character > '9')
                    && (character < 'a' || character > 'f')
                    && character != '-') {
                return false;
            }
        }
        return true;
    }

    private record Subcommand(String name, String permission) {
    }
}
