package de.dosmike.spongepowered.oreapi.netobject;

/**
 * This reflects the ore enum for review states.
 * Enum values are created with the promise that value#name().equalsIgnoreCase(remoteName).
 */
public enum OreReviewState {

    Unreviewed,
    Reviewed,
    Backlog,
    Partially_Reviewed,
    ;

    public static OreReviewState fromString(String string) {
        for (OreReviewState state : values()) {
            if (state.name().equalsIgnoreCase(string))
                return state;
        }
        throw new IllegalArgumentException("No such OreReviewState " + string);
    }

}
