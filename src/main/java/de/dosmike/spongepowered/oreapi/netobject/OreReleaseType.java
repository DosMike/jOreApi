package de.dosmike.spongepowered.oreapi.netobject;

/**
 * This reflects the ore enum for release types.
 * Enum values are created with the promise that value#name().equalsIgnoreCase(remoteName).
 */
public enum OreReleaseType {

    Major_Update, Minor_Update, Patches, Hotfix;

    public static OreReleaseType fromString(String string) {
        for (OreReleaseType type : values()) {
            if (type.name().equalsIgnoreCase(string))
                return type;
        }
        throw new IllegalArgumentException("No such OreReleaseType " + string);
    }

}
