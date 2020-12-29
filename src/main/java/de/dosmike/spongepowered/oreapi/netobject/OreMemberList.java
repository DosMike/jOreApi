package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonArray;

import java.util.HashMap;
import java.util.Map;

public class OreMemberList extends HeadlessPaginationList<OreMember> {

	public OreMemberList(JsonArray array) {
		super(array, OreMember.class);
	}

	/**
	 * Converts this list in to a map of username -> role enum.
	 * This mapping can be changed and subsequently used for updating member roles.
	 *
	 * @return username -> role mapping
	 */
	public Map<String, OreRole> forPosting() {
		Map<String, OreRole> currentMembers = new HashMap<>();
		get().forEach(m -> currentMembers.put(m.getUsername(), m.getRoleInfo().getRole()));
		return currentMembers;
	}

}
