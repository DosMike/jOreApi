package de.dosmike.spongepowered.oreapi.utility;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ReflectionHelper {

	private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<>();

	static {
		primitiveWrapperMap.put(boolean.class, Boolean.class);
		primitiveWrapperMap.put(byte.class, Byte.class);
		primitiveWrapperMap.put(char.class, Character.class);
		primitiveWrapperMap.put(double.class, Double.class);
		primitiveWrapperMap.put(float.class, Float.class);
		primitiveWrapperMap.put(int.class, Integer.class);
		primitiveWrapperMap.put(long.class, Long.class);
		primitiveWrapperMap.put(short.class, Short.class);
	}

	private static boolean isPrimitiveWrapperOf(Class<?> targetClass, Class<?> primitive) {
		if (!primitive.isPrimitive()) {
			throw new IllegalArgumentException("First argument has to be primitive type");
		}
		return primitiveWrapperMap.get(primitive) == targetClass;
	}

	public static boolean isAssignable(Class<?> from, Class<?> to) {
		if (to.isAssignableFrom(from)) {
			return true;
		}
		if (from.isPrimitive()) {
			return isPrimitiveWrapperOf(to, from);
		}
		if (to.isPrimitive()) {
			return isPrimitiveWrapperOf(from, to);
		}
		return false;
	}

	public static <T> T friendField(Object instance, String name) {
		try {
			Field f = instance.getClass().getDeclaredField(name);
			f.setAccessible(true);
			return (T) f.get(instance);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public static <T> T friendMethod(Object instance, String name) {
		try {
			Method m = instance.getClass().getDeclaredMethod(name);
			m.setAccessible(true);
			return (T) m.invoke(instance);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

}
