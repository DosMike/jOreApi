package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;
import de.dosmike.spongepowered.oreapi.utility.TypeMappers;

import java.io.Serializable;

public class OreUser implements Serializable {

    @FromJson(value = "created_at", mapper = TypeMappers.StringTimestampMapper.class)
    long createdAt;
    @FromJson("name")
    String name;
    @FromJson("tagline")
    String tagLine;
    @FromJson(value = "join_date", mapper = TypeMappers.StringTimestampMapper.class)
    long joinDate;
    @FromJson("project_count")
    int projects;
    @FromJson("roles")
    OreRoleInfo[] roles;

    public OreUser(JsonObject object) {
        JsonUtil.fillSelf(this, object);
    }

    //region getter
    public long getCreatedAt() {
        return createdAt;
    }

    public String getName() {
        return name;
    }

    public String getTagLine() {
        return tagLine;
    }

    public long getJoinDate() {
        return joinDate;
    }

    public int getProjects() {
        return projects;
    }

    public OreRoleInfo[] getRoles() {
        return roles;
    }
    //endregion

}
