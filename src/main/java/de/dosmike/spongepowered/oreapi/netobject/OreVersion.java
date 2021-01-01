package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;
import de.dosmike.spongepowered.oreapi.utility.TypeMappers;

import java.util.Date;

public class OreVersion extends OreVersionReference {

	@FromJson(value = "created_at", mapper = TypeMappers.StringTimestampMapper.class)
	long createdAt;
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

	//region getter

	/**
	 * @return the instance this version was created at as unix timestamp in milliseconds
	 * @see Date#getTime()
	 */
	public long getCreatedAt() {
		return createdAt;
	}

	/**
	 * @return this projects dependencies, required for the project to work
	 */
	public OreVersionDependency[] getDependencies() {
		return dependencies;
	}

	/**
	 * @return the version visibility
	 */
	public OreVisibility getVisibility() {
		return visibility;
	}

	/**
	 * @return the number of downloads for this version
	 */
	public int getDownloads() {
		return downloads;
	}

	/**
	 * @return information about the project asset
	 */
	public OreFileInfo getFileInfo() {
		return fileInfo;
	}

	/**
	 * @return the author name
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * Downloads version should prompts the user a warning about potential risks unless the version
	 * is marked as reviewed.
	 *
	 * @return the version review state
	 */
	public OreReviewState getReviewState() {
		return reviewState;
	}

	/**
	 * @return stability, release type, platforms and more
	 */
	public OreVersionTags getTags() {
		return tags;
	}

	/**
	 * @return the post id in the forums. you probably have no use for this.
	 */
	public int getDiscoursePostId() {
		return discoursePostId;
	}

	/**
	 * The changelog is not included in the normal versions response but has to be queried through
	 * a separate endpoint. Once that endpoint was called the changelog will remain available in
	 * this version object until the cache expires.<br>
	 * If no changelog was returned from ore this will return empty, but never again null.
	 *
	 * @return the version changelog if fetched from remote, null otherwise
	 */
	public String getChangelog() {
		return changelog;
	}
	//endregion

	/**
	 * Not meant to be called manually. This function will update the changelog from the
	 * changelog response.
	 *
	 * @param changelog new changelog
	 */
	public void updateChangelog(String changelog) {
		this.changelog = changelog != null ? changelog : "";
	}

}
