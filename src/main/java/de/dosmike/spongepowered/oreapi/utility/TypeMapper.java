package de.dosmike.spongepowered.oreapi.utility;

public interface TypeMapper<T, R> {
	Class<T> getSourceType();

	Class<R> getNativeType();

	R fromSource(T t);

	T fromNative(R t);
}
