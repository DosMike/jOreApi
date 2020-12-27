package de.dosmike.spongepowered.oreapi.routes;

import de.dosmike.spongepowered.oreapi.OreApiV2;
import de.dosmike.spongepowered.oreapi.netobject.OreOrganization;

import java.util.concurrent.CompletableFuture;

public class Organizations extends AbstractRoute {

    public Organizations(OreApiV2 api) {
        super(api);
    }

    /**
     * Returns the organization. Although currently not well documented, I assume
     * that the organization user is the user representation for this organization
     * allowing for permission and ownership checks.
     *
     * @param organization the organization to fetch
     * @return the organization, if present
     */
    public CompletableFuture<OreOrganization> get(String organization) {
        return enqueue(NetTasks.getOrganization(cm(), organization));
    }

    /**
     * Returns the route object to read and manipulate organization members
     *
     * @param organization the project to manage members on
     * @return the Member route
     */
    public Members members(String organization) {
        return new Members.Organization(api, organization);
    }

    /**
     * Create an instance for namespaced permission checking
     *
     * @param organization the organization to prepare the route for
     * @return the permission route
     */
    public Permissions permissions(String organization) {
        return Permissions.organisation(api, organization);
    }

}
