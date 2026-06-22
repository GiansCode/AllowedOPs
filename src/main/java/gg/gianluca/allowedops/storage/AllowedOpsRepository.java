package gg.gianluca.allowedops.storage;

import gg.gianluca.allowedops.AllowedOPsPlugin;
import gg.gianluca.allowedops.config.PluginConfig;
import org.bukkit.Bukkit;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public final class AllowedOpsRepository {

    private final AllowedOPsPlugin plugin;
    private final AllowedOpsStorage backend;
    private final Set<UUID> allowedOps = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean dirty = new AtomicBoolean(false);

    private AllowedOpsRepository(final AllowedOPsPlugin plugin, final AllowedOpsStorage backend) {
        this.plugin = plugin;
        this.backend = backend;
    }

    public static AllowedOpsRepository create(final AllowedOPsPlugin plugin, final PluginConfig config) {
        final AllowedOpsStorage backend = switch (config.storageType()) {
            case SQL -> new SqlStorage(plugin, config);
            case FLATFILE -> new FlatFileStorage(plugin);
        };
        return new AllowedOpsRepository(plugin, backend);
    }

    public void load() {
        allowedOps.clear();
        allowedOps.addAll(backend.loadAll());
        dirty.set(false);
    }

    public boolean isAllowed(final UUID uuid) {
        return allowedOps.contains(uuid);
    }

    public boolean add(final UUID uuid) {
        if (!allowedOps.add(uuid)) {
            return false;
        }
        markDirty();
        return true;
    }

    public boolean remove(final UUID uuid) {
        if (!allowedOps.remove(uuid)) {
            return false;
        }
        markDirty();
        return true;
    }

    public Set<UUID> snapshot() {
        return Set.copyOf(allowedOps);
    }

    public int size() {
        return allowedOps.size();
    }

    public Collection<UUID> view() {
        return allowedOps;
    }

    public boolean isDirty() {
        return dirty.get();
    }

    public void markDirty() {
        dirty.set(true);
    }

    public void saveSync() {
        if (!dirty.compareAndSet(true, false)) {
            return;
        }
        try {
            backend.saveAll(allowedOps);
        } catch (Exception exception) {
            dirty.set(true);
            plugin.getLogger().log(Level.SEVERE, "Failed to save allowed OP list", exception);
        }
    }

    public void saveAsync() {
        if (!dirty.get()) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveSync);
    }

    public void close() {
        backend.close();
    }
}
