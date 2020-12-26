package de.dosmike.spongepowered.oreapi.routes;

import de.dosmike.spongepowered.oreapi.OreApiV2;
import de.dosmike.spongepowered.oreapi.netobject.OreUser;
import de.dosmike.spongepowered.oreapi.netobject.OreUserFilter;
import de.dosmike.spongepowered.oreapi.netobject.OreUserList;

import java.util.concurrent.CompletableFuture;

public class Users extends AbstractRoute {

    public Users(OreApiV2 api) {
        super(api);
    }

    /**
     * Search one or more users or organisations with the specific filter
     */
    public CompletableFuture<OreUserList> search(OreUserFilter filter) {
        return enqueue(NetTasks.userSearch(cm(), filter));
    }

    /**
     * Get the user object for the user represented by the current session
     */
    public CompletableFuture<OreUser> self() {
        return enqueue(NetTasks.getUser(cm(), null, true));
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
        return enqueue(NetTasks.getUser(cm(), name, false));
    }
}
