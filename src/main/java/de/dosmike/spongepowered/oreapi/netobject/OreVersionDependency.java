package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;

import java.io.Serializable;

public class OreVersionDependency implements Serializable {

	@FromJson("plugin_id")
	String pluginId;
	@FromJson(value = "version", optional = true)
	String version;

	public OreVersionDependency(JsonObject object) {
		JsonUtil.fillSelf(this, object);
	}

	/**
	 * Please note that dependencies are not required to be hosted on ore.
	 * For this reason you might not be able to find this project through the api.
	 *
	 * @return the project id that this plugin depends on
	 */
	public String getPluginId() {
		return pluginId;
	}

	/**
	 * NOTE: might be a version range.<br>
	 * Details on VersionRange <a href="https://docs.oracle.com/middleware/1212/core/MAVEN/maven_version.htm#CJHDEHAB">https://docs.oracle.com/middleware/1212/core/MAVEN/maven_version.htm#CJHDEHAB</a><br>
	 * NOTE: Versions are not guaranteed to have any format! Thus all versions are handled as Strings.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Check if plugin id and version match
	 */
	public boolean equals(OreVersionDependency other) {
		if ((this.version == null) != (other.version == null)) return false;
		return this.pluginId.equalsIgnoreCase(other.pluginId) &&
				(this.version == null || this.version.equals(other.version));
	}
}
