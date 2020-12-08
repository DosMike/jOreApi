package de.dosmike.spongepowered.oreapi;

import de.dosmike.spongepowered.oreapi.netobject.*;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * This class is the "party in the front" - enjoy a class with clean API<br>
 * <ul><li>{@link OreApiV2} Presents a nice interface with only the necessary methods. It utilizes the cache and, if
 * necessary calls into {@link NetTasks} for live data.</li>
 * <li>{@link NetTasks} Is the actual API implementation. Providing suppliers that can be scheduled in the Limiter held
 * by the {@link ConnectionManager}</li>
 * <li>{@link ConnectionManager} holds all the local API data including cache and session. It contains all sorts of
 * utility from building connection objects to destroying sessions</li></ul>
 */
public class OreApiV2 implements AutoCloseable {

    private ConnectionManager instance;

    OreApiV2(ConnectionManager connectionManager) {
        instance = connectionManager;
    }
    //region NON_API - Utility
    private ObjectCache cache() { return instance.cache; }
    private <T> CompletableFuture<T> enqueue(Supplier<T> task) {
        return ConnectionManager.limiter.enqueue(task);
    }
    private static String urlencoded(String s) { try { return URLEncoder.encode(s, "UTF-8"); } catch (Throwable e) { throw new RuntimeException(e); } }

    /** @return true if the termination of this session was confirmed by api. will be false if already destroyed */
    public boolean destroySession() {
        if (instance == null) throw new IllegalStateException("This API instance was closed");
        return instance.destroySession();
    }

    @Override
    public void close() {
        if (instance == null) throw new IllegalStateException("This API instance was closed");
        destroySession();
        instance.notifyClosed(this);
        instance = null;
    }
    //endregion

    //region Permission
    public CompletableFuture<OrePermissionGrant> getPermissions() {
        return enqueue(NetTasks.getPermissions(instance, ""));
    }
    public CompletableFuture<OrePermissionGrant> getPermissions(OreNamespace namespace) {
        return enqueue(NetTasks.getPermissions(instance, "projectOwner="+ urlencoded(namespace.getOwner())+"&projectSlug="+ urlencoded(namespace.getSlug())));
    }
    public CompletableFuture<OrePermissionGrant> getPermissions(String organization) {
        return enqueue(NetTasks.getPermissions(instance, "organizationName="+ urlencoded(organization)));
    }

    public CompletableFuture<Boolean> hasAllPermissions(Collection<OrePermission> perms) {
        if (perms.isEmpty()) return CompletableFuture.completedFuture(true);
        return enqueue(NetTasks.checkPermissions(instance, "", perms, false));
    }
    public CompletableFuture<Boolean> hasAllPermissions(OreNamespace namespace, Collection<OrePermission> perms) {
        if (perms.isEmpty()) return CompletableFuture.completedFuture(true);
        return enqueue(NetTasks.checkPermissions(instance, "projectOwner="+ urlencoded(namespace.getOwner())+"&projectSlug="+ urlencoded(namespace.getSlug()), perms, false));
    }
    public CompletableFuture<Boolean> hasAllPermissions(String organization, Collection<OrePermission> perms) {
        if (perms.isEmpty()) return CompletableFuture.completedFuture(true);
        return enqueue(NetTasks.checkPermissions(instance, "organizationName="+ urlencoded(organization), perms, false));
    }

    public CompletableFuture<Boolean> hasAnyPermissions(Collection<OrePermission> perms) {
        if (perms.isEmpty()) return CompletableFuture.completedFuture(true);
        return enqueue(NetTasks.checkPermissions(instance, "", perms, true));
    }
    public CompletableFuture<Boolean> hasAnyPermissions(OreNamespace namespace, Collection<OrePermission> perms) {
        if (perms.isEmpty()) return CompletableFuture.completedFuture(true);
        return enqueue(NetTasks.checkPermissions(instance, "projectOwner="+ urlencoded(namespace.getOwner())+"&projectSlug="+ urlencoded(namespace.getSlug()), perms, true));
    }
    public CompletableFuture<Boolean> hasAnyPermissions(String organization, Collection<OrePermission> perms) {
        if (perms.isEmpty()) return CompletableFuture.completedFuture(true);
        return enqueue(NetTasks.checkPermissions(instance, "organizationName="+ urlencoded(organization), perms, true));
    }
    //endregion
    //region Project
    /**
     * @param filter a string as returned by {@link OrePagination#getQueryPage}
     * @return empty if connection failed
     */
    public CompletableFuture<OreProjectList> projectSearch(OreProjectFilter filter) {
        //don't want to cache a search
        return enqueue(NetTasks.projectSearch(instance, filter));
    }

    /**
     * @return empty if the connection failed or no such plugin exists
     */
    public CompletableFuture<OreProject> getProject(OreNamespace namespace) {
        return cache().project(namespace)
                .map(CompletableFuture::completedFuture)
                .orElseGet(()->enqueue(NetTasks.getProject(instance, namespace)));
    }

    /**
     * The API has no direct way of searching projects by pluginId
     * but the seach also returns matches for pluginId.
     * This means we have to sift through all search results and remove all
     * results that might accidentally match on name or keywords.
     * @return the first matching OreProject if through all result pages, a project with matching project id was found.
     */
    public CompletableFuture<OreProject> findProjectByPluginId(String pluginId) {
        return cache().project(pluginId)
                .map(CompletableFuture::completedFuture)
                .orElseGet(()->enqueue(NetTasks.findByPluginId(instance, pluginId)));
    }
    //endregion
    //region Version
    /**
     * @return empty if the connection failed or no such plugin exists
     */
    public CompletableFuture<OreVersionList> listVersions(OreProjectReference project, @Nullable OrePaginationFilter pagination) {
        return enqueue(NetTasks.listVersions(instance, project, pagination));
    }

    /**
     * @return empty if the connection failed or no such plugin or version exists
     */
    public CompletableFuture<OreVersion> getVersion(OreProjectReference project, String versionName) {
        return cache().version(project.getPluginId(), versionName)
                .map(CompletableFuture::completedFuture)
                .orElseGet(()->enqueue(NetTasks.getVersion(instance, project, versionName)));
    }

    /**
     * Injects the version changelog into OreVersion through a separate request.
     * To see if the changelog was already fetched, check OreVersion#getChangelog for a non-null value.
     * If the plugin referenced by the version is no longer cached it will be updated and cached again.
     * @return empty on error, or if no changelog is available
     */
    public CompletableFuture<String> getVersionChangelog(OreVersion version) {
        return getVersion(version.getProjectRef(), version.getName())
                .thenCompose(v->v.getChangelog()
                        .map(CompletableFuture::completedFuture)
                        .orElseGet(()->enqueue(NetTasks.getVerionChangelog(instance, v))));
    }
    //endregion
    //region NON_API - Version Download
    /** If the version is not marked as Reviewed with {@link OreVersion#getReviewState()}
     * you should prompt the user with a disclaimer, that installing such plugins might be
     * unsafe and that neither you or the SpongePowered Team is responsible for any damages
     * resulting from the user continuing.
     * THIS IS NOT PART OF THE API (but i include it anyway, because that might be a common goal)
     */
    public CompletableFuture<URL> getDownloadURL(OreVersion version) {
        return enqueue(NetTasks.getDownloadURL(instance, version));
    }
    //endregion

    public static ConnectionManager.Builder builder() {
        return ConnectionManager.builder();
    }

}
