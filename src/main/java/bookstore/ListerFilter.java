package org.dgkrajnik.bookstore;

import java.util.List;

import java.time.LocalDate;
import java.math.BigDecimal;

public class ListerFilter {
    protected String nameLike = null;
    protected LocalDate dateMin = null;
    protected LocalDate dateMax = null;
    protected BigDecimal priceMin = null;
    protected BigDecimal priceMax = null;
    protected String authorLike = null;
    protected List<String> anyTag = null;
    protected List<String> allTag = null;

    public ListerFilter() {};

    public void filterName(String nameLike) {
        this.nameLike = nameLike;
    }
    public void filterDateAfter(LocalDate dateMin) {
        this.dateMin = dateMin;
    }
    public void filterDateBefore(LocalDate dateMax) {
        this.dateMax = dateMax;
    }
    public void filterPriceAbove(BigDecimal priceMin) {
        this.priceMin = priceMin;
    }
    public void filterPriceBelow(BigDecimal priceMax) {
        this.priceMax = priceMax;
    }
    public void filterAuthor(String authorLike) {
        this.authorLike = authorLike;
    }
    public void filterMatchAnyTag(List<String> anyTag) {
        if (this.allTag != null && anyTag != null) {
            throw new IllegalArgumentException("Cannot have an any tag match while an all tag match is set.");
        }
        this.anyTag = anyTag;
    }
    public void filterMatchAllTags(List<String> allTag) {
        if (this.anyTag != null && allTag != null) {
            throw new IllegalArgumentException("Cannot have an all tag match while an any tag match is set.");
        }
        this.allTag = allTag;
    }
}
