package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;

import java.io.Serializable;

/**
 * Expresses membership of a user with a project or organization
 */
public class OreMembership implements Serializable {

    @FromJson("scope")
    String scope;
    @FromJson(value = "organization.name", optional = true)
    String organization;
    @FromJson(value = "project", optional = true)
    OreProjectReference project;
    @FromJson("role")
    OreRole role;
    @FromJson("is_accepted")
    boolean isAccepted;

    public OreMembership(JsonObject json) {
        JsonUtil.fillSelf(this, json);
    }

    //region getter
    public String getScope() {
        return scope;
    }

    public String getOrganization() {
        return organization;
    }

    public OreProjectReference getProject() {
        return project;
    }

    public OreRole getRole() {
        return role;
    }

    public boolean isAccepted() {
        return isAccepted;
    }
    //endregion

}
