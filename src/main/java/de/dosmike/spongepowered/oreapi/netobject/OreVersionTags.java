package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.OreApiV2;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonTags;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;

import java.io.Serializable;

/**
 * The version tags are a collection of meta data about the version.
 * They are among the only thing you can edit after a version was created.
 */
public class OreVersionTags implements Serializable {

	@FromJson("mixin")
	boolean mixin;
	@FromJson("stability")
	@JsonTags("patchVersion")
	OreStability stability;
	@FromJson(value = "release_type", optional = true)
	@JsonTags("patchVersion")
	OreReleaseType releaseType;
	@FromJson("platforms")
	@JsonTags("patchVersion")
	OrePlatform[] platforms;

	public OreVersionTags(JsonObject object) {
		JsonUtil.fillSelf(this, object);
	}

	public boolean isMixin() {
		return mixin;
	}

	/**
	 * @return the stability of this version
	 */
	public OreStability getStability() {
		return stability;
	}

	/**
	 * If you want to commit changes to this value you'll have to update the owning version on the remote.
	 *
	 * @param stability set the new stability of this version
	 * @see OreVersion#update(OreApiV2)
	 */
	public void setStability(OreStability stability) {
		this.stability = stability;
	}

	/**
	 * @return get the current release type
	 */
	public OreReleaseType getReleaseType() {
		return releaseType;
	}

	/**
	 * If you want to commit changes to this value you'll have to update the owning version on the remote.
	 *
	 * @param releaseType the new release type for this version
	 * @see OreVersion#update(OreApiV2)
	 */
	public void setReleaseType(OreReleaseType releaseType) {
		this.releaseType = releaseType;
	}

	/**
	 * @return the current collection of platforms assigned
	 */
	public OrePlatform[] getPlatforms() {
		return platforms;
	}

	/**
	 * For setting platforms, the backend uses SimplePlatforms, consisting on only of platform name and
	 * optional platform version. The other values will be discarded for the update request.
	 * If you want to commit changes to this value you'll have to update the owning version on the remote.
	 *
	 * @param platforms the new set of platforms
	 */
	public void setPlatforms(OrePlatform[] platforms) {
		this.platforms = platforms;
	}

	/**
	 * Get a specific platform from the list. This exists mainly to complete the bean property.
	 *
	 * @param index platform index to return
	 * @return the platform at the specified index
	 */
	public OrePlatform getPlatform(int index) {
		return platforms[index];
	}

	/**
	 * Set a specific platform in the list. This exists mainly to complete the bean property.
	 * If you edit a platform from the list you don't need to set it again.
	 * If you want to commit changes to this value you'll have to update the owning version on the remote.
	 *
	 * @param index    the platform index to set
	 * @param platform the platform to set to the index
	 */
	public void setPlatform(int index, OrePlatform platform) {
		this.platforms[index] = platform;
	}

}
