package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;

import java.io.Serializable;

public class OreProjectStatsAll implements Serializable {

	@FromJson("views")
	long views;
	@FromJson("downloads")
	long downloads;
	@FromJson("recent_views")
	long recentViews;
	@FromJson("recent_downloads")
	long recentDownloads;
	@FromJson("stars")
	long stars;
	@FromJson("watchers")
	long watchers;

	public OreProjectStatsAll(JsonObject json) {
		JsonUtil.fillSelf(this, json);
	}

	public long getViews() {
		return views;
	}

	public long getDownloads() {
		return downloads;
	}

	public long getRecentViews() {
		return recentViews;
	}

	public long getRecentDownloads() {
		return recentDownloads;
	}

	public long getStars() {
		return stars;
	}

	public long getWatchers() {
		return watchers;
	}
}
