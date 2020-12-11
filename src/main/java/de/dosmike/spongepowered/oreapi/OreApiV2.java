package de.dosmike.spongepowered.oreapi;

import de.dosmike.spongepowered.oreapi.netobject.*;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
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
	private ObjectCache cache() {
		return instance.cache;
	}

	private <T> CompletableFuture<T> enqueue(Supplier<T> task) {
		return ConnectionManager.limiter.enqueue(task);
	}

	private static String urlencoded(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private ConnectionManager getConnectionManager() {
		return instance;
	}

	/**
	 * @return true if the termination of this session was confirmed by api. will be false if already destroyed
	 */
	public boolean destroySession() {
		if (instance == null) throw new IllegalStateException("This API instance was closed");
		return instance.destroySession();
	}

	@Override
	public void close() {
		if (instance == null) throw new IllegalStateException("This API instance was closed");
		destroySession();
		ConnectionManager.notifyClosed(this);
		instance = null;
	}
	//endregion

	//region Permission
	public CompletableFuture<OrePermissionGrant> getPermissions() {
		return enqueue(NetTasks.getPermissions(instance, ""));
	}

	public CompletableFuture<OrePermissionGrant> getPermissions(OreNamespace namespace) {
		return enqueue(NetTasks.getPermissions(instance, "projectOwner=" + urlencoded(namespace.getOwner()) + "&projectSlug=" + urlencoded(namespace.getSlug())));
	}

	public CompletableFuture<OrePermissionGrant> getPermissions(String organization) {
		return enqueue(NetTasks.getPermissions(instance, "organizationName=" + urlencoded(organization)));
	}

	public CompletableFuture<Boolean> hasAllPermissions(Collection<OrePermission> perms) {
		if (perms.isEmpty()) return CompletableFuture.completedFuture(true);
		return enqueue(NetTasks.checkPermissions(instance, "", perms, false));
	}

	public CompletableFuture<Boolean> hasAllPermissions(OreNamespace namespace, Collection<OrePermission> perms) {
		if (perms.isEmpty()) return CompletableFuture.completedFuture(true);
		return enqueue(NetTasks.checkPermissions(instance, "projectOwner=" + urlencoded(namespace.getOwner()) + "&projectSlug=" + urlencoded(namespace.getSlug()), perms, false));
	}

	public CompletableFuture<Boolean> hasAllPermissions(String organization, Collection<OrePermission> perms) {
		if (perms.isEmpty()) return CompletableFuture.completedFuture(true);
		return enqueue(NetTasks.checkPermissions(instance, "organizationName=" + urlencoded(organization), perms, false));
	}

	public CompletableFuture<Boolean> hasAnyPermissions(Collection<OrePermission> perms) {
		if (perms.isEmpty()) return CompletableFuture.completedFuture(true);
		return enqueue(NetTasks.checkPermissions(instance, "", perms, true));
	}

	public CompletableFuture<Boolean> hasAnyPermissions(OreNamespace namespace, Collection<OrePermission> perms) {
		if (perms.isEmpty()) return CompletableFuture.completedFuture(true);
		return enqueue(NetTasks.checkPermissions(instance, "projectOwner=" + urlencoded(namespace.getOwner()) + "&projectSlug=" + urlencoded(namespace.getSlug()), perms, true));
	}

	public CompletableFuture<Boolean> hasAnyPermissions(String organization, Collection<OrePermission> perms) {
		if (perms.isEmpty()) return CompletableFuture.completedFuture(true);
		return enqueue(NetTasks.checkPermissions(instance, "organizationName=" + urlencoded(organization), perms, true));
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
	 * The API has no direct way of searching projects by pluginId
	 * but the seach also returns matches for pluginId.
	 * This means we have to sift through all search results and remove all
	 * results that might accidentally match on name or keywords.
	 *
	 * @return the first matching OreProject if through all result pages, a project with matching project id was found.
	 */
	public CompletableFuture<OreProject> findProjectByPluginId(String pluginId) {
		return cache().project(pluginId)
				.map(CompletableFuture::completedFuture)
				.orElseGet(() -> enqueue(NetTasks.findByPluginId(instance, pluginId)));
	}

	/**
	 * @return empty if the connection failed or no such plugin exists
	 */
	public CompletableFuture<OreProject> getProject(OreNamespace namespace) {
		return cache().project(namespace)
				.map(CompletableFuture::completedFuture)
				.orElseGet(() -> enqueue(NetTasks.getProject(instance, namespace)));
	}

	/**
	 * @param template request data
	 * @return the task requesting the project to be created
	 */
	public CompletableFuture<OreProject> createProject(OreProjectTemplate template) {
		return enqueue(NetTasks.createProject(instance, template));
	}

	/**
	 * If you want to save changes in the repository, you'll have to update it through this method.
	 * If you changed parts of the namespace (owner, project name) all existing instances will be
	 * invalid and requests involving those will most likely fail. Use the result of this method
	 * instead.
	 *
	 * @param project the project you want to update on ore
	 * @return a completable future that will return the updated instance from remote.
	 */
	public CompletableFuture<OreProject> updateProject(OreProject project) {
		return enqueue(NetTasks.updateProject(instance, project));
	}

	/**
	 * Deletes this project from ore and removes it from cache.<br>
	 * <b>This is permanent and can not be undone!</b>
	 */
	public CompletableFuture<Void> deleteProject(OreProjectReference project) {
		return enqueue(NetTasks.deleteProject(instance, project));
	}

	/**
	 * Retrieve the current list of members for this project with additional role information.
	 * You will only see accepted roles unless you have {@link OrePermission#Manage_Subject_Members}
	 * To update member roles call {@link OreMemberList#forPosting()} to get the mapping.
	 * @param project the project to fetch members for
	 */
	public CompletableFuture<OreMemberList> getProjectMembers(OreProjectReference project) {
		return enqueue(NetTasks.getMembers(instance, project));
	}

	/**
	 * Updates the member list for this project. Keep in mind that you have to send the whole list.
	 * If you need to create the list, use {@link OreMemberList#forPosting()}
	 * Keep in mind that a changed role is more like an invite and has to be accepted by the other party.
	 *
	 * @param project the project to edit
	 * @param roles   a username -&gt; role mapping
	 */
	public CompletableFuture<Void> setProjectMembers(OreProjectReference project, Map<String, OreRole> roles) {
		return enqueue(NetTasks.setMembers(instance, project, roles));
	}

	/**
	 * Gets the day stats (views and downloads) for the project in the specified date range.
	 *
	 * @param project the project to query
	 * @param from    the first day to query (inclusive)
	 * @param to      the last day to query (inclusive)
	 */
	public CompletableFuture<Map<Date, OreProjectStatsDay>> getProjectStats(OreProjectReference project, Date from, Date to) {
		return enqueue(NetTasks.getProjectStats(instance, project, from, to));
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
				.orElseGet(() -> enqueue(NetTasks.getVersion(instance, project, versionName)));
	}

	/**
	 * Injects the version changelog into OreVersion through a separate request.
	 * To see if the changelog was already fetched, check OreVersion#getChangelog for a non-null value.
	 * If the plugin referenced by the version is no longer cached it will be updated and cached again.
	 *
	 * @return empty on error, or if no changelog is available
	 */
	public CompletableFuture<String> getVersionChangelog(OreVersion version) {
		return getVersion(version.getProjectRef(), version.getName())
				.thenCompose(v -> v.getChangelog()
						.map(CompletableFuture::completedFuture)
						.orElseGet(() -> enqueue(NetTasks.getVerionChangelog(instance, v))));
	}
	//endregion
	//region NON_API - Version Download

	/**
	 * If the version is not marked as Reviewed with {@link OreVersion#getReviewState()}
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
