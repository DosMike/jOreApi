package de.dosmike.spongepowered.oreapi.routes;

import de.dosmike.spongepowered.oreapi.OreApiV2;
import de.dosmike.spongepowered.oreapi.netobject.*;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.nio.file.Path;
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
     * Scan a plugin file for future upload. Use this before uploading a version to see which tags Ore will
     * assign the file. Requires the {@link OrePermission#Create_Version} in the project or owning organization.<br>
     * Please note that the resulting version is not cached for obvious reason.
     *
     * @param file the file to scan
     * @return a OreVersion for inspection
     */
    public CompletableFuture<OreVersion> scan(Path file) {
        return enqueue(NetTasks.scanVersion(cm(), project, file));
    }

    /**
     * Creates a new version for a project. Requires the {@link OrePermission#Create_Version} in the
     * project or owning organization.
     *
     * @param deployVersionInfo further information regarding this new version
     * @param file              the file to upload
     * @return the created OreVersion
     */
    public CompletableFuture<OreVersion> create(OreDeployVersionInfo deployVersionInfo, Path file) {
        return enqueue(NetTasks.createVersion(cm(), project, deployVersionInfo, file));
    }

    /**
     * If you want to save changes in the repository, you'll have to update it through this method.
     * Update a version. Please note that everything you can edit on a version is a limited set
     * of data withing the tags, namely stability, release type and platforms
     *
     * @param version the version you want to update on the backend
     * @return the newly cached instance for this version
     */
    public CompletableFuture<OreVersion> update(OreVersion version) {
        if (!version.getProjectRef().equals(project))
            throw new IllegalArgumentException("The supplied version does not relate to the referenced project");
        return enqueue(NetTasks.updateVersion(cm(), version));
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
