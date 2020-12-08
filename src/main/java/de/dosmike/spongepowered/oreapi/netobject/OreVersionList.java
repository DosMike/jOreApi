package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;

public class OreVersionList extends OreResultList<OreVersion, OrePaginationFilter> {

    /**
     * This allows requests for version without having to specify the project again.
     * Also prevents accidentally specifying in the wrong project.
     */
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
