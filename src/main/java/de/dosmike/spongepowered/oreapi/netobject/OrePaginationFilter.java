package de.dosmike.spongepowered.oreapi.netobject;

import java.io.Serializable;

/**
 * Add this to a request that lists multiple results for pagination.
 * It encapsulates the request parameters limit and offset.
 */
public class OrePaginationFilter implements Serializable, Cloneable {

	int limit = 25;
	int offset = 0;

	public OrePaginationFilter() {
	}

	public OrePaginationFilter(int limit, int offset) {
		this.limit = limit > 0 ? limit : 25;
		this.offset = offset >= 0 ? offset : 0;
	}

	/**
	 * @return the parameter string for requests
	 */
	@Override
	public String toString() {
		return "limit=" + limit + "&offset=" + offset;
	}

	/**
	 * @return a copy of this filter
	 */
	@Override
	public OrePaginationFilter clone() {
		return new OrePaginationFilter(limit, offset);
	}

	/**
	 * Set this page using natural counting as offset.
	 * As the pagination filter has no knowledge of total possible results, there's no upper limit enforced here.
	 *
	 * @param page the page to jump to
	 * @throws IllegalArgumentException if the page value is zero or negative
	 */
	public void setPage(int page) {
		if (page < 1) throw new IllegalArgumentException("Page has to be positive integer");
		int offset = (page - 1) * limit;
	}

	/**
	 * Breaks down the current offset and limit into a natural counted page number.
	 *
	 * @return the page of results this filter will return.
	 */
	public int getPage() {
		return (offset / limit) + 1;
	}

	/**
	 * Convenience method for setPage(getPage()+1), that increments the offset by limit once
	 */
	public void incrementPage() {
		offset += limit;
	}

	/**
	 * Convenience method for setPage(getPage()-1), that decrements the offset by limit once.
	 * This method does not throw, but simply does not decrement below page 1.
	 */
	public void decrementPage() {
		offset = Math.max(0, offset - limit);
	}

	/**
	 * @return the maximum number of results to return per page
	 */
	public int getLimit() {
		return limit;
	}

	/**
	 * @return the offset into the result set as the first element to return
	 */
	public int getOffset() {
		return offset;
	}
}
