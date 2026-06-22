package gg.gianluca.allowedops.listener;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.connection.PlayerConnection;
import io.papermc.paper.connection.PlayerLoginConnection;

import java.util.Optional;
import java.util.UUID;

final class LoginProfiles {

    record Snapshot(UUID uuid, String name) {
    }

    private LoginProfiles() {
    }

    static Optional<Snapshot> fromConnection(final PlayerConnection connection) {
        if (connection instanceof PlayerLoginConnection loginConnection) {
            PlayerProfile profile = loginConnection.getAuthenticatedProfile();
            if (profile == null) {
                profile = loginConnection.getUnsafeProfile();
            }
            return fromPlayerProfile(profile);
        }

        if (connection instanceof PlayerConfigurationConnection configurationConnection) {
            return fromPlayerProfile(configurationConnection.getProfile());
        }

        return Optional.empty();
    }

    private static Optional<Snapshot> fromPlayerProfile(final PlayerProfile profile) {
        if (profile == null) {
            return Optional.empty();
        }

        final UUID uuid = profile.getId();
        if (uuid == null) {
            return Optional.empty();
        }

        final String name = profile.getName() != null ? profile.getName() : uuid.toString();
        return Optional.of(new Snapshot(uuid, name));
    }
}
