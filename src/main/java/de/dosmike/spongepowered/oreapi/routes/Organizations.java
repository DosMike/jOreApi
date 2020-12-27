package de.dosmike.spongepowered.oreapi.routes;

import de.dosmike.spongepowered.oreapi.OreApiV2;

public class Organizations extends AbstractRoute {

    public Organizations(OreApiV2 api) {
        super(api);
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

}
