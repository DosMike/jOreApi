package de.dosmike.spongepowered.oreapi.netobject;

import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Used to filter projects in a project search.
 */
public class OreProjectFilter extends OreCompactProjectFilter {

	String query = null;
	Set<OreCategory> categories = new HashSet<>();
	/**
	 * Strings of ^\w+(:[\w\.]+)?$
	 */
	Set<String> platforms = new HashSet<>();
	Set<OreStability> stabilities = new HashSet<>();
	String owner = null;
	boolean sortByRelevance = true;
	boolean exact = false;

	/**
	 * Convenience constructor for default pagination with only a search query set.
	 * Offset and limit per page will use the default 25 per page from 0.
	 *
	 * @param query the string to search for
	 */
	public OreProjectFilter(String query) {
		super();
		this.query = query;
	}

	/**
	 * Convenience constructor for default pagination without particular query.
	 * Offset and limit per page will use the default 25 per page from 0.
	 */
	public OreProjectFilter() {
		super();
	}

	/**
	 * Convenience constructor for custom pagination without particular query.
	 *
	 * @param limit  the maximum amount of results per page
	 * @param offset the first result to return
	 */
	public OreProjectFilter(int limit, int offset) {
		super(limit, offset);
	}

	/**
	 * Convenience constructor for custom pagination with the given search query set.
	 *
	 * @param limit  the maximum amount of results per page
	 * @param offset the first result to return
	 * @param query  the string to search for
	 */
	public OreProjectFilter(int limit, int offset, String query) {
		super(limit, offset);
		this.query = query;
	}

	/**
	 * Set to null to clear
	 *
	 * @param searchQuery the string to search for
	 */
	public void setQuery(String searchQuery) {
		this.query = searchQuery;
	}

	/**
	 * @return the current search query if set, or null otherwise
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * Clear any filter for categories
	 *
	 * @see #getCategories()
	 */
	public void clearCategories() {
		categories.clear();
	}

	/**
	 * Add categories to this filter. Projects in the result have to be in one of these categories.
	 *
	 * @param categories the categories you are interested in
	 * @see #getCategories()
	 */
	public void addCategories(OreCategory... categories) {
		this.categories.addAll(Arrays.asList(categories));
	}

	/**
	 * Remove categories from this filter
	 *
	 * @param categories the categories you want ot remove
	 * @see #getCategories()
	 */
	public void removeCategories(OreCategory... categories) {
		this.categories.removeAll(Arrays.asList(categories));
	}

	/**
	 * Every project has to be assigned to a singular category, that best fits
	 * the project. This set of categories filters using "any match". This means
	 * you can get project from different categories, for example if the best
	 * category is ambiguous or you generally want projects from a reduced set of
	 * categories.<br>
	 * This method gives access to the backing set, allowing you to manipulate
	 * the filter in a convenient way.
	 *
	 * @return a mutable set of categories to filter for
	 */
	public Set<OreCategory> getCategories() {
		return categories;
	}

	/**
	 * Add a required platform to the filter without version requirement.<br>
	 * Supported platforms are: spongeapi, spongeforge, spongevanilla, sponge, lantern, forge.<br>
	 * Platform names are not enforced, but your result my end up empty if you use different names.
	 *
	 * @param name the name of the platform to limit the search to. Has to match \w+
	 */
	public void addPlatform(String name) {
		if (name == null || !name.matches("^\\w+$"))
			throw new IllegalArgumentException("Name does not match ^\\w+$");
		platforms.add(name);
	}

	/**
	 * Add a required platform to the filter including a version requirement.<br>
	 * Supported platforms are: spongeapi, spongeforge, spongevanilla, sponge, lantern, forge.<br>
	 * Platform names are not enforced, but your result my end up empty if you use different names.
	 *
	 * @param name    the name of the platform to limit the search to. Has to match \w+
	 * @param version the platform version. i think display version is ok as well. Has to match [\w.]+
	 */
	public void addPlatform(String name, String version) {
		if (name == null || !name.matches("^\\w+$"))
			throw new IllegalArgumentException("Name does not match ^\\w+$");
		String fused = name;
		if (version != null) {
			if (!name.matches("^[\\w.]+$"))
				throw new IllegalArgumentException("Version does not match ^[\\w.]+$");
			fused += ":" + version;
		}
		platforms.add(fused);
	}

	/**
	 * Clear any platform requirements for this search filter.
	 */
	public void clearPlatforms() {
		platforms.clear();
	}

	/**
	 * Remove the named platform form this search filter.
	 * Will ignore, if the platform was not part of the filter.
	 *
	 * @param name the platform to remove. Has to match \w+
	 */
	public void removePlatform(String name) {
		if (name == null || !name.matches("^\\w+$"))
			throw new IllegalArgumentException("Name does not match ^\\w+$");
		platforms.removeIf(x -> x.matches("^" + name + "(?::$)"));
	}

	/**
	 * @return the set of platforms to filter for, without versions
	 */
	public Set<String> getPlatforms() {
		return platforms.stream().map(x -> {
			int i = x.indexOf(':');
			return (i < 0) ? x : x.substring(0, i);
		}).collect(Collectors.toSet());
	}

	/**
	 * Filters the currently set platform versions for the provided name and returns a set formatted
	 * like name:version. If no version is specified the entry equals the name and matches any version.
	 *
	 * @param name Has to match \w+
	 * @return all platform entries with the given name.
	 */
	public Set<String> getPlatformVersions(String name) {
		if (name == null || !name.matches("^\\w+$"))
			throw new IllegalArgumentException("Name does not match ^\\w+$");
		return platforms.stream().filter(x -> x.matches("^" + name + "(?::$)")).collect(Collectors.toSet());
	}

	/**
	 * Returns the set of all platforms and versions currently filtered form, formatted
	 * like name:version. If no version is specified the entry equals the name and matches any version.<br>
	 * Due to formatting restrictions the return value is a copy of the internal list.
	 *
	 * @return the listing of all platforms and versions filtered for.
	 */
	public Set<String> getAllPlatformVersions() {
		return new HashSet<>(platforms);
	}

	/**
	 * Clear the current filter for allowed stabilities.<br>
	 * Note that due to this being a required filter,
	 * the result might fall back to recommended &amp; stable by the backend if empty.
	 */
	public void clearStabilities() {
		stabilities.clear();
	}

	/**
	 * Add projects to the result that have versions with the specified stabilities
	 *
	 * @param stabilities the stabilities to look out for
	 */
	public void addStabilities(OreStability... stabilities) {
		this.stabilities.addAll(Arrays.asList(stabilities));
	}

	/**
	 * If a project has no more versions with specified stabilities,
	 * it will not be returned anymore.<br>
	 * Note that due to this being a required filter, the result might fall back to recommended &amp;
	 * stable by the backend if empty.
	 *
	 * @param stabilities the stabilities to ignore
	 */
	public void removeStabilities(OreStability... stabilities) {
		this.stabilities.removeAll(Arrays.asList(stabilities));
	}

	/**
	 * Note that due to this being a required filter, the result might fall back to recommended &amp;
	 * stable by the backend if empty.
	 *
	 * @return a mutable set of current stabilities
	 */
	public Set<OreStability> getStabilities() {
		return stabilities;
	}

	/**
	 * Only return projects owned by the specified user or organization.
	 * Set this value to null to clear the filter.
	 *
	 * @param owner the project owner to match in this search
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return the project owner currently filtered for, or null
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * You can always sort by relevance in addition to your primary sorting.
	 *
	 * @param sortByRelevance true if you want to additionally sort by relevance
	 */
	public void doSortByRelevance(boolean sortByRelevance) {
		this.sortByRelevance = sortByRelevance;
	}

	/**
	 * @return true if currently sorting by relevance in addition to the specified order
	 */
	public boolean isSortingByRelevance() {
		return sortByRelevance;
	}

	/**
	 * Requires the search query to match exactly.
	 *
	 * @param exact true if you want exact seach
	 */
	public void doExactSearch(boolean exact) {
		this.exact = exact;
	}

	/**
	 * @return true if search is exact
	 */
	public boolean isSearchingExact() {
		return exact;
	}

	@Override
	public String toString() {
		List<String> elements = new LinkedList<>();
		if (query != null) {
			elements.add("q=" + query);
		}
		if (!categories.isEmpty()) {
			elements.addAll(categories.stream()
					.map(x -> "categories=" + urlEncoded(x.name().toLowerCase(Locale.ROOT)))
					.collect(Collectors.toList()));
		}
		if (!platforms.isEmpty()) {
			elements.addAll(platforms.stream()
					.map(x -> "platforms=" + urlEncoded(x.toLowerCase(Locale.ROOT)))
					.collect(Collectors.toList()));
		}
		if (!stabilities.isEmpty()) {
			elements.addAll(stabilities.stream()
					.map(x -> "stability=" + urlEncoded(x.name().toLowerCase(Locale.ROOT)))
					.collect(Collectors.toList()));
		}
		if (owner != null) {
			elements.add("owner=" + urlEncoded(owner));
		}
		elements.add("relevance=" + urlEncoded(sortByRelevance ? "true" : "false"));
		elements.add("exact=" + urlEncoded(exact ? "true" : "false"));
		elements.add(super.toString());
		return String.join("&", elements);
	}

	@Override
	public OreProjectFilter clone() {
		OreProjectFilter clone = new OreProjectFilter();
		clone.query = query;
		clone.categories.addAll(categories);
		clone.platforms.addAll(platforms);
		clone.stabilities.addAll(stabilities);
		clone.owner = owner;
		clone.sort = sort;
		clone.sortByRelevance = sortByRelevance;
		clone.exact = exact;
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
