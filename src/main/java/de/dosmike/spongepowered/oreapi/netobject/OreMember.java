package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;

public class OreMember {

	@FromJson("user")
	private String username;
	@FromJson("role")
	private OreRoleInfo role;

	public OreMember(JsonObject fromJson) {
		JsonUtil.fillSelf(this, fromJson);
	}

	//region getter
	public String getUsername() {
		return username;
	}

	public OreRoleInfo getRoleInfo() {
		return role;
	}
	//endregion
}
