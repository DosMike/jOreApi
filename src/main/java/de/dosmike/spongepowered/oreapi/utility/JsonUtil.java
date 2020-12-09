package de.dosmike.spongepowered.oreapi.utility;

import com.google.gson.*;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Could probably change to gson, but this was fun.
 * Fills object instances from json.
 */
public class JsonUtil {

	//region from json
	public static void fillSelf(Object instance, JsonObject source) {
		Class<?> clz = instance.getClass();
		while (clz != Object.class) {
			fillSelfOfType(instance, clz, source);
			clz = clz.getSuperclass();
		}
	}

	private static void fillSelfOfType(Object instance, Class<?> type, JsonObject source) {
		Field[] fields = type.getDeclaredFields();
		for (Field f : fields) {
			FromJson j = f.getAnnotation(FromJson.class);
			if (j == null) continue;
			try {
				f.setAccessible(true);
				Class<?> elementType = f.getType();
				String[] pelem = j.value().split("\\.");
				JsonElement jchild = source;
				for (int i = 0; i < pelem.length && jchild != null; i++) {
					if (!jchild.isJsonObject()) jchild = null;
					else jchild = jchild.getAsJsonObject().get(pelem[i]);
				}
				if (!j.optional() && (jchild == null || jchild.isJsonNull())) {
					throw new RuntimeException("Missing non-optional field '" + j.value() + "'");
				}
				if (elementType.isArray() && !jchild.isJsonArray()) {
					throw new RuntimeException("Expected array where non-array json element supplied at '" + j.value() + "'");
				} else if (!elementType.isArray() && jchild.isJsonArray()) {
					throw new RuntimeException("Expected non-array where array json element supplied at '" + j.value() + "'");
				}

				boolean asArray = false;
				if (elementType.isArray()) {
					elementType = elementType.getComponentType();
					asArray = true;
				}

				TypeMapper<Object, Object> mapper = null;
				if (!TypeMappers.IdentityMapper.class.isAssignableFrom(j.mapper())) try {
					//construct mapper
					Constructor<?> con = j.mapper().getConstructor();
					con.setAccessible(true);
					mapper = (TypeMapper<Object, Object>) con.newInstance();

					//check that mapper output type is field type
					if (!ReflectionHelper.isAssignable(mapper.getNativeType(), elementType)) {
						throw new RuntimeException("Mapper output is not assignable to field type");
					}
					//input type is now the element type
					elementType = mapper.getSourceType();

				} catch (IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException e) {
					throw new RuntimeException("Could not instantiate mapper '" + j.mapper().getSimpleName() + "'");
				}

				Function<JsonElement, Object> parser;
				if (ReflectionHelper.isAssignable(elementType, String.class)) {
					if (asArray)
						parser = (a) -> parseArray(a, JsonUtil::parserOptString, String.class);
					else
						parser = JsonUtil::parserOptString;
				} else if (ReflectionHelper.isAssignable(elementType, boolean.class)) {
					if (asArray)
						parser = (a) -> parseArray(a, JsonUtil::parserOptBoolean, Boolean.TYPE);
					else
						parser = JsonUtil::parserOptBoolean;
				} else if (ReflectionHelper.isAssignable(elementType, long.class)) {
					if (asArray)
						parser = (a) -> parseArray(a, JsonUtil::parserOptLong, Long.TYPE);
					else
						parser = JsonUtil::parserOptLong;
				} else if (ReflectionHelper.isAssignable(elementType, int.class)) {
					if (asArray)
						parser = (a) -> parseArray(a, JsonUtil::parserOptInteger, Integer.TYPE);
					else
						parser = JsonUtil::parserOptInteger;
				} else if (elementType.isEnum()) {
					final Class<Enum<?>> finalElementType = (Class<Enum<?>>) elementType;
					if (asArray)
						parser = (a) -> parseArray(a, (json) -> parserOptEnum(json, finalElementType), finalElementType);
					else
						parser = (json) -> parserOptEnum(json, finalElementType);
				} else {
					final Class<?> finalElementType = elementType;
					if (asArray)
						parser = (json) -> {
							if (!json.isJsonArray()) return Array.newInstance(finalElementType, 0);
							JsonArray array = json.getAsJsonArray();
							Object parsedObjects = Array.newInstance(finalElementType, array.size());
							for (int i = 0; i < array.size(); i++) {
								Array.set(parsedObjects, i, parserOptObject(array.get(i), finalElementType));
							}
							return (Object[]) parsedObjects;
						};
					else
						parser = (json) -> parserOptObject(json, finalElementType);
				}

				Object result = parser.apply(jchild);

				if (mapper != null) {
					if (asArray) {
						//map array to field type
						Object tmp = Array.newInstance(f.getClass(), Array.getLength(result));
						for (int i = 0; i < Array.getLength(result); i++) {
							Array.set(tmp, i, mapper.fromSource(Array.get(result, i)));
						}
						f.set(instance, tmp);
					} else {
						f.set(instance, mapper.fromSource(result));
					}
				} else {
					f.set(instance, result);
				}

			} catch (IllegalAccessException e) {
				String rem = "\nParent JSON: " + source.toString();
				throw new RuntimeException("Could not fill field " + f.getName() + rem);
			} catch (Throwable e) {
				String rem = "\nParent JSON: " + source.toString();
				throw new RuntimeException("Could not fill field " + f.getName() + rem, e);
			}
		}
	}

	private static boolean parserOptBoolean(JsonElement element) {
		if (element == null || element.isJsonNull()) return false;
		return element.getAsBoolean();
	}

	private static int parserOptInteger(JsonElement element) {
		if (element == null || element.isJsonNull()) return 0;
		return element.getAsInt();
	}

	private static long parserOptLong(JsonElement element) {
		if (element == null || element.isJsonNull()) return 0L;
		return element.getAsLong();
	}

	private static String parserOptString(JsonElement element) {
		if (element == null || element.isJsonNull()) return null;
		return element.getAsString();
	}

	private static Enum<?> parserOptEnum(JsonElement element, Class<Enum<?>> enumClass) {
		if (element == null || element.isJsonNull()) return null;
		try {
			Method fromString = enumClass.getDeclaredMethod("fromString", String.class);
			fromString.setAccessible(true);
			return (Enum<?>) fromString.invoke(null, element.getAsString());
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			return null;
		}
	}

	private static <T extends Object> T parserOptObject(JsonElement element, Class<T> tClass) {
		if (element == null || element.isJsonNull() || !(element instanceof JsonObject)) {
			return null;
		} else {
			try {
				Constructor<T> constructor = tClass.getConstructor(JsonObject.class);
				constructor.setAccessible(true);
				return constructor.newInstance(element);
			} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
				if (e.getCause() != null) {
					throw new RuntimeException(e.getMessage(), e.getCause());
				} else {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		}
	}
	//endregion

	//region to json
	private static <T extends Object> T[] parseArray(JsonElement element, Function<JsonElement, T> elementParser, Class<T> tClass) {
		if (!element.isJsonArray()) return (T[]) Array.newInstance(tClass, 0);
		JsonArray array = element.getAsJsonArray();
		T[] instance = (T[]) Array.newInstance(tClass, array.size());
		for (int i = 0; i < array.size(); i++) {
			instance[i] = elementParser.apply(array.get(i));
		}
		return instance;
	}

	/**
	 * will convert all annotated fields into json elements and attach them to a json object.
	 * subclasses to not have to implement a toJson function to be added as object, as only the
	 * FromJson annotation will be used.
	 *
	 * @param instance the object to jsonify
	 * @return the object as jso
	 */
	public static JsonObject buildJson(Object instance, String... tags) {
		JsonObject plain = new JsonObject();
		List<String> requiredTags = Arrays.asList(tags);

		Class<?> clz = instance.getClass();
		while (clz != Object.class) {
			buildJson(instance, clz, plain, requiredTags);
			clz = clz.getSuperclass();
		}

		return plain;
	}

	private static void buildJson(Object instance, Class<?> forClass, JsonObject target, Collection<String> tags) {
		for (Field f : forClass.getDeclaredFields()) {
			FromJson j = f.getAnnotation(FromJson.class);
			if (j == null) continue;
			if (!tags.isEmpty()) {
				JsonTags t = f.getAnnotation(JsonTags.class);
				Set<String> putTags = Arrays.stream(t.value()).collect(Collectors.toSet());
				if (!putTags.containsAll(tags)) continue;
			}
			try {
				Class<?> elementType = f.getType();
				boolean asArray = false;
				if (elementType.isArray()) {
					asArray = true;
					elementType = elementType.getComponentType();
				}

				TypeMapper<Object, Object> mapper = null;
				if (!TypeMappers.IdentityMapper.class.isAssignableFrom(j.mapper())) try {
					//construct mapper
					Constructor<?> con = j.mapper().getConstructor();
					con.setAccessible(true);
					mapper = (TypeMapper<Object, Object>) con.newInstance();

					//check that mapper output type is field type
					if (!ReflectionHelper.isAssignable(mapper.getNativeType(), elementType)) {
						throw new RuntimeException("Mapper input is not compatible with field type");
					}
					//input type is now the element type
					elementType = mapper.getSourceType();

				} catch (IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException e) {
					throw new RuntimeException("Could not instantiate mapper '" + j.mapper().getSimpleName() + "'");
				}

				Function<Object, JsonElement> mingle;
				if (ReflectionHelper.isAssignable(elementType, String.class)) {
					if (asArray)
						mingle = (a) -> mangledArray(a, JsonUtil::mangledString);
					else
						mingle = JsonUtil::mangledString;
				} else if (ReflectionHelper.isAssignable(elementType, boolean.class)) {
					if (asArray)
						mingle = (a) -> mangledArray(a, JsonUtil::mangledBoolean);
					else
						mingle = JsonUtil::mangledBoolean;
				} else if (ReflectionHelper.isAssignable(elementType, long.class) ||
						ReflectionHelper.isAssignable(elementType, int.class)) {
					if (asArray)
						mingle = (a) -> mangledArray(a, JsonUtil::mangledNumber);
					else
						mingle = JsonUtil::mangledNumber;
				} else if (elementType.isEnum()) {
					if (asArray)
						mingle = (a) -> mangledArray(a, JsonUtil::mangledEnum);
					else
						mingle = JsonUtil::mangledEnum;
				} else {
					if (asArray)
						mingle = (a) -> mangledArray(a, JsonUtil::mangledObject);
					else
						mingle = JsonUtil::mangledObject;
				}

				Object raw = f.get(instance);

				//prepare native
				Object mapOut;
				if (mapper != null) {
					if (asArray) {
						mapOut = Array.newInstance(mapper.getSourceType(), Array.getLength(raw));
						for (int i = 0; i < Array.getLength(raw); i++) {
							Array.set(mapOut, i, mapper.fromNative(Array.get(raw, i)));
						}
					} else {
						mapOut = mapper.fromNative(raw);
					}
				} else {
					mapOut = raw;
				}

				JsonElement value = mingle.apply(mapOut);

				String[] pelem = j.value().split("\\.");
				if (pelem.length > 1) {
					JsonObject onto = target;
					for (int i = 0; i < pelem.length - 1; i++) {
						if (!onto.has(pelem[i]) || !onto.get(pelem[i]).isJsonObject()) {
							onto.add(pelem[i], new JsonObject());
						}
						onto = onto.getAsJsonObject(pelem[i]);
					}
					onto.add(pelem[pelem.length - 1], value);
				} else {
					target.add(j.value(), value);
				}
			} catch (IllegalAccessException e) {
				String rem = "\nObject Type: " + instance.getClass().getSimpleName();
				throw new RuntimeException("Could not fill field " + f.getName() + rem);
			} catch (Throwable e) {
				String rem = "\nObject Type: " + instance.getClass().getSimpleName();
				throw new RuntimeException("Could not fill field " + f.getName() + rem, e);
			}
		}
	}

	private static JsonElement mangledString(Object s) {
		return s == null ? JsonNull.INSTANCE : new JsonPrimitive((String) s);
	}

	private static JsonElement mangledBoolean(Object n) {
		return n == null ? JsonNull.INSTANCE : new JsonPrimitive((Boolean) n);
	}

	private static JsonElement mangledNumber(Object n) {
		return n == null ? JsonNull.INSTANCE : new JsonPrimitive((Number) n);
	}

	private static JsonElement mangledEnum(Object e) {
		return e == null ? JsonNull.INSTANCE : new JsonPrimitive(((Enum<?>) e).name());
	}

	private static JsonElement mangledObject(Object o) {
		return o == null ? JsonNull.INSTANCE : buildJson(o);
	}

	private static JsonArray mangledArray(Object arrayObject, Function<Object, JsonElement> elementMangler) {
		JsonArray jar = new JsonArray();
		for (int i = 0; i < Array.getLength(arrayObject); i++) {
			jar.set(i, elementMangler.apply(Array.get(arrayObject, i)));
		}
		return jar;
	}

	//endregion
}
