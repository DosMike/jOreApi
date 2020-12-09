package de.dosmike.spongepowered.oreapi.netobject;

public enum OreCategory {

	Admin_Tools,
	Chat,
	Dev_Tools,
	Economy,
	Gameplay,
	Games,
	Protection,
	Role_Playing,
	World_Management,
	Misc,
	;

	public static OreCategory fromString(String string) {
		for (OreCategory category : values()) {
			if (category.name().equalsIgnoreCase(string))
				return category;
		}
		throw new IllegalArgumentException("No such OreCategory " + string);
	}

}
