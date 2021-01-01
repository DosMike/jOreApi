package de.dosmike.spongepowered.oreapi.routes;

import de.dosmike.spongepowered.oreapi.OreApiV2;
import de.dosmike.spongepowered.oreapi.netobject.*;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Version management route
 */
public class Versions extends AbstractRoute {

    public Versions(OreApiV2 api) {
        super(api);
    }

    /**
     * @param project    project
     * @param pagination pagination
     * @return empty if the connection failed or no such plugin exists
     */
    public CompletableFuture<OreVersionList> list(OreProjectReference project, @Nullable OrePaginationFilter pagination) {
        return enqueue(NetTasks.listVersions(cm(), project, pagination));
    }

    /**
     * Get a version from the remote. This will use the cache owned by your
     * api instance to speed things up. The cache by default is 5 minutes.<br>
     * If you don't want to use the cache and to get a fresh instance of this object,
     * try {@link #fetch(OreVersionReference)} instead.
     *
     * @param version the version
     * @return empty if the connection failed or no such plugin or version exists
     */
    public CompletableFuture<OreVersion> get(OreVersionReference version) {
        return cache().version(version.getProjectRef().getPluginId(), version.getName())
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> enqueue(NetTasks.getVersion(cm(), version)));
    }

    /**
     * Intended to renew your OreVersion instance, as it's not advised to keep
     * instances around for long. While your API instance has sole ownership over
     * your instance cache, they might have changed on the remote.
     * If you don't care and rather access the cached object, use {@link #get(OreVersionReference)}
     * instead.<br>
     * Best usage is probably using something along the lines of
     * <code>OreVersion#with(OreApiV2, Versions::fetch)</code>
     *
     * @param version the version
     * @return empty if the connection failed or no such plugin or version exists
     */
    public CompletableFuture<OreVersion> fetch(OreVersionReference version) {
        return cache().version(version.getProjectRef().getPluginId(), version.getName())
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> enqueue(NetTasks.getVersion(cm(), version)));
    }

    /**
     * Injects the version changelog into OreVersion through a separate request.
     * To see if the changelog was already fetched, check OreVersion#getChangelog for a non-null value.
     * If the plugin referenced by the version is no longer cached it will be updated and cached again.
     *
     * @param version the version
     * @return empty on error, or if no changelog is available
     */
    public CompletableFuture<String> changelog(OreVersionReference version) {
        return get(version)
                .thenCompose(v -> Optional.ofNullable(v.getChangelog())
                        .map(CompletableFuture::completedFuture)
                        .orElseGet(() -> enqueue(NetTasks.getVerionChangelog(cm(), v))));
    }

    /**
     * Scan a plugin file for future upload. Use this before uploading a version to see which tags Ore will
     * assign the file. Requires the {@link OrePermission#Create_Version} in the project or owning organization.<br>
     * Please note that the resulting version is not cached for obvious reason.
     *
     * @param project the project to scan this for for context
     * @param file    the file to scan
     * @return a OreVersion for inspection
     */
    public CompletableFuture<OreVersion> scan(OreProjectReference project, Path file) {
        return enqueue(NetTasks.scanVersion(cm(), project, file));
    }

    /**
     * Creates a new version for a project. Requires the {@link OrePermission#Create_Version} in the
     * project or owning organization.
     *
     * @param deployVersionInfo further information regarding this new version
     * @return the created OreVersion
     */
    public CompletableFuture<OreVersion> create(OreDeployVersionInfo deployVersionInfo) {
        return enqueue(NetTasks.createVersion(cm(), deployVersionInfo));
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
        return enqueue(NetTasks.updateVersion(cm(), version));
    }

    /**
     * Removes the version from the remote and from the cache.<br>
     * <b>This is permanent and cannot be undone!</b>
     * It is in fact so permanent, that a new version with the same name may not be created unless unlocked by an admin.
     *
     * @param version the version to delete
     * @return nothing
     */
    public CompletableFuture<Void> delete(OreVersionReference version) {
        return enqueue(NetTasks.deleteVersion(cm(), version));
    }

    /**
     * Gets the day stats (downloads) for the project in the specified date range.
     *
     * @param version the version to query
     * @param from    the first day to query (inclusive)
     * @param to      the last day to query (inclusive)
     * @return stats
     */
    public CompletableFuture<Map<Date, OreVersionStatsDay>> stats(OreVersionReference version, Date from, Date to) {
        return enqueue(NetTasks.getVersionStats(cm(), version, from, to));
    }

    /**
     * The required permissions vary depending on the wanted visibility. Having reviewer permission
     * guarantees access to all visibilities no matter the circumstances. The docs look rather copy-pasty.
     * Anyhow, this requests the visibility of the version being changed on the remote, followed by updating the
     * cached version object if successful and present
     *
     * @param version    the version to update
     * @param visibility this will be the new visibility for this project
     * @param comment    The api allows you to specify a reason for why you changed the visibility
     * @param <T>        the version reference class
     * @return the updated version for chaining/updating
     */
    public <T extends OreVersionReference> CompletableFuture<T> visibility(T version, OreVisibility visibility, String comment) {
        return enqueue(NetTasks.updateVersionVisibility(cm(), version, visibility, comment));
    }

    /**
     * If the version is not marked as Reviewed with {@link OreVersion#getReviewState()}
     * you should prompt the user with a disclaimer, that installing such plugins might be
     * unsafe and that neither you or the SpongePowered Team is responsible for any damages
     * resulting from the user continuing.
     * THIS IS NOT PART OF THE API (but i include it anyway, because that might be a common goal)
     *
     * @param version the version to get the asset download url for
     * @return the download url
     */
    public CompletableFuture<URL> downloadUrl(OreVersion version) {
        return enqueue(NetTasks.getDownloadURL(cm(), version));
    }

}
