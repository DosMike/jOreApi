package de.dosmike.spongepowered.oreapi.netobject;

/**
 * This reflects the ore enum for project categories.
 * Enum values are created with the promise that value#name().equalsIgnoreCase(remoteName).
 */
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
