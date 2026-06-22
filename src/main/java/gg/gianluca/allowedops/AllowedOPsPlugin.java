package gg.gianluca.allowedops;

import gg.gianluca.allowedops.config.PluginConfig;
import gg.gianluca.allowedops.discord.DiscordNotifier;
import gg.gianluca.allowedops.listener.AllowedOpJoinListener;
import gg.gianluca.allowedops.listener.OpValidateLoginListener;
import gg.gianluca.allowedops.storage.AllowedOpsRepository;
import gg.gianluca.allowedops.command.AllowedOpsCommandRegistrar;
import gg.gianluca.allowedops.scheduler.OpKickScheduler;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class AllowedOPsPlugin extends JavaPlugin {

    private PluginConfig pluginConfig;
    private AllowedOpsRepository repository;
    private DiscordNotifier discordNotifier;
    private BukkitTask saveTask;
    private OpKickScheduler opKickScheduler;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadLocalState();

        getServer().getPluginManager().registerEvents(new OpValidateLoginListener(this), this);
        getServer().getPluginManager().registerEvents(new AllowedOpJoinListener(this), this);
        new AllowedOpsCommandRegistrar(this).register();

        getLogger().info("AllowedOPs enabled with " + repository.size() + " allowed operator(s).");
    }

    @Override
    public void onDisable() {
        cancelSaveTask();
        cancelOpKickTask();
        if (repository != null) {
            repository.saveSync();
            repository.close();
        }
        if (discordNotifier != null) {
            discordNotifier.shutdown();
        }
    }

    public void reloadLocalState() {
        reloadConfig();
        pluginConfig = PluginConfig.from(getConfig());

        cancelSaveTask();
        cancelOpKickTask();

        if (repository != null) {
            repository.saveSync();
            repository.close();
        }

        repository = AllowedOpsRepository.create(this, pluginConfig);
        repository.load();

        if (discordNotifier != null) {
            discordNotifier.shutdown();
        }
        discordNotifier = new DiscordNotifier(this, pluginConfig);

        scheduleSaveTask();
        scheduleOpKickTask();
    }

    public void flushStorage() {
        if (repository != null) {
            repository.saveAsync();
        }
    }

    public PluginConfig pluginConfig() {
        return pluginConfig;
    }

    public AllowedOpsRepository repository() {
        return repository;
    }

    public DiscordNotifier discord() {
        return discordNotifier;
    }

    private void scheduleSaveTask() {
        final long interval = pluginConfig.saveIntervalTicks();
        if (interval <= 0L) {
            return;
        }
        saveTask = getServer().getScheduler().runTaskTimerAsynchronously(
                this,
                () -> {
                    if (repository != null && repository.isDirty()) {
                        repository.saveSync();
                    }
                },
                interval,
                interval
        );
    }

    private void cancelSaveTask() {
        if (saveTask != null) {
            saveTask.cancel();
            saveTask = null;
        }
    }

    private void scheduleOpKickTask() {
        if (opKickScheduler == null) {
            opKickScheduler = new OpKickScheduler(this);
        }

        getServer().getGlobalRegionScheduler().run(this, task -> opKickScheduler.start());
    }

    private void cancelOpKickTask() {
        if (opKickScheduler != null) {
            opKickScheduler.cancel();
        }
    }
}
