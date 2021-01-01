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

	/**
	 * @return the name to query for, or null if not set
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * @return the amount of projects a user needs, to be included in the search result
	 */
	public int getMinProjects() {
		return minProjects;
	}

	/**
	 * @param minProjects the amount of projects a user needs, to be included in the search result
	 */
	public void setMinProjects(int minProjects) {
		if (minProjects < 0)
			throw new IllegalArgumentException("Minimum Project count cannot be negative");
		this.minProjects = minProjects;
	}

	/**
	 * Clear any role requirement for users in this search
	 */
	public void clearRoles() {
		this.roles.clear();
	}

	/**
	 * Add role requirements to this search. Only users with these roles will be returned
	 *
	 * @param roles roles to add
	 */
	public void addRoles(OreRole... roles) {
		this.roles.addAll(Arrays.asList(roles));
	}

	/**
	 * Remove role requirements from this search. Only users with these roles will be returned
	 *
	 * @param roles roles to remove
	 */
	public void removeRoles(OreRole... roles) {
		this.roles.removeAll(Arrays.asList(roles));
	}

	/**
	 * Get a mutable set of roles required on users. Only users with these roles will be returned.
	 *
	 * @return the set of roles
	 */
	public Set<OreRole> getRoles() {
		return roles;
	}

	/**
	 * @return true if organizations are allowed to be returned as users with your search
	 */
	public boolean isExcludingOrganizations() {
		return excludeOrganizations;
	}

	/**
	 * Since organizations work like users in many ways, you can search users with this as well.
	 * If you don't want organizations in your result set, you can set this to true.
	 *
	 * @param excludeOrganizations true if you only want real users
	 */
	public void doExcludeOrganizations(boolean excludeOrganizations) {
		this.excludeOrganizations = excludeOrganizations;
	}

	/**
	 * @return the current sort order for your results
	 */
	public Sort getSort() {
		return sort;
	}

	/**
	 * @param sort set the order in which to return your results
	 */
	public void setSort(Sort sort) {
		this.sort = sort;
	}

	/**
	 * @return true if the result will be sorted descending
	 */
	public boolean isSortingDescending() {
		return sortDescending;
	}

	/**
	 * @param sortDescending true if you want the results to be sorted descending
	 */
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
