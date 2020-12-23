package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonTags;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;

import java.io.Serializable;

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

	public OreStability getStability() {
		return stability;
	}

	public OreReleaseType getReleaseType() {
		return releaseType;
	}

	public OrePlatform[] getPlatforms() {
		return platforms;
	}
}
