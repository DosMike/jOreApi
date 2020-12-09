package de.dosmike.spongepowered.oreapi.netobject;

public enum OreVisibility {

	Public, New, NeedsChanges, NeedsApproval, SoftDelete;

	public static OreVisibility fromString(String string) {
		for (OreVisibility visibility : values()) {
			if (visibility.name().equalsIgnoreCase(string))
				return visibility;
		}
		throw new IllegalArgumentException("No such OreStability " + string);
	}


}
