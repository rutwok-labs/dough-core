package io.github.bakedlibs.dough.updater;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;

import javax.annotation.Nonnull;

import org.bukkit.plugin.Plugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.bakedlibs.dough.versions.SemanticVersion;

public class ModrinthUpdater extends AbstractPluginUpdater<SemanticVersion> {

    private static final String API_BASE = "https://api.modrinth.com/v2/project/";

    private final String projectId;

    public ModrinthUpdater(@Nonnull Plugin plugin, @Nonnull File file, @Nonnull String projectId, @Nonnull SemanticVersion currentVersion) {
        super(plugin, file, currentVersion);
        this.projectId = projectId;
    }

    @Override
    public void start() {
        try {
            URL url = new URI(API_BASE + projectId + "/version").toURL();

            scheduleAsyncUpdateTask(new UpdaterTask<SemanticVersion>(this, url) {

                @Override
                public UpdateInfo parse(String result) throws MalformedURLException, URISyntaxException {
                    JsonArray versions = JsonParser.parseString(result).getAsJsonArray();

                    if (versions == null || versions.isEmpty()) {
                        getLogger().log(Level.WARNING, "Modrinth returned no versions for: {0}", projectId);
                        return null;
                    }

                    JsonObject latest = versions.get(0).getAsJsonObject();
                    String versionNumber = latest.get("version_number").getAsString();
                    SemanticVersion latestVersion = SemanticVersion.parse(versionNumber);
                    getLatestVersion().complete(latestVersion);

                    JsonObject file = latest.getAsJsonArray("files").get(0).getAsJsonObject();
                    URL downloadUrl = new URI(file.get("url").getAsString()).toURL();
                    return new UpdateInfo(downloadUrl, latestVersion);
                }
            });
        } catch (MalformedURLException | URISyntaxException e) {
            getLogger().log(Level.SEVERE, "Modrinth updater URL is malformed", e);
        }
    }
}
