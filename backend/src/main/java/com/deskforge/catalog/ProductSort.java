package com.deskforge.catalog;

import org.springframework.data.domain.Sort;

public enum ProductSort {
    FEATURED(Sort.by(Sort.Order.desc("featured"), Sort.Order.asc("name"))),
    PRICE_ASC(Sort.by(Sort.Order.asc("price"))),
    PRICE_DESC(Sort.by(Sort.Order.desc("price"))),
    NEWEST(Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))),
    NAME(Sort.by(Sort.Order.asc("name")));

    private final Sort sort;

    ProductSort(Sort sort) {
        this.sort = sort;
    }

    public Sort sort() {
        return sort;
    }
}
