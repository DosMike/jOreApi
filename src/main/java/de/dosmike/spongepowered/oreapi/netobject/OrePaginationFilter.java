package de.dosmike.spongepowered.oreapi.netobject;

import java.io.Serializable;

/**
 * Add this to a request that lists multiple results for pagination.
 * It encapsulates the request parameters limit and offset.
 */
public class OrePaginationFilter implements Serializable,Cloneable {

    int limit = 25;
    int offset = 0;

    public OrePaginationFilter() {}

    public OrePaginationFilter(int limit, int offset) {
        this.limit = limit>0 ? limit : 25;
        this.offset = offset>=0 ? offset : 0;
    }

    /** @return the parameter string for requests */
    @Override
    public String toString() {
        return "limit="+limit+"&offset="+offset;
    }

    /**
     * @return a copy of this filter
     */
    @Override
    public OrePaginationFilter clone() {
        return new OrePaginationFilter(limit, offset);
    }

    public void setPage(int page) {
        if (page < 1) throw new IllegalArgumentException("Page has to be positive integer");
        int offset = (page-1)*limit;
    }

    public int getPage() {
        return (offset/limit)+1;
    }

    public void incrementPage() {
        offset += limit;
    }

    public void decrementPage() {
        offset = Math.max(0, offset-limit);
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }
}
