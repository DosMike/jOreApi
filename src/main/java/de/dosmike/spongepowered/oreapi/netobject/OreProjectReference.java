package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;


import java.io.Serializable;
import java.util.Locale;

/**
 * internal minimal project reference
 */
public class OreProjectReference implements Serializable {

    @FromJson("plugin_id")
    String pluginId;
    @FromJson("namespace")
    OreNamespace namespace;

    protected OreProjectReference(){}
    protected OreProjectReference(JsonObject object){
        JsonUtil.fillSelf(this, object);
    }
    private OreProjectReference(OreProjectReference project) {
        this.pluginId = project.pluginId;
        this.namespace = new OreNamespace(project.namespace.owner, project.namespace.slug);
    }

    public static OreProjectReference fromProject(OreProject project) {
        return new OreProjectReference(project);
    }
    public OreProjectReference toReference() {
        return new OreProjectReference(this);
    }

    public String getPluginId() {
        return pluginId.toLowerCase(Locale.ROOT);
    }

    public OreNamespace getNamespace() {
        return namespace;
    }
}
