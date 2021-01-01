package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;
import de.dosmike.spongepowered.oreapi.utility.TypeMappers;

import java.io.Serializable;
import java.util.Date;

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

    /**
     * @return the instance this user was created at as unix timestamp in milliseconds
     * @see Date#getTime()
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * @return the users name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the users tagline. Like a little summary on their profile
     */
    public String getTagLine() {
        return tagLine;
    }

    /**
     * @return the instance this user joined as unix timestamp in milliseconds
     * @see Date#getTime()
     */
    public long getJoinDate() {
        return joinDate;
    }

    /**
     * @return the number of projects this user owns
     */
    public int getProjects() {
        return projects;
    }

    /**
     * @return a collection of roles this user has globally
     */
    public OreRoleInfo[] getRoles() {
        return roles;
    }
    //endregion

}
