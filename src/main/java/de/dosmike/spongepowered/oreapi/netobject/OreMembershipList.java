package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonArray;

/**
 * A list containing information about what objects a user if member of
 */
public class OreMembershipList extends HeadlessPaginationList<OreMembership> {

	public OreMembershipList(JsonArray array) {
		super(array, OreMembership.class);
	}

}
