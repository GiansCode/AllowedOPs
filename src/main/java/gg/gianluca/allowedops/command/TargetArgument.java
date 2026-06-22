package gg.gianluca.allowedops.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import gg.gianluca.allowedops.AllowedOPsPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class TargetArgument {

    private TargetArgument() {
    }

    public static RequiredArgumentBuilder<CommandSourceStack, String> playerOrUuid(final String name) {
        return playerOrUuid(name, null);
    }

    public static RequiredArgumentBuilder<CommandSourceStack, String> playerOrUuid(
            final String name,
            final Supplier<Iterable<UUID>> extraUuidSuggestions
    ) {
        return Commands.argument(name, StringArgumentType.word())
                .suggests((context, builder) -> suggestPlayersAndUuids(context, builder, extraUuidSuggestions));
    }

    public static String getTargetInput(final CommandContext<CommandSourceStack> context, final String argumentName) {
        return StringArgumentType.getString(context, argumentName);
    }

    private static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestPlayersAndUuids(
            final com.mojang.brigadier.context.CommandContext<CommandSourceStack> context,
            final SuggestionsBuilder builder,
            final Supplier<Iterable<UUID>> extraUuidSuggestions
    ) {
        final String remaining = builder.getRemainingLowerCase();

        for (final var player : Bukkit.getOnlinePlayers()) {
            final String name = player.getName();
            if (remaining.isEmpty() || name.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                builder.suggest(name);
            }
        }

        for (final OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            final String name = offlinePlayer.getName();
            if (name != null && (remaining.isEmpty() || name.toLowerCase(Locale.ROOT).startsWith(remaining))) {
                builder.suggest(name);
            }
        }

        if (extraUuidSuggestions != null) {
            for (final UUID uuid : extraUuidSuggestions.get()) {
                final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                final String name = offlinePlayer.getName();
                if (name != null && (remaining.isEmpty() || name.toLowerCase(Locale.ROOT).startsWith(remaining))) {
                    builder.suggest(name);
                }
                final String uuidString = uuid.toString();
                if (remaining.isEmpty() || uuidString.startsWith(remaining)) {
                    builder.suggest(uuidString);
                }
            }
        }

        if (looksLikePartialUuid(remaining)) {
            for (final OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                final String uuidString = offlinePlayer.getUniqueId().toString();
                if (uuidString.startsWith(remaining)) {
                    builder.suggest(uuidString);
                }
            }
        }

        return builder.buildFuture();
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
}
