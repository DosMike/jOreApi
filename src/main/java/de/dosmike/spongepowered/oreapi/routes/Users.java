package de.dosmike.spongepowered.oreapi.routes;

import de.dosmike.spongepowered.oreapi.OreApiV2;
import de.dosmike.spongepowered.oreapi.netobject.*;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Query user information
 */
public class Users extends AbstractRoute {

    public Users(OreApiV2 api) {
        super(api);
    }

    /**
     * Search one or more users or organisations with the specific filter
     *
     * @param filter filter
     * @return user list
     */
    public CompletableFuture<OreUserList> search(OreUserFilter filter) {
        return enqueue(NetTasks.userSearch(cm(), filter));
    }

    /**
     * Get the user object for the user represented by the current session
     * @return user
     */
    public CompletableFuture<OreUser> self() {
        return cache().user("@me")
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> enqueue(NetTasks.getUser(cm(), null, true)));
    }

    /**
     * Retrieve public information about the specified user.
     * Please note that username seem to only support word characters
     * (a-zA-Z0-9_-). Any other name will not be queried and return a NoResultException
     *
     * @param name the username to fetch
     * @return the OreUser, if such exists
     */
    public CompletableFuture<OreUser> get(String name) {
        return cache().user(name)
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> enqueue(NetTasks.getUser(cm(), name, false)));
    }

    /**
     * Fetch the objects that a user has a membership relation with.
     * These are be projects and organizations, both wrapped in {@link OreMembership}s.
     *
     * @param user the user to query memberships for
     * @return the list of membership objects
     */
    public CompletableFuture<OreMembershipList> memberships(String user) {
        return enqueue(NetTasks.getUserMemberships(cm(), user));
    }

    /**
     * Fetch a compact list of all projects a user has starred.
     *
     * @param user   the user to query
     * @param filter the filter to sort and paginate or null
     * @return the compact list of starred projects
     * @see OreCompactProject
     */
    public CompletableFuture<OreCompactProjectList> starred(String user, @Nullable OreCompactProjectFilter filter) {
        return enqueue(NetTasks.getUserStarred(cm(), user, filter));
    }

    /**
     * Fetch a compact list of all projects a user is watching.
     *
     * @param user   the user to query
     * @param filter the filter to sort and paginate or null
     * @return the compact list of starred projects
     * @see OreCompactProject
     */
    public CompletableFuture<OreCompactProjectList> watching(String user, @Nullable OreCompactProjectFilter filter) {
        return enqueue(NetTasks.getUserWatching(cm(), user, filter));
    }

}
