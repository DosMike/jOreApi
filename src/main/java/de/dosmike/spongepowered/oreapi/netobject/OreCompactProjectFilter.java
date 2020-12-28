package de.dosmike.spongepowered.oreapi.netobject;

import java.net.URLEncoder;
import java.util.Locale;

/**
 * Used for project information in relation to a user -
 * namely Starred and Watched Projects
 */
public class OreCompactProjectFilter extends OrePaginationFilter {

	public enum Sort {
		Stars, Downloads, Views, Newest,
		/**
		 * this is the default
		 */
		Updated, Only_Relevance, Recent_Downloads, Recent_Views
	}

	Sort sort = Sort.Updated;

	public OreCompactProjectFilter() {

	}

	public void setSorting(Sort sorting) {
		this.sort = sorting;
	}

	public Sort getSorting() {
		return sort;
	}

	@Override
	public String toString() {
		return "sort=" + urlEncoded(sort.name().toLowerCase(Locale.ROOT))
				+ "&" + super.toString();
	}

	@Override
	public OreCompactProjectFilter clone() {
		OreCompactProjectFilter clone = new OreCompactProjectFilter();
		clone.sort = sort;
		clone.limit = limit;
		clone.offset = offset;
		return clone;
	}

	private static String urlEncoded(String string) {
		try {
			return URLEncoder.encode(string, "UTF-8");
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
