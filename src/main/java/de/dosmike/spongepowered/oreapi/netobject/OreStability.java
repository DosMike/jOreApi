package de.dosmike.spongepowered.oreapi.netobject;

public enum OreStability {

    Recommended, Stable, Beta, Alpha, Bleeding, Unsupported, Broken;

    public static OreStability fromString(String string) {
        for (OreStability stability : values()) {
            if (stability.name().equalsIgnoreCase(string))
                return stability;
        }
        throw new IllegalArgumentException("No such OreStability "+string);
    }

}
