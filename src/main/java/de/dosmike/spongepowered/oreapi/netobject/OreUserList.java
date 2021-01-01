package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;

public class OreUserList extends OreResultList<OreUser, OreUserFilter> {
	@Override
	protected OreUser constructInstanceFromJson(Class<OreUser> clazz, JsonObject json) {
		return new OreUser(json);
	}

	public OreUserList(JsonObject object, Class<OreUser> resultClass, OreUserFilter previousFilter) {
		super(object, resultClass, previousFilter);
	}
}
