package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.OreApiV2;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonTags;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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

	//region getters
	public List<String> getKeywords() {
		return new ArrayList<>(Arrays.asList(keywords));
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
	//endregion

	//region setters

	/**
	 * Specifying keywords will help people find your project on ore. You can set up to 5 keywords.
	 * If more than 5 Keywords are passed to this method and exception will be thrown.
	 * If you want to commit changes to this value you'll have to update the owning object on the remote.
	 *
	 * @param keywords the new set of keywords for this project
	 * @see OreApiV2#updateProject
	 * @see OreProject#update
	 */
	public void setKeywords(Collection<String> keywords) {
		this.keywords = keywords.toArray(new String[0]);
	}

	/**
	 * Change this projects homepage url.
	 * If you want to commit changes to this value you'll have to update the owning object on the remote.
	 *
	 * @param homepageUrl the new url
	 * @see OreApiV2#updateProject
	 * @see OreProject#update
	 */
	public void setHomepageUrl(String homepageUrl) {
		this.homepageUrl = homepageUrl;
	}

	/**
	 * Change this projects issue tracker url.
	 * If you want to commit changes to this value you'll have to update the owning object on the remote.
	 *
	 * @param issuesUrl the new url
	 * @see OreApiV2#updateProject
	 * @see OreProject#update
	 */
	public void setIssuesUrl(String issuesUrl) {
		this.issuesUrl = issuesUrl;
	}

	/**
	 * Change this projects source code url.
	 * If you want to commit changes to this value you'll have to update the owning object on the remote.
	 *
	 * @param sourcesUrl the new url
	 * @see OreApiV2#updateProject
	 * @see OreProject#update
	 */
	public void setSourcesUrl(String sourcesUrl) {
		this.sourcesUrl = sourcesUrl;
	}

	/**
	 * Change this projects support url.
	 * If you want to commit changes to this value you'll have to update the owning object on the remote.
	 *
	 * @param supportUrl the new url
	 * @see OreApiV2#updateProject
	 * @see OreProject#update
	 */
	public void setSupportUrl(String supportUrl) {
		this.supportUrl = supportUrl;
	}

	/**
	 * Change this projects license. The name is intended to be well known name. Examples are:<ul>
	 * <li>MIT</li>
	 * <li>Apache 2.0</li>
	 * <li>GNU General Public License (GPL)</li>
	 * <li>GNU Lesser General Public License (LGPL)</li>
	 * </ul>
	 * The license url should match your chosen license name and point to your
	 * instance of the license file, preferably within your sourcecode repository.
	 * <br>
	 * If you want to commit changes to this value you'll have to update the owning object on the remote.
	 *
	 * @param licenseName the name for your projects license
	 * @param licenseUrl  the url for your projects license
	 * @see OreApiV2#updateProject
	 * @see OreProject#update
	 */
	public void setLicenseName(String licenseName, String licenseUrl) {
		this.licenseName = licenseName;
		this.licenseUrl = licenseUrl;
	}

	/**
	 * Set whether this project syncs up with the forums or not. This means that changes to your projects
	 * description (home page) will be reflected in the matching forum threads first post.
	 * If you want to commit changes to this value you'll have to update the owning object on the remote.
	 *
	 * @param forumSync true if you want the forum to be synced
	 * @see OreApiV2#updateProject
	 * @see OreProject#update
	 */
	public void setForumSync(boolean forumSync) {
		this.forumSync = forumSync;
	}
	//endregion
}
