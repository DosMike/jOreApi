package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;

import java.io.Serializable;

/**
 * IDK man, feels like user should be the user representation of this organization
 * for permission checking and similar, but the API docs currently don't explain this object.
 */
public class OreOrganization implements Serializable {

    @FromJson("owner")
    String owner;
    @FromJson("user")
    OreUser user;

    public OreOrganization(JsonObject json) {
        JsonUtil.fillSelf(this, json);
    }

    //region getter
    public String getOwner() {
        return owner;
    }

    public OreUser getUser() {
        return user;
    }
    //endregion

}
