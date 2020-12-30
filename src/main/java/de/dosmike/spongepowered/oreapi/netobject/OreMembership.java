package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Map;

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

    /**
     * Can be used to check if this membership wraps a project ref or an organisation ref
     * aka whether getOrganisation or getProject returns a value.<br>
     * For project references this will return "<tt>project</tt>".
     *
     * @return the scope of this membership
     */
    public String getScope() {
        return scope;
    }

    /**
     * @return the organization name, if this instance represents a organization membership
     */
    @Nullable
    public String getOrganization() {
        return organization;
    }

    /**
     * @return the project reference, if this instance represents a project membership
     */
    @Nullable
    public OreProjectReference getProject() {
        return project;
    }

    /**
     * @return the role a user has within the represented organization or project
     */
    public OreRole getRole() {
        return role;
    }

    /**
     * @return true if the user has accepted their role withing the represented organization or project
     * @see de.dosmike.spongepowered.oreapi.routes.Members#set(Map)
     */
    public boolean isAccepted() {
        return isAccepted;
    }
    //endregion

}
