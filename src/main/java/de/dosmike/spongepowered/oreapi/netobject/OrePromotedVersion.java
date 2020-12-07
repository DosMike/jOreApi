package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;


import java.io.Serializable;

/**
 * IMPORTANT: This is only a partial version that
 * contains the most crucial display information for recommended versions
 * to be displayed on the project page. For full version objects please
 * use the version endpoints.
 * This combines PromotedVersion
 */
public class OrePromotedVersion implements Serializable {

    @FromJson("version")
    String version;
    @FromJson("platforms")
    OrePlatform[] platforms;

    public OrePromotedVersion(JsonObject object) {
        JsonUtil.fillSelf(this,object);
    }

    public String getVersion() {
        return version;
    }

    public OrePlatform[] getPlatforms() {
        return platforms;
    }
}
