package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;

public class OreVersionList extends OreResultList<OreVersion, OrePaginationFilter> {

    private final OreProjectReference versionProjectRef;
    @Override
    protected OreVersion constructInstanceFromJson(Class<OreVersion> clazz, JsonObject json) {
        return new OreVersion(versionProjectRef, json);
    }

    public OreVersionList(JsonObject object, OreProjectReference backRef, Class<OreVersion> resultClass, OrePaginationFilter previousFilter) {
        super(object, previousFilter);
        this.versionProjectRef = backRef.toReference();
        lateParse(object, resultClass);
    }
}
