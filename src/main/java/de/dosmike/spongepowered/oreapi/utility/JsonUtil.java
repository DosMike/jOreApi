package de.dosmike.spongepowered.oreapi.utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.*;
import java.util.function.Function;

/**
 * Could probably change to gson, but this was fun.
 * Fills object instances from json.
 */
public class JsonUtil {

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
            if (j==null) continue;
            try {
                f.setAccessible(true);
                Class<?> elementType = f.getType();
                String[] pelem = j.value().split("\\.");
                JsonElement jchild = source;
                for (int i=0; i<pelem.length && jchild != null; i++) {
                    if (!jchild.isJsonObject()) jchild = null;
                    else jchild = jchild.getAsJsonObject().get(pelem[i]);
                }
                if (!j.optional() && (jchild == null || jchild.isJsonNull())) {
                    throw new RuntimeException("Missing non-optional field '"+j.value()+"'");
                }
                if (elementType.isArray() && !jchild.isJsonArray()) {
                    throw new RuntimeException("Expected array where non-array json element supplied at '"+j.value()+"'");
                } else if (!elementType.isArray() && jchild.isJsonArray()) {
                    throw new RuntimeException("Expected non-array where array json element supplied at '"+j.value()+"'");
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
                    mapper = (TypeMapper<Object, Object>)con.newInstance();

                    //check that mapper output type is field type
                    if (!ReflectionHelper.isAssignable(mapper.getOutputType(), elementType)) {
                        throw new RuntimeException("Mapper output is not assignable to field type");
                    }
                    //input type is now the element type
                    elementType = mapper.getInputType();

                } catch (IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException e) {
                    throw new RuntimeException("Could not instantiate mapper '"+j.mapper().getSimpleName()+"'");
                }

                Function<JsonElement, Object> parser;
                if (ReflectionHelper.isAssignable(elementType, String.class)) {
                    if (asArray)
                        parser = (a)->parseArray(a, JsonUtil::parserOptString, String.class);
                    else
                        parser = JsonUtil::parserOptString;
                } else if (ReflectionHelper.isAssignable(elementType, boolean.class)) {
                    if (asArray)
                        parser = (a)->parseArray(a, JsonUtil::parserOptBoolean, Boolean.TYPE);
                    else
                        parser = JsonUtil::parserOptBoolean;
                } else if (ReflectionHelper.isAssignable(elementType, long.class)) {
                    if (asArray)
                        parser = (a)->parseArray(a, JsonUtil::parserOptLong, Long.TYPE);
                    else
                        parser = JsonUtil::parserOptLong;
                } else if (ReflectionHelper.isAssignable(elementType, int.class)) {
                    if (asArray)
                        parser = (a)->parseArray(a, JsonUtil::parserOptInteger, Integer.TYPE);
                    else
                        parser = JsonUtil::parserOptInteger;
                } else if (elementType.isEnum()) {
                    final Class<Enum<?>> finalElementType = (Class<Enum<?>>) elementType;
                    if (asArray)
                        parser = (a)->parseArray(a, (json)->parserOptEnum(json,finalElementType), finalElementType);
                    else
                        parser = (json)->parserOptEnum(json,finalElementType);
                } else {
                    final Class<?> finalElementType = elementType;
                    if (asArray)
                        parser = (json)->{
                            if (!json.isJsonArray()) return Array.newInstance(finalElementType, 0);
                            JsonArray array = json.getAsJsonArray();
                            Object parsedObjects = Array.newInstance(finalElementType, array.size());
                            for (int i = 0; i < array.size(); i++) {
                                Array.set(parsedObjects, i, parserOptObject(array.get(i), finalElementType));
                            }
                            return (Object[])parsedObjects;
                        };
                    else
                        parser = (json)->parserOptObject(json, finalElementType);
                }

                Object result = parser.apply(jchild);

                if (mapper != null) {
                    if (asArray) {
                        //map array to field type
                        Object tmp = Array.newInstance(f.getClass(), Array.getLength(result));
                        for (int i = 0; i < Array.getLength(result); i++) {
                            Array.set(tmp,i, mapper.apply ( Array.get(result,i) ) );
                        }
                        f.set(instance, tmp);
                    } else {
                        f.set(instance, mapper.apply( result ));
                    }
                } else {
                    f.set(instance, result);
                }

            } catch (IllegalAccessException e) {
                String rem = "\nParent JSON: "+source.toString();
                throw new RuntimeException("Could not fill field "+f.getName()+rem);
            } catch (Throwable e) {
                String rem = "\nParent JSON: "+source.toString();
                throw new RuntimeException("Could not fill field "+f.getName()+rem, e);
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
                if (e.getCause()!=null) {
                    throw new RuntimeException(e.getMessage(), e.getCause());
                } else {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
    }

    private static <T extends Object> T[] parseArray(JsonElement element, Function<JsonElement,T> elementParser, Class<T> tClass) {
        if (!element.isJsonArray()) return (T[])Array.newInstance(tClass, 0);
        JsonArray array = element.getAsJsonArray();
        T[] instance = (T[])Array.newInstance(tClass, array.size());
        for (int i = 0; i < array.size(); i++) {
            instance[i] = elementParser.apply(array.get(i));
        }
        return instance;
    }

}
