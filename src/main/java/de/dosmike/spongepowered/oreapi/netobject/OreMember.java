package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;

import java.io.Serializable;

public class OreMember implements Serializable {

	@FromJson("user")
	private String username;
	@FromJson("role")
	private OreRoleInfo role;

	public OreMember(JsonObject fromJson) {
		JsonUtil.fillSelf(this, fromJson);
	}

	//region getter

	/**
	 * @return the username for this user-&gt;role mapping
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the role information for this user
	 */
	public OreRoleInfo getRoleInfo() {
		return role;
	}
	//endregion
}
