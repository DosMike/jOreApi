package de.dosmike.spongepowered.oreapi.netobject;

/**
 * This reflects the ore enum for project stability.
 * Enum values are created with the promise that value#name().equalsIgnoreCase(remoteName).
 */
public enum OreStability {

	Recommended, Stable, Beta, Alpha, Bleeding, Unsupported, Broken;

	public static OreStability fromString(String string) {
		for (OreStability stability : values()) {
			if (stability.name().equalsIgnoreCase(string))
				return stability;
		}
		throw new IllegalArgumentException("No such OreStability " + string);
	}

}
