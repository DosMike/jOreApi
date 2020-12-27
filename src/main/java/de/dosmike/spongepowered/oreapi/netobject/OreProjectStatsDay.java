package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;

import java.io.Serializable;

public class OreProjectStatsDay implements Serializable {

	@FromJson("views")
	private int views;
	@FromJson("downloads")
	private int downloads;

	public OreProjectStatsDay(JsonObject fromJson) {
		JsonUtil.fillSelf(this, fromJson);
	}

	//region getters
	public int getViews() {
		return views;
	}

	public int getDownloads() {
		return downloads;
	}
	//endregion

}
