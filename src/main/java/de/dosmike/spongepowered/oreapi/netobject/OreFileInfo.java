package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;

import java.io.Serializable;

/**
 * Contains information about the file associated with a project version
 */
public class OreFileInfo implements Serializable {

	@FromJson("name")
	String name;
	@FromJson("size_bytes")
	long byteSize;
	@FromJson("md5_hash")
	String md5;

	public OreFileInfo(JsonObject object) {
		JsonUtil.fillSelf(this, object);
	}

	/**
	 * @return the file name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the file size in bytes
	 */
	public long getByteSize() {
		return byteSize;
	}

	/**
	 * @return the files md5 hash for integrity checks
	 */
	public String getMd5() {
		return md5;
	}
}
