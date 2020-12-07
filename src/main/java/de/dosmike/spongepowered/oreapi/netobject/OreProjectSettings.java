package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;


import java.io.Serializable;

public class OreProjectSettings implements Serializable {

    @FromJson("keywords")
    String[] keywords;
    @FromJson(value = "homepage", optional = true)
    String homepageUrl;
    @FromJson(value = "issues", optional = true)
    String issuesUrl;
    @FromJson(value = "sources", optional = true)
    String sourcesUrl;
    @FromJson(value = "support", optional = true)
    String supportUrl;
    @FromJson(value = "license.name", optional = true)
    String licenseName;
    @FromJson(value = "license.url", optional = true)
    String licenseUrl;
    @FromJson("forum_sync")
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
