package de.dosmike.spongepowered.oreapi.routes;

import de.dosmike.spongepowered.oreapi.OreApiV2;
import de.dosmike.spongepowered.oreapi.netobject.OrePaginationFilter;
import de.dosmike.spongepowered.oreapi.netobject.OreProjectReference;
import de.dosmike.spongepowered.oreapi.netobject.OreVersion;
import de.dosmike.spongepowered.oreapi.netobject.OreVersionList;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class Versions extends AbstractRoute {

    private OreProjectReference project;

    public Versions(OreApiV2 api, OreProjectReference reference) {
        super(api);
        project = reference.toReference();
    }

    /**
     * @return empty if the connection failed or no such plugin exists
     */
    public CompletableFuture<OreVersionList> list(@Nullable OrePaginationFilter pagination) {
        return enqueue(NetTasks.listVersions(cm(), project, pagination));
    }

    /**
     * @return empty if the connection failed or no such plugin or version exists
     */
    public CompletableFuture<OreVersion> get(String versionName) {
        return cache().version(project.getPluginId(), versionName)
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> enqueue(NetTasks.getVersion(cm(), project, versionName)));
    }

    /**
     * Injects the version changelog into OreVersion through a separate request.
     * To see if the changelog was already fetched, check OreVersion#getChangelog for a non-null value.
     * If the plugin referenced by the version is no longer cached it will be updated and cached again.
     *
     * @return empty on error, or if no changelog is available
     */
    public CompletableFuture<String> changelog(OreVersion version) {
        if (!version.getProjectRef().equals(project))
            throw new IllegalArgumentException("The supplied version does not relate to the referenced project (" + version.getProjectRef().toString() + " vs " + project.toString() + ")");
        return get(version.getName())
                .thenCompose(v -> v.getChangelog()
                        .map(CompletableFuture::completedFuture)
                        .orElseGet(() -> enqueue(NetTasks.getVerionChangelog(cm(), v))));
    }


    /**
     * If the version is not marked as Reviewed with {@link OreVersion#getReviewState()}
     * you should prompt the user with a disclaimer, that installing such plugins might be
     * unsafe and that neither you or the SpongePowered Team is responsible for any damages
     * resulting from the user continuing.
     * THIS IS NOT PART OF THE API (but i include it anyway, because that might be a common goal)
     */
    public CompletableFuture<URL> getDownloadURL(OreVersion version) {
        if (!version.getProjectRef().equals(project))
            throw new IllegalArgumentException("The supplied version does not relate to the referenced project (" + version.getProjectRef().toString() + " vs " + project.toString() + ")");
        return enqueue(NetTasks.getDownloadURL(cm(), version));
    }

}
