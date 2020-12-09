package de.dosmike.spongepowered.oreapi.netobject;

import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

public class OreProjectFilter extends OrePaginationFilter {

	public enum Sort {
		Stars, Downloads, Views, Newest,
		/**
		 * this is the default
		 */
		Updated, Only_Relevance, Recent_Downloads, Recent_Views
	}

	String query = null;
	Set<OreCategory> categories = new HashSet<>();
	/**
	 * Value nullable!
	 */
	Map<String, String> platforms = new HashMap<>();
	Set<OreStability> stabilities = new HashSet<>();
	String owner = null;
	Sort sort = Sort.Updated;
	boolean sortByRelevance = true;
	boolean exact = false;

	/**
	 * convenience constructor for default pagination with only a search query set.
	 *
	 * @param query the string to search for
	 */
	public OreProjectFilter(String query) {
		this.query = query;
	}

	public OreProjectFilter() {

	}

	/**
	 * Set to null to clear
	 *
	 * @param searchQuery the string to search for
	 */
	public void setQuery(String searchQuery) {
		this.query = searchQuery;
	}

	public Optional<String> getQuery() {
		return Optional.ofNullable(query);
	}

	public void clearCategories() {
		categories.clear();
	}

	public void addCategories(OreCategory... categories) {
		this.categories.addAll(Arrays.asList(categories));
	}

	public void removeCategories(OreCategory... categories) {
		this.categories.removeAll(Arrays.asList(categories));
	}

	public Set<OreCategory> getCategories() {
		return categories;
	}

	public void addPlatform(String name) {
		platforms.put(name, null);
	}

	public void addPlatform(String name, String version) {
		platforms.put(name, version);
	}

	public void clearPlatforms() {
		platforms.clear();
	}

	public void removePlatform(String name) {
		platforms.remove(name);
	}

	public Set<String> getPlatforms() {
		return platforms.keySet();
	}

	public Optional<String> getPlatformVersion(String name) {
		return Optional.ofNullable(platforms.get(name));
	}

	public Map<String, String> getAllPlatformVersions() {
		return platforms;
	}

	public void clearStailities() {
		stabilities.clear();
	}

	public void addStabilities(OreStability... stabilities) {
		this.stabilities.addAll(Arrays.asList(stabilities));
	}

	public void removeStabilities(OreStability... stabilities) {
		this.stabilities.removeAll(Arrays.asList(stabilities));
	}

	public Set<OreStability> getStabilities() {
		return stabilities;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getOwner() {
		return owner;
	}

	public void setSorting(Sort sorting) {
		this.sort = sorting;
	}

	public Sort getSorting() {
		return sort;
	}

	public void doSortByRelevance(boolean sortByRelevance) {
		this.sortByRelevance = sortByRelevance;
	}

	public boolean isSortingByRelevance() {
		return sortByRelevance;
	}

	public void doExactSearch(boolean exact) {
		this.exact = exact;
	}

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
			elements.addAll(platforms.entrySet().stream()
					.map(e -> e.getValue() != null ? e.getKey() + ":" + e.getValue() : e.getKey())
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
		elements.add("sort=" + urlEncoded(sort.name().toLowerCase(Locale.ROOT)));
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
		clone.platforms.putAll(platforms);
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
