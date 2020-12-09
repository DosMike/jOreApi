package de.dosmike.spongepowered.oreapi.netobject;

public enum OrePermission {

	View_Public_Info,
	Edit_Own_User_Settings,
	Edit_Api_Keys,
	Edit_Subject_Settings,
	Manage_Subject_Members,
	Is_Subject_Owner,
	Create_Project,
	Edit_Page,
	Delete_Project,
	Create_Version,
	Edit_Version,
	Delete_Version,
	Edit_Tags,
	Create_Organization,
	Post_As_Organization,
	Mod_Notes_And_Flags,
	See_Hidden,
	Is_Staff,
	Reviewer,
	View_Health,
	View_IP,
	View_Stats,
	View_Logs,
	Manual_Value_Changes,
	Hard_Delete_Project,
	Hard_Delete_Version,
	Edit_All_User_Settings,
	;

	public static OrePermission fromString(String string) {
		for (OrePermission permission : values()) {
			if (permission.name().equalsIgnoreCase(string))
				return permission;
		}
		throw new IllegalArgumentException("No such OrePermission " + string);
	}

}
