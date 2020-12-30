package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonTags;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;
import de.dosmike.spongepowered.oreapi.utility.ReflectiveUse;

/**
 * The compact project in this implementation is a read only reference for quick additional
 * information.<br>
 * If you want a full version, or manipulate version information you can get or fetch the whole
 * project using #with.<br>
 * <b>Note: Compact Versions are not cached</b>
 */
public class OreCompactProject extends OreProjectReference {

	/**
	 * used for updating. if you change the name/owner you can't update the repository
	 * because the change needs to be pushed against the original namespace.<br>
	 * This field will be set on construction, and be used when #update is called.
	 * After that, this instance will be invalid.
	 * TODO: maybe mark the instance invalid with a flag?
	 */
	@ReflectiveUse
	OreNamespace shadowNamespace;

	@FromJson("name")
	@JsonTags({"patchProject", "compact"})
	String name;
	@FromJson("promoted_versions")
	@JsonTags("compact")
	OrePromotedVersion[] promotedVersions;
	@FromJson("stats")
	@JsonTags("compact")
	OreProjectStatsAll stats;
	@FromJson("category")
	@JsonTags({"patchProject", "compact"})
	OreCategory category;
	@FromJson("visibility")
	@JsonTags("compact")
	OreVisibility visibility;

	protected OreCompactProject() {
	}

	/**
	 * Create an instance from a compact project json object
	 */
	public OreCompactProject(JsonObject object) {
		JsonUtil.fillSelf(this, object);
		shadowNamespace = new OreNamespace(namespace.owner, namespace.slug);
	}

	//region getter

	/**
	 * @return the name of this project
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return a list of all promoted versions
	 */
	public OrePromotedVersion[] getPromotedVersions() {
		return promotedVersions;
	}

	/**
	 * @return the total stats for this project
	 */
	public OreProjectStatsAll getStats() {
		return stats;
	}

	/**
	 * @return the category this project is listen in
	 */
	public OreCategory getCategory() {
		return category;
	}

	/**
	 * @return the visibility of this project
	 */
	public OreVisibility getVisibility() {
		return visibility;
	}
	//endregion

}
