package de.dosmike.spongepowered.oreapi.routes;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.OreApiV2;
import de.dosmike.spongepowered.oreapi.netobject.*;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static de.dosmike.spongepowered.oreapi.utility.ReflectionHelper.friendField;

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
     * @return the first matching OreProject if through all result pages, a project with matching project id was found.
     */
    public CompletableFuture<OreProject> findById(String pluginId) {
        return cache().project(pluginId)
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> enqueue(NetTasks.findByPluginId(cm(), pluginId)));
    }

    /**
     * @return empty if the connection failed or no such plugin exists
     */
    public CompletableFuture<OreProject> get(OreNamespace namespace) {
        return cache().project(namespace)
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> enqueue(NetTasks.getProject(cm(), namespace)));
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
     * Updating the visibility of a project is done with a separate request. Some effort is made
     * to dynamically call only the required endpoints on the remote so you only have to edit
     * the project instance as desired before calling this.
     * Request order is PATCH /project, POST /project//visibility
     *
     * @param project the project you want to update on ore
     * @return a completable future that will return the updated instance from remote.
     */
    public CompletableFuture<OreProject> update(OreProject project) {
        //get hidden values
        final int dirt = friendField(project, "dirty");
        //did a direct member or a settings value change?
        boolean updateBase = ((dirt & 1) != 0) || (int) friendField(project, "settingsHash") != project.getSettings().hashCode();
        boolean updateVisibility = ((dirt & 2) != 0);

        //prepare for project update - because project will be replaced if updateBase, i cache everything else ahead
        String visibilityUpdate;
        if (updateVisibility) {
            JsonObject requestJson = new JsonObject();
            requestJson.addProperty("visibility", project.getVisibility().toString());
            String comment = friendField(project, "visibilityComment");
            requestJson.addProperty("comment", comment != null ? comment : "");
            visibilityUpdate = requestJson.toString();
        } else {
            visibilityUpdate = null;
        }

        CompletableFuture<OreProject> transformed = null;
        if (updateBase) transformed = enqueue(NetTasks.updateProject(cm(), project));
        if (updateVisibility) {
            if (transformed == null)
                transformed = enqueue(NetTasks.updateProjectVisibility(cm(), project, visibilityUpdate));
            else {
                transformed.thenCompose(op -> enqueue(NetTasks.updateProjectVisibility(cm(), op, visibilityUpdate)));
            }
        }
        if (transformed == null) transformed = CompletableFuture.completedFuture(project);//nothing to update

        return transformed;
    }

    /**
     * Deletes this project from ore and removes it from cache.<br>
     * <b>This is permanent and can not be undone!</b>
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
        return new Members(api, project);
    }

    /**
     * Gets the day stats (views and downloads) for the project in the specified date range.
     *
     * @param project the project to query
     * @param from    the first day to query (inclusive)
     * @param to      the last day to query (inclusive)
     */
    public CompletableFuture<Map<Date, OreProjectStatsDay>> stats(OreProjectReference project, Date from, Date to) {
        return enqueue(NetTasks.getProjectStats(cm(), project, from, to));
    }

    /**
     * Returns the route object to read and manipulate project versions
     *
     * @param project the project to manage versions on
     * @return the Versions route
     */
    public Versions versions(OreProjectReference project) {
        return new Versions(api, project);
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
