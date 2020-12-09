package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonArray;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class HeadlessPaginationList<T> implements Supplier<List<T>> {

	private T[] elements;

	public HeadlessPaginationList(JsonArray array, Class<T> typeClass) {
		elements = JsonUtil.fillArray(array, typeClass);
	}

	@Override
	public List<T> get() {
		return new ArrayList<>(Arrays.asList(elements));
	}
}
