package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonArray;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Wrapper for list results that do not own pagination information although
 * accepting limit and offset parameters.
 * This wraps a json array into a list of parsed objects.
 */
public class HeadlessPaginationList<T> implements Supplier<List<T>>, Serializable {

	final private T[] elements;

	/**
	 * Constructor for parsing a json array of uniform objects into specified instances.
	 *
	 * @param array     input data
	 * @param typeClass the class to construct instances from
	 */
	public HeadlessPaginationList(JsonArray array, Class<T> typeClass) {
		elements = JsonUtil.fillArray(array, typeClass);
	}

	/**
	 * @return the all entries in the response as list
	 */
	@Override
	public List<T> get() {
		return new ArrayList<>(Arrays.asList(elements));
	}
}
