package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

public class OreResultList<T, F extends OrePaginationFilter> implements Serializable {

	OrePagination<F> pagination;
	LinkedList<T> result = new LinkedList<>();

	/**
	 * Builds one T element from a Json array element.
	 * This requires the class to have a constructor that accepts a single JsonObject
	 *
	 * @param clazz the type to construct
	 * @param json  the json array element to use for construction
	 * @return The filled instance
	 */
	protected T constructInstanceFromJson(Class<T> clazz, JsonObject json) {
		try {
			Constructor<T> constructor = clazz.getConstructor(JsonObject.class);
			constructor.setAccessible(true);
			return constructor.newInstance(json.getAsJsonObject());
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new UnsupportedOperationException("The target class " + clazz.getSimpleName() + " could not be constructed from Json", e);
		}
	}

	/**
	 * Constructs a default list where constructInstanceFromJson does not require
	 * member variables in this list instance.
	 *
	 * @param object         the Pagination json object
	 * @param resultClass    the element class for the result list
	 * @param previousFilter the filter to use as base for pagination
	 */
	public OreResultList(JsonObject object, Class<T> resultClass, @NotNull F previousFilter) {
		this(object, previousFilter);
		lateParse(object, resultClass);
	}

	/**
	 * This constructs the pagination but does not parse the array yet.
	 * This allows for member variables to be set up for later access in
	 * constructInstanceFromJson<br>
	 * <b>Important: don't forget to call {@link #lateParse}</b>
	 *
	 * @param object         the Pagination json object
	 * @param previousFilter the filter to use as base for pagination
	 */
	public OreResultList(JsonObject object, @NotNull F previousFilter) {
		pagination = new OrePagination<>(object.get("pagination").getAsJsonObject(), previousFilter);
	}

	/**
	 * "Call to 'super()' must be first statement in constructor body"<br>
	 * Subclasses that have to manage member variables before parsing the json have to call this manually.
	 *
	 * @param object      the JsonObject to read the result elements from
	 * @param resultClass the element class to parse to
	 */
	protected void lateParse(JsonObject object, Class<T> resultClass) {
		JsonArray array = object.getAsJsonArray("result");
		for (int i = 0; i < array.size(); i++)
			result.add(constructInstanceFromJson(resultClass, array.get(i).getAsJsonObject()));
	}

	/**
	 * Get the pagination object. This enables you to quickly get a new
	 * pagination instance for a following query.
	 *
	 * @return pagination for this result.
	 */
	public OrePagination<F> getPagination() {
		return pagination;
	}

	/**
	 * The result is the list of all parsed elements from a request.
	 * @return A list of elements
	 */
	public List<T> getResult() {
		return result;
	}

}
