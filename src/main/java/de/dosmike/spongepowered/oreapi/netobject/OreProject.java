package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.OreApiV2;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;
import de.dosmike.spongepowered.oreapi.utility.TypeMappers;

import java.io.Serializable;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

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

    //Region builder
    public static class Builder {
        JsonObject request = new JsonObject();

        private Builder() {
        }

        public Builder setName(String name) {
            if (name == null) throw new NullPointerException("name can't be null");
            request.addProperty("name", name);
            return Builder.this;
        }

        public Builder setPluginId(String pluginId) {
            if (pluginId == null) throw new NullPointerException("pluginId can't be null");
            request.addProperty("plugin_id", pluginId);
            return Builder.this;
        }

        public Builder setCategory(OreCategory category) {
            if (category == null) throw new NullPointerException("category can't be null");
            request.addProperty("category", category.name().toLowerCase(Locale.ROOT));
            return Builder.this;
        }

        /**
         * this is most likely the summary
         */
        public Builder setDescription(String description) {
            if (description == null) throw new NullPointerException("description can't be null");
            request.addProperty("description", description);
            return Builder.this;
        }

        public Builder setOwner(String ownerName) {
            if (ownerName == null) throw new NullPointerException("ownerName can't be null");
            request.addProperty("owner_name", ownerName);
            return Builder.this;
        }

        public CompletableFuture<OreProject> build(OreApiV2 api) {
            //noinspection deprecation
            return api.createProject(request);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
    //endregion

}
