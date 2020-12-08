package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;
import de.dosmike.spongepowered.oreapi.utility.TypeMappers;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Optional;

public class OreVersion implements Serializable {

    /**
     * This allows requests for version without having to specify the project again.
     * Also prevents accidentally specifying in the wrong project.
     */
    OreProjectReference project;
    @FromJson(value = "created_at", mapper = TypeMappers.StringTimestampMapper.class)
    long createdAt;
    @FromJson("name")
    String name;
    @FromJson("dependencies")
    OreVersionDependency[] dependencies;
    @FromJson("visibility")
    OreVisibility visibility;
    @FromJson("stats.downloads")
    int downloads;
    @FromJson("file_info")
    OreFileInfo fileInfo;
    @FromJson(value = "author", optional = true)
    String author;
    @FromJson("review_state")
    OreReviewState reviewState;
    @FromJson("tags")
    OreVersionTags tags;
    @FromJson(value = "external.discourse.post_id", optional = true)
    int discoursePostId;

    String changelog = null;

    public OreVersion(OreProjectReference projectBackRef, JsonObject object) {
        project = projectBackRef.toReference();
        JsonUtil.fillSelf(this, object);
    }

    public OreProjectReference getProjectRef() {
        return project;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getName() {
        return name;
    }

    public String getURLSafeName() {
        try {
            return URLEncoder.encode(name, "UTF-8");
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public OreVersionDependency[] getDependencies() {
        return dependencies;
    }

    public OreVisibility getVisibility() {
        return visibility;
    }

    public int getDownloads() {
        return downloads;
    }

    public OreFileInfo getFileInfo() {
        return fileInfo;
    }

    public String getAuthor() {
        return author;
    }

    public OreReviewState getReviewState() {
        return reviewState;
    }

    public OreVersionTags getTags() {
        return tags;
    }

    public int getDiscoursePostId() {
        return discoursePostId;
    }

    public void updateChangelog(String changelog) {
        this.changelog = changelog != null ? changelog : "";
    }
    public Optional<String> getChangelog() {
        return Optional.ofNullable(changelog);
    }
}
