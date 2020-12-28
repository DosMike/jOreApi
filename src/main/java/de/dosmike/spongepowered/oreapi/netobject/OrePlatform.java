package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonTags;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * parses both SimplePlatform and VersionPlatform.
 * Keep in mind, that SimplePlatform will not provide a display platform version or minecraft version.
 * platform version is optional.
 */
public class OrePlatform implements Serializable {

	@FromJson("platform")
	@JsonTags("patchVersion")
	String platform;
	@FromJson(value = "platform_version", optional = true)
	@JsonTags("patchVersion")
	String platformVersion;
	@FromJson(value = "display_platform_version", optional = true)
	String displayPlatformVersion;
	@FromJson(value = "minecraft_version", optional = true)
	String minecraftVersion;

	/**
	 * Create the namespace from a JsonObject. This is used for JsonUtil#fillSelf.
	 *
	 * @param object the json scoped into namespace information
	 */
	public OrePlatform(JsonObject object) {
		JsonUtil.fillSelf(this, object);
	}

	/**
	 * Creates a "SimplePlatform" that can be used to update a version.
	 * A simple platform contains only name and version. Display version and Minecraft version
	 * will be generated by ore if possible.
	 *
	 * @param platform        the platform name
	 * @param platformVersion the version for the platform
	 */
	public OrePlatform(String platform, String platformVersion) {
		this.platform = platform;
		this.platformVersion = platformVersion;
	}

	/**
	 * @return the name of the platform
	 */
	public String getName() {
		return platform;
	}

	/**
	 * @return the platform version. can be formatted as range like [7.0,)
	 */
	@Nullable
	public String getPlatformVersion() {
		return platformVersion;
	}

	/**
	 * @return the human readable platform version. should not be a range.
	 */
	@Nullable
	public String getDisplayPlatformVersion() {
		return displayPlatformVersion;
	}

	/**
	 * @return the supported minecraft version
	 */
	@Nullable
	public String getMinecraftVersion() {
		return minecraftVersion;
	}

	/**
	 * The version, formatted roughly <tt>platform (displayVersion, minecraftVersion)</tt>.<br>
	 * If display version is not available the full platform version is used or "v?".<br>
	 * If minecraft version is not available it's skipped.
	 */
	@Override
	public String toString() {
		String val = platform + " (";
		if (displayPlatformVersion != null) val += displayPlatformVersion;
		else if (platformVersion != null) val += platformVersion;
		else val += "v?";
		if (minecraftVersion != null) val += ", " + minecraftVersion;
		return val + ")";
	}
}
