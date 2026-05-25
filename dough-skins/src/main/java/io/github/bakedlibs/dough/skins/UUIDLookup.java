package io.github.bakedlibs.dough.skins;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang.Validate;
import org.bukkit.plugin.Plugin;

import io.github.bakedlibs.dough.common.DoughLogger;

public class UUIDLookup {

    private static final Pattern UUID_PATTERN = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");
    private static final JsonParser JSON_PARSER = new JsonParser();
    private static final String ERROR_TOKEN = "error";
    // \w matches [a-zA-Z0-9_] - underscores and digits at any position
    // are valid Minecraft usernames. Min 3, max 16 characters.
    private static final Pattern NAME_PATTERN = Pattern.compile("\\w{3,16}");
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static volatile boolean cacheEnabled = false;
    private static volatile Duration cacheTtl = Duration.ofMinutes(30);
    private static final Map<String, CacheEntry> CACHE = new ConcurrentHashMap<>();

    private record CacheEntry(UUID uuid, long expiresAt) {}

    private UUIDLookup() {}

    public static void enableCache(@Nonnull Duration ttl) {
        cacheTtl = ttl;
        cacheEnabled = true;
    }

    public static void disableCache() {
        cacheEnabled = false;
        CACHE.clear();
    }

    /**
     *  Returns the {@link CompletableFuture} with the {@link UUID }
     *
     * @param plugin plugin invoking this function
     * @param name username of the player
     * @return {@link CompletableFuture} with the {@link UUID }
     */
    @ParametersAreNonnullByDefault
    public static @Nonnull CompletableFuture<UUID> getUuidFromUsername(Plugin plugin, String name) {
        Validate.notNull(plugin, "The plugin instance must not be null!");
        Validate.notNull(name, "The username cannot be null!");

        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("\"" + name + "\" is not a valid Minecraft Username!");
        }

        String cacheKey = name.toLowerCase();

        if (cacheEnabled) {
            CacheEntry entry = CACHE.get(cacheKey);

            if (entry != null && System.currentTimeMillis() < entry.expiresAt()) {
                return CompletableFuture.completedFuture(entry.uuid());
            }
        }

        String targetUrl = "https://playerdb.co/api/player/minecraft/" + name;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .timeout(Duration.ofMinutes(2))
                .header("user-agent", "Mozilla/5.0 Dough (+https://github.com/baked-libs/dough)")
                .GET()
                .build();

        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(s -> JSON_PARSER.parse(s).getAsJsonObject())
                .thenApply(jsonObject -> {
                    if (jsonObject.get("success").getAsBoolean()) {
                        JsonObject data = jsonObject.getAsJsonObject("data");
                        JsonObject player = data.getAsJsonObject("player");
                        return UUID.fromString(player.get("id").getAsString());
                    } else {
                        return null;
                    }
                })
                .thenApply(uuid -> {
                    if (uuid != null && cacheEnabled) {
                        CACHE.put(cacheKey, new CacheEntry(uuid, System.currentTimeMillis() + cacheTtl.toMillis()));
                    }

                    return uuid;
                });
    }

    /**
     *  Returns the {@link CompletableFuture} with the {@link UUID }
     *
     * @param plugin plugin invoking this function
     * @param name username of the player
     * @return {@link CompletableFuture} with the {@link UUID }
     * @deprecated Mojang's sessions API is rate-limited to ~200 req/minute
     *             per IP. Use {@link #getUuidFromUsername(Plugin, String)}
     *             which hits playerdb.co instead and has a more generous rate limit.
     */
    @Deprecated(since = "1.3.1")
    @ParametersAreNonnullByDefault
    public static @Nonnull CompletableFuture<UUID> forUsername(Plugin plugin, String name) {
        Validate.notNull(plugin, "The plugin instance must not be null!");
        Validate.notNull(name, "The username cannot be null!");

        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("\"" + name + "\" is not a valid Minecraft Username!");
        }

        CompletableFuture<UUID> future = new CompletableFuture<>();
        DoughLogger logger = new DoughLogger(plugin.getServer(), "skins");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String targetUrl = "https://api.mojang.com/users/profiles/minecraft/" + name;

            try (InputStreamReader reader = new InputStreamReader(new URL(targetUrl).openStream(), StandardCharsets.UTF_8)) {
                JsonElement element = JSON_PARSER.parse(reader);

                if (!(element instanceof JsonNull)) {
                    JsonObject obj = element.getAsJsonObject();

                    if (obj.has(ERROR_TOKEN)) {
                        String error = obj.get(ERROR_TOKEN).getAsString();
                        future.completeExceptionally(new UnsupportedOperationException(error));
                    }

                    String id = obj.get("id").getAsString();
                    future.complete(UUID.fromString(UUID_PATTERN.matcher(id).replaceAll("$1-$2-$3-$4-$5")));
                }
            } catch (MalformedURLException e) {
                logger.log(Level.SEVERE, "Malformed sessions url: {0}", targetUrl);
                future.completeExceptionally(e);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Exception while requesting skin: {0}", targetUrl);
                future.completeExceptionally(e);
            }
        });

        return future;
    }

}
