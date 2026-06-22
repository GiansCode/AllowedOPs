package gg.gianluca.allowedops.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gg.gianluca.allowedops.AllowedOPsPlugin;
import gg.gianluca.allowedops.config.PluginConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public final class SqlStorage implements AllowedOpsStorage {

    private final AllowedOPsPlugin plugin;
    private final String tableName;
    private HikariDataSource dataSource;

    public SqlStorage(final AllowedOPsPlugin plugin, final PluginConfig config) {
        this.plugin = plugin;
        this.tableName = sanitizeTableName(config.sqlTable());
        initPool(config);
        ensureTable();
    }

    private void initPool(final PluginConfig config) {
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.sqlJdbcUrl());
        hikariConfig.setUsername(config.sqlUsername());
        hikariConfig.setPassword(config.sqlPassword());
        hikariConfig.setMaximumPoolSize(config.sqlPoolSize());
        hikariConfig.setPoolName("AllowedOPs-Pool");
        hikariConfig.setAutoCommit(true);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "16");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        this.dataSource = new HikariDataSource(hikariConfig);
    }

    private void ensureTable() {
        final String ddl = """
                CREATE TABLE IF NOT EXISTS %s (
                    uuid CHAR(36) NOT NULL PRIMARY KEY
                )
                """.formatted(tableName);

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(ddl);
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize SQL table", exception);
            throw new IllegalStateException("Could not initialize SQL storage", exception);
        }
    }

    @Override
    public Set<UUID> loadAll() {
        final String query = "SELECT uuid FROM " + tableName;
        final Set<UUID> result = new HashSet<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                try {
                    result.add(UUID.fromString(resultSet.getString("uuid")));
                } catch (IllegalArgumentException ignored) {
                    // Skip malformed rows.
                }
            }
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load allowed OP list from SQL", exception);
            throw new IllegalStateException("Could not load SQL storage", exception);
        }

        return result;
    }

    @Override
    public void saveAll(final Collection<UUID> allowedOps) {
        final String deleteAll = "DELETE FROM " + tableName;
        final String insert = "INSERT INTO " + tableName + " (uuid) VALUES (?)";

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (Statement deleteStatement = connection.createStatement()) {
                deleteStatement.executeUpdate(deleteAll);
            }

            try (PreparedStatement insertStatement = connection.prepareStatement(insert)) {
                for (final UUID uuid : allowedOps) {
                    insertStatement.setString(1, uuid.toString());
                    insertStatement.addBatch();
                }
                insertStatement.executeBatch();
            }

            connection.commit();
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save allowed OP list to SQL", exception);
            throw new IllegalStateException("Could not save SQL storage", exception);
        }
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private static String sanitizeTableName(final String tableName) {
        if (tableName == null || !tableName.matches("^[A-Za-z_][A-Za-z0-9_]*$")) {
            throw new IllegalArgumentException("Invalid SQL table name: " + tableName);
        }
        return tableName;
    }
}
