package de.dosmike.spongepowered.oreapi.netobject;

import java.net.URLEncoder;
import java.util.Locale;

/**
 * Used for project information in relation to a user -
 * namely Starred and Watched Projects
 */
public class OreCompactProjectFilter extends OrePaginationFilter {

	/**
	 * Used to define how to sort the projects in the result.
	 * The default sorting is by the last update date (Updated).
	 */
	public enum Sort {
		Stars, Downloads, Views, Newest, Updated, Only_Relevance, Recent_Downloads, Recent_Views
	}

	Sort sort = Sort.Updated;

	public OreCompactProjectFilter() {
		super();
	}

	public OreCompactProjectFilter(int limit, int offset) {
		super(limit, offset);
	}

	/**
	 * @param sorting what order the projects should be returned in
	 */
	public void setSorting(Sort sorting) {
		this.sort = sorting;
	}

	/**
	 * @return what order the projects should be returned in
	 */
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
