package gg.gianluca.allowedops.config;

import gg.gianluca.allowedops.storage.StorageType;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.EnumMap;
import java.util.Map;

public final class PluginConfig {

    public enum DiscordAlert {
        UNAUTHORIZED_OP,
        ALLOWED_OP_JOIN,
        ADD_OP,
        REMOVE_OP,
        RELOAD
    }

    private final boolean onlyConsoleCanExecute;
    private final StorageType storageType;
    private final String sqlJdbcUrl;
    private final String sqlUsername;
    private final String sqlPassword;
    private final String sqlTable;
    private final int sqlPoolSize;
    private final String kickMessage;
    private final long saveIntervalTicks;
    private final boolean discordEnabled;
    private final String discordWebhookUrl;
    private final Map<DiscordAlert, Boolean> discordAlerts;
    private final Map<DiscordAlert, String> discordMessages;

    private PluginConfig(
            final boolean onlyConsoleCanExecute,
            final StorageType storageType,
            final String sqlJdbcUrl,
            final String sqlUsername,
            final String sqlPassword,
            final String sqlTable,
            final int sqlPoolSize,
            final String kickMessage,
            final long saveIntervalTicks,
            final boolean discordEnabled,
            final String discordWebhookUrl,
            final Map<DiscordAlert, Boolean> discordAlerts,
            final Map<DiscordAlert, String> discordMessages
    ) {
        this.onlyConsoleCanExecute = onlyConsoleCanExecute;
        this.storageType = storageType;
        this.sqlJdbcUrl = sqlJdbcUrl;
        this.sqlUsername = sqlUsername;
        this.sqlPassword = sqlPassword;
        this.sqlTable = sqlTable;
        this.sqlPoolSize = sqlPoolSize;
        this.kickMessage = kickMessage;
        this.saveIntervalTicks = saveIntervalTicks;
        this.discordEnabled = discordEnabled;
        this.discordWebhookUrl = discordWebhookUrl;
        this.discordAlerts = discordAlerts;
        this.discordMessages = discordMessages;
    }

    public static PluginConfig from(final FileConfiguration config) {
        final Map<DiscordAlert, Boolean> alerts = new EnumMap<>(DiscordAlert.class);
        alerts.put(DiscordAlert.UNAUTHORIZED_OP, config.getBoolean("discord.alerts.unauthorized-op", true));
        alerts.put(DiscordAlert.ALLOWED_OP_JOIN, config.getBoolean("discord.alerts.allowed-op-join", false));
        alerts.put(DiscordAlert.ADD_OP, config.getBoolean("discord.alerts.add-op", true));
        alerts.put(DiscordAlert.REMOVE_OP, config.getBoolean("discord.alerts.remove-op", true));
        alerts.put(DiscordAlert.RELOAD, config.getBoolean("discord.alerts.reload", false));

        final Map<DiscordAlert, String> messages = new EnumMap<>(DiscordAlert.class);
        messages.put(DiscordAlert.UNAUTHORIZED_OP, config.getString("discord.messages.unauthorized-op", ""));
        messages.put(DiscordAlert.ALLOWED_OP_JOIN, config.getString("discord.messages.allowed-op-join", ""));
        messages.put(DiscordAlert.ADD_OP, config.getString("discord.messages.add-op", ""));
        messages.put(DiscordAlert.REMOVE_OP, config.getString("discord.messages.remove-op", ""));
        messages.put(DiscordAlert.RELOAD, config.getString("discord.messages.reload", ""));

        return new PluginConfig(
                config.getBoolean("only-console-can-execute", true),
                StorageType.from(config.getString("storage.type", "flatfile")),
                config.getString("sql.jdbc-url", ""),
                config.getString("sql.username", ""),
                config.getString("sql.password", ""),
                config.getString("sql.table", "allowed_ops"),
                Math.max(1, config.getInt("sql.pool-size", 2)),
                config.getString("kick-message", "&cYou are not authorized to have operator privileges on this server."),
                config.getLong("save-interval-ticks", 6000L),
                config.getBoolean("discord.enabled", false),
                config.getString("discord.webhook-url", ""),
                alerts,
                messages
        );
    }

    public boolean onlyConsoleCanExecute() {
        return onlyConsoleCanExecute;
    }

    public StorageType storageType() {
        return storageType;
    }

    public String sqlJdbcUrl() {
        return sqlJdbcUrl;
    }

    public String sqlUsername() {
        return sqlUsername;
    }

    public String sqlPassword() {
        return sqlPassword;
    }

    public String sqlTable() {
        return sqlTable;
    }

    public int sqlPoolSize() {
        return sqlPoolSize;
    }

    public String kickMessage() {
        return kickMessage;
    }

    public net.kyori.adventure.text.Component kickMessageComponent() {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(kickMessage);
    }

    public long saveIntervalTicks() {
        return saveIntervalTicks;
    }

    public boolean discordEnabled() {
        return discordEnabled;
    }

    public String discordWebhookUrl() {
        return discordWebhookUrl;
    }

    public boolean discordAlertEnabled(final DiscordAlert alert) {
        return discordAlerts.getOrDefault(alert, false);
    }

    public String discordMessage(final DiscordAlert alert) {
        return discordMessages.getOrDefault(alert, "");
    }
}
