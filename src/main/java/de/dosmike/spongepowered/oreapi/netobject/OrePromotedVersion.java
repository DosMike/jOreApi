package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;

import java.io.Serializable;

/**
 * IMPORTANT: This is only a partial version that
 * contains the most crucial display information for recommended versions
 * to be displayed on the project page. For full version objects please
 * use the version endpoints with the version reference.
 */
public class OrePromotedVersion implements Serializable {

	OreProjectReference projectReference;
	@FromJson("version")
	String version;
	@FromJson("platforms")
	OrePlatform[] platforms;

	public OrePromotedVersion(JsonObject object) {
		JsonUtil.fillSelf(this, object);
	}

	void setProjectReference(OreProjectReference ref) {
		projectReference = ref.toReference();
	}

	/**
	 * @return the version name
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @return the platforms for this version
	 */
	public OrePlatform[] getPlatforms() {
		return platforms;
	}

	/**
	 * The internal structure of promoted versions differs a bit, now allowing it to be a
	 * full version reference. But this method converts it to one.
	 *
	 * @return a version reference of this version
	 */
	public OreVersionReference toReference() {
		return OreVersionReference.builder()
				.versionName(version)
				.project(projectReference)
				.build();
	}

}
