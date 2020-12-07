package de.dosmike.spongepowered.oreapi.utility;

import java.util.function.Function;

public interface TypeMapper<T,R> extends Function<T,R> {
    Class<T> getInputType();
    Class<R> getOutputType();
}
