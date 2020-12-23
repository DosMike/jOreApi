package de.dosmike.spongepowered.oreapi.netobject;

import java.util.Locale;

public enum OreVisibility {

	Public, New, NeedsChanges, NeedsApproval, SoftDelete;

	public static OreVisibility fromString(String string) {
		for (OreVisibility visibility : values()) {
			if (visibility.name().equalsIgnoreCase(string))
				return visibility;
		}
		throw new IllegalArgumentException("No such OreStability " + string);
	}

	/**
	 * returns the original (remote) enum value
	 */
	public String toString() {
		return name().substring(0, 1).toLowerCase(Locale.ROOT) + name().substring(1);
	}

}
