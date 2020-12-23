package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;

import java.util.Arrays;

public class OreRoleInfo {

	@FromJson("name")
	private OreRole role;
	@FromJson("title")
	private String title;
	@FromJson("color")
	private String color;
	@FromJson("permissions")
	private OrePermission[] permissions;
	@FromJson("is_accepted")
	private boolean isAccepted;

	public OreRoleInfo(JsonObject fromJson) {
		JsonUtil.fillSelf(this, fromJson);
	}

	//region getter
	public OreRole getRole() {
		return role;
	}

	/**
	 * Human readable version of the OreRole
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * The role color seems to be a web-safe color name. Non-moderation users seem to get the
	 * color "transparent"
	 */
	public String getColor() {
		return color;
	}

	/**
	 * Get all permissions for this member in the project scope
	 */
	public OrePermissionGrant getPermissions() {
		return new OrePermissionGrant(Arrays.asList(permissions.clone()));
	}

	/**
	 * When you add a member to a project the opposite party receives what is equal to an invite.
	 * The first have to accept the role within the project.
	 * This flag indicates whether they have accepted the role or not.
	 *
	 * @return true if the role was accepted
	 */
	public boolean isAccepted() {
		return isAccepted;
	}
	//endregion

}