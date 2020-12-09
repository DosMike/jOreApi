package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonArray;

import java.util.HashMap;
import java.util.Map;

public class OreMemberList extends HeadlessPaginationList<OreMember> {

	public OreMemberList(JsonArray array) {
		super(array, OreMember.class);
	}

	public Map<String, OreRole> forPosting() {
		Map<String, OreRole> currentMembers = new HashMap<>();
		get().forEach(m -> currentMembers.put(m.getUsername(), m.getRoleInfo().getRole()));
		return currentMembers;
	}

}
