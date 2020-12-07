package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;

public class OreProjectList extends OreResultList<OreProject, OreProjectFilter> {
    @Override
    protected OreProject constructInstanceFromJson(Class<OreProject> clazz, JsonObject json) {
        return new OreProject(json);
    }

    public OreProjectList(JsonObject object, Class<OreProject> resultClass, OreProjectFilter previousFilter) {
        super(object, resultClass, previousFilter);
    }
}
