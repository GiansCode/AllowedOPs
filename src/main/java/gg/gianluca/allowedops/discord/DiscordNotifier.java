package gg.gianluca.allowedops.discord;

import gg.gianluca.allowedops.AllowedOPsPlugin;
import gg.gianluca.allowedops.config.PluginConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class DiscordNotifier {

    private final AllowedOPsPlugin plugin;
    private volatile PluginConfig config;
    private final HttpClient httpClient;
    private final ExecutorService executor;

    public DiscordNotifier(final AllowedOPsPlugin plugin, final PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.executor = Executors.newSingleThreadExecutor(runnable -> {
            final Thread thread = new Thread(runnable, "AllowedOPs-Discord");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void updateConfig(final PluginConfig config) {
        this.config = config;
    }

    public void send(final PluginConfig.DiscordAlert alert, final Map<String, String> placeholders) {
        final PluginConfig current = this.config;
        if (!current.discordEnabled() || !current.discordAlertEnabled(alert)) {
            return;
        }

        final String webhookUrl = current.discordWebhookUrl();
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        String message = current.discordMessage(alert);
        if (message == null || message.isBlank()) {
            return;
        }

        for (final Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        final String payload = buildPayload(message);
        executor.execute(() -> post(webhookUrl, payload));
    }

    private void post(final String webhookUrl, final String payload) {
        try {
            final HttpRequest request = HttpRequest.newBuilder(URI.create(webhookUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            final HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                plugin.getLogger().warning("Discord webhook returned HTTP " + response.statusCode());
            }
        } catch (Exception exception) {
            plugin.getLogger().warning("Failed to send Discord webhook: " + exception.getMessage());
        }
    }

    private static String buildPayload(final String content) {
        final String escaped = content
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
        return "{\"content\":\"" + escaped + "\"}";
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}
