package de.dosmike.spongepowered.oreapi.netobject;

public enum OreRole {

	Ore_Admin,
	Ore_Mod,
	Sponge_Leader,
	Team_Leader,
	Community_Leader,
	Sponge_Staff,
	Sponge_Developer,
	Ore_Dev,
	Web_Dev,
	Documenter,
	Support,
	Contributor,
	Advisor,
	Stone_Donor,
	Quartz_Donor,
	Iron_Donor,
	Gold_Donor,
	Diamond_Donor,
	Project_Owner,
	Project_Admin,
	Project_Developer,
	Project_Editor,
	Project_Support,
	Organization,
	Organization_Owner,
	Organization_Admin,
	Organization_Developer,
	Organization_Editor,
	Organization_Support;

	public static OreRole fromString(String string) {
		for (OreRole role : values()) {
			if (role.name().equalsIgnoreCase(string))
				return role;
		}
		throw new IllegalArgumentException("No such OreRole " + string);
	}

}
