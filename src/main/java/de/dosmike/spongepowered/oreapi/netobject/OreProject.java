package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;
import de.dosmike.spongepowered.oreapi.utility.TypeMappers;

import java.io.Serializable;

public class OreProject extends OreProjectReference implements Serializable {

    @FromJson(value = "created_at", mapper = TypeMappers.StringTimestampMapper.class)
    long createdAt;
    @FromJson("name")
    String name;
    @FromJson("promoted_versions")
    OrePromotedVersion[] promotedVersions;
    @FromJson("stats")
    OreProjectStatsAll stats;
    @FromJson("category")
    OreCategory category;
    @FromJson(value = "summary", optional = true)
    String description;
    @FromJson(value = "last_updated", mapper = TypeMappers.StringTimestampMapper.class)
    long lastUpdate;
    @FromJson("visibility")
    OreVisibility visibility;
    @FromJson("settings")
    OreProjectSettings settings;
    @FromJson("icon_url")
    String urlIcon;

    public OreProject(JsonObject object) {
        JsonUtil.fillSelf(this, object);
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getName() {
        return name;
    }

    public OrePromotedVersion[] getPromotedVersions() {
        return promotedVersions;
    }

    public OreProjectStatsAll getStats() {
        return stats;
    }

    public OreCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public OreVisibility getVisibility() {
        return visibility;
    }

    public OreProjectSettings getSettings() {
        return settings;
    }

    public String getUrlIcon() {
        return urlIcon;
    }

}
