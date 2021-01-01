package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;

import java.io.Serializable;

public class OreVersionStatsDay implements Serializable {

	@FromJson("downloads")
	private int downloads;

	public OreVersionStatsDay(JsonObject fromJson) {
		JsonUtil.fillSelf(this, fromJson);
	}

	//region getters
	public int getDownloads() {
		return downloads;
	}
	//endregion

}
