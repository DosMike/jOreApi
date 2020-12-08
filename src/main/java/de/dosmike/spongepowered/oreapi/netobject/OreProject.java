package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.OreApiV2;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonTags;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;
import de.dosmike.spongepowered.oreapi.utility.TypeMappers;

import java.io.Serializable;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class OreProject extends OreProjectReference implements Serializable {

    /**
     * used for updating. if you change the name/owner you can't update the repository
     * because the change needs to be pushed against the original namespace.<br>
     * This field will be set on construction, and be used when {@link #update} is called.
     * After that, this instance will be invalid.
     * TODO: maybe mark the instance invalid with a flag?
     */
    final OreNamespace shadowNamespace;

    @FromJson(value = "created_at", mapper = TypeMappers.StringTimestampMapper.class)
    long createdAt;
    @FromJson("name")
    @JsonTags("patchProject")
    String name;
    @FromJson("promoted_versions")
    OrePromotedVersion[] promotedVersions;
    @FromJson("stats")
    OreProjectStatsAll stats;
    @FromJson("category")
    @JsonTags("patchProject")
    OreCategory category;
    /**
     * the summary is limited to 120 characters
     */
    @FromJson(value = "summary", optional = true)
    @JsonTags("patchProject")
    String summary;
    @FromJson(value = "last_updated", mapper = TypeMappers.StringTimestampMapper.class)
    long lastUpdate;
    @FromJson("visibility")
    OreVisibility visibility;
    @FromJson("settings")
    @JsonTags("patchProject")
    OreProjectSettings settings;
    @FromJson("icon_url")
    String urlIcon;

    public OreProject(JsonObject object) {
        JsonUtil.fillSelf(this, object);
        shadowNamespace = new OreNamespace(namespace.owner, namespace.slug);
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

    public String getSummary() {
        return summary;
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

    public JsonObject toJson() {
        return JsonUtil.buildJson(this);
    }

    private JsonObject getPatchJson() {
        return JsonUtil.buildJson(this, "patchProject");
    }

    /**
     * convenience function for OreApiV2.updateProject(this).
     * If you want to save changes in the repository, you'll have to update it through this method.
     * If you changed parts of the namespace (owner, project name) all existing instances will be
     * invalid and requests involving those will most likely fail. Use the result of this method
     * instead.
     *
     * @param api the api instance to use for this request
     * @return a completable future that will return the updated instance from remote.
     */
    public CompletableFuture<OreProject> update(OreApiV2 api) {
        return api.updateProject(this);
    }

}
