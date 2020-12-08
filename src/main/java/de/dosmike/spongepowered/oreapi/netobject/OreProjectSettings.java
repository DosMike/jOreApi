package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonTags;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;

import java.io.Serializable;

public class OreProjectSettings implements Serializable {

    /**
     * this supports a maximum of 5 keywords
     */
    @FromJson("keywords")
    @JsonTags("patchProject")
    String[] keywords;
    @FromJson(value = "homepage", optional = true)
    @JsonTags("patchProject")
    String homepageUrl;
    @FromJson(value = "issues", optional = true)
    @JsonTags("patchProject")
    String issuesUrl;
    @FromJson(value = "sources", optional = true)
    @JsonTags("patchProject")
    String sourcesUrl;
    @FromJson(value = "support", optional = true)
    @JsonTags("patchProject")
    String supportUrl;
    @FromJson(value = "license.name", optional = true)
    @JsonTags("patchProject")
    String licenseName;
    @FromJson(value = "license.url", optional = true)
    @JsonTags("patchProject")
    String licenseUrl;
    @FromJson("forum_sync")
    @JsonTags("patchProject")
    boolean forumSync;

    public OreProjectSettings(JsonObject object) {
        JsonUtil.fillSelf(this, object);
    }

    public String[] getKeywords() {
        return keywords;
    }

    public String getHomepageUrl() {
        return homepageUrl;
    }

    public String getIssuesUrl() {
        return issuesUrl;
    }

    public String getSourcesUrl() {
        return sourcesUrl;
    }

    public String getSupportUrl() {
        return supportUrl;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public boolean isForumSync() {
        return forumSync;
    }
}
