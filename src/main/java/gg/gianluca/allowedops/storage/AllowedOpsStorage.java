package gg.gianluca.allowedops.storage;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface AllowedOpsStorage {

    Set<UUID> loadAll();

    void saveAll(Collection<UUID> allowedOps);

    default void close() {
    }
}
