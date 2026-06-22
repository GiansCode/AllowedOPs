package gg.gianluca.allowedops.scheduler;

import gg.gianluca.allowedops.AllowedOPsPlugin;
import gg.gianluca.allowedops.enforcement.OpEnforcement;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Player;

public final class OpKickScheduler {

    private final AllowedOPsPlugin plugin;
    private ScheduledTask task;

    public OpKickScheduler(final AllowedOPsPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        cancel();

        if (!plugin.pluginConfig().checkOpKickSchedulerEnabled()) {
            return;
        }

        final long interval = plugin.pluginConfig().checkOpKickSchedulerIntervalTicks();
        if (interval <= 0L) {
            return;
        }

        task = plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(
                plugin,
                scheduledTask -> checkOnlineOps(),
                1L,
                interval
        );

        plugin.getLogger().info("OP kick scheduler running every " + interval + " tick(s).");
        checkOnlineOps();
    }

    public void cancel() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void checkOnlineOps() {
        for (final Player player : plugin.getServer().getOnlinePlayers()) {
            OpEnforcement.enforceOnlineOp(plugin, player);
        }
    }
}
