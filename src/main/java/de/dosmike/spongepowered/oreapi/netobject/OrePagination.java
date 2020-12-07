package de.dosmike.spongepowered.oreapi.netobject;

import com.google.gson.JsonObject;
import de.dosmike.spongepowered.oreapi.utility.FromJson;
import de.dosmike.spongepowered.oreapi.utility.JsonUtil;

import java.io.Serializable;

public class OrePagination<F extends OrePaginationFilter> implements Serializable {

    @FromJson("limit")
    int limit;
    @FromJson("offset")
    int offset;
    /** num results total, if offset+limit &gt;= count you're on the last page */
    @FromJson("count")
    int count;

    /** This is the filter that was used to obtain the result holding this pagination */
    F filter;

    public OrePagination(JsonObject object, F filterBase) {
        this.filter = filterBase;
        JsonUtil.fillSelf(this, object);
    }

    /** generate generic query parameters for the specified page without initial
     * concatinator
     * @return modified filter */
    public F getQueryPage(int page) {
        if (page < 1) throw new IllegalArgumentException("Page has to be positive integer");
        F newFilter = (F) filter.clone();
        newFilter.setPage(page);
        return newFilter;
    }
    /** calculate current page based on offset
     * @return current page number */
    public int getPage() {
        return (offset/limit)+1;
    }
    public int getLastPage() {
        return Math.max((int)Math.ceil((double)count/limit),1);
    }
    /** create the query for the next page (with max getLastPage()), see {@link #getQueryPage} for more info
     * @return modified filter */
    public F getQueryNext() {
        int page = getPage();
        int lastPage = getLastPage();
        F newFilter = (F)filter.clone();
        newFilter.setPage(page>=lastPage ? lastPage : getPage()+1);
        return newFilter;
    }
    /** create the query for the previous page (with min page 1),
     * see {@link #getQueryPage} for more info
     * @return modified filter */
    public F getQueryPrevious() {
        int page = getPage();
        F newFilter = (F)filter.clone();
        newFilter.setPage(page <= 2 ? 1 : page-1);
        return newFilter;
    }

    /** @return the actual amount of total results*/
    public int getResultCount() {
        return count;
    }

    /** @return the result limit per page */
    public int getPageLimit() {
        return limit;
    }
    /** @return the result offset of entries for this page */
    public int getPageOffset() {
        return offset;
    }

    public boolean hasMorePages() {
        return getPage() < getLastPage();
    }
}
