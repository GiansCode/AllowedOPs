package gg.gianluca.allowedops.storage;

import gg.gianluca.allowedops.AllowedOPsPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class FlatFileStorage implements AllowedOpsStorage {

    private final File file;

    public FlatFileStorage(final AllowedOPsPlugin plugin) {
        this.file = new File(plugin.getDataFolder(), "allowed-ops.yml");
    }

    @Override
    public Set<UUID> loadAll() {
        if (!file.exists()) {
            return Set.of();
        }

        final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        final List<String> raw = yaml.getStringList("allowed-ops");
        final Set<UUID> result = HashSet.newHashSet(raw.size());

        for (final String entry : raw) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            try {
                result.add(UUID.fromString(entry.trim()));
            } catch (IllegalArgumentException ignored) {
                // Skip invalid entries rather than failing startup.
            }
        }
        return result;
    }

    @Override
    public void saveAll(final Collection<UUID> allowedOps) {
        final File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Could not create plugin data folder: " + parent.getAbsolutePath());
        }

        final YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("allowed-ops", allowedOps.stream().map(UUID::toString).sorted().toList());

        try {
            yaml.save(file);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write " + file.getAbsolutePath(), exception);
        }
    }
}
