package de.dosmike.spongepowered.oreapi.netobject;

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
