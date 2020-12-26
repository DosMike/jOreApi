package de.dosmike.spongepowered.oreapi.netobject;

import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

public class OreUserFilter extends OrePaginationFilter {

	public enum Sort {
		Name, Join_Date, Project_Count;
	}

	String query = null;
	int minProjects = 0;
	Set<OreRole> roles = new HashSet<>();
	boolean excludeOrganizations = false;
	Sort sort = Sort.Project_Count;
	boolean sortDescending = false;

	/**
	 * convenience constructor for default pagination with only a search query set.
	 *
	 * @param query the string to search for
	 */
	public OreUserFilter(String query) {
		this.query = query;
	}

	public OreUserFilter() {

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

	public int getMinProjects() {
		return minProjects;
	}

	public void setMinProjects(int minProjects) {
		if (minProjects < 0)
			throw new IllegalArgumentException("Minimum Project count cannot be negative");
		this.minProjects = minProjects;
	}

	public void clearRoles() {
		this.roles.clear();
	}

	public void addRoles(OreRole... roles) {
		this.roles.addAll(Arrays.asList(roles));
	}

	public void removeRoles(OreRole... roles) {
		this.roles.removeAll(Arrays.asList(roles));
	}

	public Set<OreRole> getRoles() {
		return roles;
	}

	public boolean isExcludingOrganizations() {
		return excludeOrganizations;
	}

	public void doExcludeOrganizations(boolean excludeOrganizations) {
		this.excludeOrganizations = excludeOrganizations;
	}

	public Sort getSort() {
		return sort;
	}

	public void setSort(Sort sort) {
		this.sort = sort;
	}

	public boolean isSortingDescending() {
		return sortDescending;
	}

	public void doSortDescending(boolean sortDescending) {
		this.sortDescending = sortDescending;
	}

	@Override
	public String toString() {
		List<String> elements = new LinkedList<>();
		if (query != null) {
			elements.add("q=" + query);
		}
		if (minProjects > 0) {
			elements.add("minProjects=" + minProjects);
		}
		if (!roles.isEmpty()) {
			elements.addAll(roles.stream()
					.map(x -> "roles=" + urlEncoded(x.name().toLowerCase(Locale.ROOT)))
					.collect(Collectors.toList()));
		}
		elements.add("excludeOrganizations=" + urlEncoded(excludeOrganizations ? "true" : "false"));
		elements.add("sort=" + urlEncoded(sort.name().toLowerCase(Locale.ROOT)));
		elements.add("sortDescending=" + urlEncoded(sortDescending ? "true" : "false"));
		elements.add(super.toString());
		return String.join("&", elements);
	}

	@Override
	public OreUserFilter clone() {
		OreUserFilter clone = new OreUserFilter();
		clone.query = query;
		clone.minProjects = minProjects;
		clone.excludeOrganizations = excludeOrganizations;
		clone.sort = sort;
		clone.sortDescending = sortDescending;
		clone.roles = new HashSet<>(roles);
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
