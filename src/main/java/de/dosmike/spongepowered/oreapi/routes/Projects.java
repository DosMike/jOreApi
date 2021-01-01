package de.dosmike.spongepowered.oreapi.routes;

import de.dosmike.spongepowered.oreapi.OreApiV2;
import de.dosmike.spongepowered.oreapi.netobject.*;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Route for Projects
 */
public class Projects extends AbstractRoute {

    public Projects(OreApiV2 api) {
        super(api);
    }

    /**
     * @param filter a string as returned by {@link OrePagination#getQueryPage}
     * @return empty if connection failed
     */
    public CompletableFuture<OreProjectList> search(OreProjectFilter filter) {
        //don't want to cache a search
        return enqueue(NetTasks.projectSearch(cm(), filter));
    }

    /**
     * The API has no direct way of searching projects by pluginId
     * but the seach also returns matches for pluginId.
     * This means we have to sift through all search results and remove all
     * results that might accidentally match on name or keywords.
     *
     * @param pluginId the plugin id to search
     * @return the first matching OreProject if through all result pages, a project with matching project id was found.
     */
    public CompletableFuture<OreProject> findById(String pluginId) {
        return cache().project(pluginId)
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> enqueue(NetTasks.findByPluginId(cm(), pluginId)));
    }

    /**
     * Get a project from the remote. This will use the cache owned by your
     * api instance to speed things up. The cache by default is 5 minutes.<br>
     * If you don't want to use the cache and to get a fresh instance of this object,
     * try {@link #fetch(OreProjectReference)} instead.
     *
     * @param namespace namespace
     * @return empty if the connection failed or no such plugin exists
     */
    public CompletableFuture<OreProject> get(OreNamespace namespace) {
        return cache().project(namespace)
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> enqueue(NetTasks.getProject(cm(), namespace)));
    }

    /**
     * Intended to renew your OreProject instance, as it's not advised to keep
     * instances around for long. While your API instance has sole ownership over
     * your instance cache, they might have changed on the remote.
     * If you don't care and rather access the cached object, use {@link #get(OreNamespace)}
     * instead.<br>
     * Best usage is probably using something along the lines of
     * <code>OreProject#with(OreApiV2, Projects::fetch)</code>
     *
     * @param projectRef project reference
     * @return empty if the connection failed or no such plugin exists
     */
    public CompletableFuture<OreProject> fetch(OreProjectReference projectRef) {
        return enqueue(NetTasks.getProject(cm(), projectRef.getNamespace()));
    }

    /**
     * @param template request data
     * @return the task requesting the project to be created
     */
    public CompletableFuture<OreProject> create(OreProjectTemplate template) {
        return enqueue(NetTasks.createProject(cm(), template));
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
    public CompletableFuture<OreProject> update(OreProject project) {
        return enqueue(NetTasks.updateProject(cm(), project));
    }

    /**
     * Deletes this project from ore and removes it from cache.<br>
     * <b>This is permanent and can not be undone!</b>
     *
     * @param project project
     * @return nothing
     */
    public CompletableFuture<Void> delete(OreProjectReference project) {
        return enqueue(NetTasks.deleteProject(cm(), project));
    }

    /**
     * Returns the route object to read and manipulate project members
     *
     * @param project the project to manage members on
     * @return the Member route
     */
    public Members members(OreProjectReference project) {
        return new Members.Project(api, project);
    }

    /**
     * Gets the day stats (views and downloads) for the project in the specified date range.
     *
     * @param project the project to query
     * @param from    the first day to query (inclusive)
     * @param to      the last day to query (inclusive)
     * @return stats
     */
    public CompletableFuture<Map<Date, OreProjectStatsDay>> stats(OreProjectReference project, Date from, Date to) {
        return enqueue(NetTasks.getProjectStats(cm(), project, from, to));
    }

    /**
     * The required permissions vary depending on the wanted visibility. Having reviewer permission
     * guarantees access to all visibilities no matter the circumstances. IN all other cases these rules apply.
     * <ul>
     * <li>{@link OreVisibility#NeedsApproval} requires {@link OrePermission#Edit_Subject_Settings} and
     * that the current visibility is {@link OreVisibility#NeedsChanges}</li>
     * <li>{@link OreVisibility#SoftDelete} requires {@link OrePermission#Delete_Project}</li>
     * </ul>
     *
     * @param project the project to update
     * @param visibility this will be the new visibility for this project
     * @param comment    The api allows you to specify a reason for why you changed the visibility
     * @param <T> the project reference class
     * @return the project for chaining/updating
     */
    public <T extends OreProjectReference> CompletableFuture<T> visibility(T project, OreVisibility visibility, String comment) {
        return enqueue(NetTasks.updateProjectVisibility(cm(), project, visibility, comment));
    }

    /**
     * Returns the route object to read and manipulate project versions
     *
     * @return the Versions route
     */
    public Versions versions() {
        return new Versions(api);
    }

    /**
     * Create an instance for namespaced permission checking
     *
     * @param namespace the project to prepare the route for
     * @return the permission route
     */
    public Permissions permissions(OreNamespace namespace) {
        return Permissions.namespace(api, namespace);
    }

    /**
     * Create an instance for namespaced permission checking
     *
     * @param project the project to prepare the route for
     * @return the permission route
     */
    public Permissions permissions(OreProjectReference project) {
        return Permissions.namespace(api, project.getNamespace());
    }

}
