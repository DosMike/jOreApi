package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

public class OreResultList<T, F extends OrePaginationFilter> implements Serializable {

	OrePagination<F> pagination;
	LinkedList<T> result = new LinkedList<>();

	protected T constructInstanceFromJson(Class<T> clazz, JsonObject json) {
		try {
			Constructor<T> constructor = clazz.getConstructor(JsonObject.class);
			constructor.setAccessible(true);
			return constructor.newInstance(json.getAsJsonObject());
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new UnsupportedOperationException("The target class " + clazz.getSimpleName() + " could not be constructed from Json", e);
		}
	}

	public OreResultList(JsonObject object, Class<T> resultClass, F previousFilter) {
		this(object, previousFilter);
		lateParse(object, resultClass);
	}

	/**
	 * don't forget to call {@link #lateParse} because Java 8 is a steaming pile of
	 */
	public OreResultList(JsonObject object, F previousFilter) {
		pagination = new OrePagination<>(object.get("pagination").getAsJsonObject(), previousFilter);
	}

	/**
	 * "Call to 'super()' must be first statement in constructor body" - fuck you too
	 */
	protected void lateParse(JsonObject object, Class<T> resultClass) {
		JsonArray array = object.getAsJsonArray("result");
		for (int i = 0; i < array.size(); i++)
			result.add(constructInstanceFromJson(resultClass, array.get(i).getAsJsonObject()));
	}

	public OrePagination<F> getPagination() {
		return pagination;
	}

	public List<T> getResult() {
		return result;
	}

}
