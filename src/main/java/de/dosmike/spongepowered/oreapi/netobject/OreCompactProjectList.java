package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;

public class OreCompactProjectList extends OreResultList<OreCompactProject, OreCompactProjectFilter> {
	@Override
	protected OreCompactProject constructInstanceFromJson(Class<OreCompactProject> clazz, JsonObject json) {
		return new OreCompactProject(json);
	}

	public OreCompactProjectList(JsonObject object, Class<OreCompactProject> resultClass, OreCompactProjectFilter previousFilter) {
		super(object, resultClass, previousFilter);
	}
}
