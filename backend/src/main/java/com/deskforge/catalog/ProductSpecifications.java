package com.deskforge.catalog;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

final class ProductSpecifications {

    private ProductSpecifications() {
    }

    static Specification<Product> matching(ProductFilter filter) {
        Specification<Product> active = (root, query, cb) -> cb.isTrue(root.get("active"));
        return active.and(matchingIncludingInactive(filter));
    }

    static Specification<Product> matchingIncludingInactive(ProductFilter filter) {
        List<Specification<Product>> specs = new ArrayList<>();
        if (filter.category() != null && !filter.category().isBlank()) {
            specs.add((root, query, cb) -> cb.equal(root.get("category").get("slug"), filter.category()));
        }
        if (filter.search() != null && !filter.search().isBlank()) {
            String term = "%" + filter.search().strip().toLowerCase() + "%";
            specs.add((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), term),
                    cb.like(cb.lower(root.get("shortDescription")), term)));
        }
        if (filter.minPrice() != null) {
            BigDecimal min = filter.minPrice();
            specs.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), min));
        }
        if (filter.maxPrice() != null) {
            BigDecimal max = filter.maxPrice();
            specs.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), max));
        }
        if (filter.inStockOnly()) {
            specs.add((root, query, cb) -> cb.greaterThan(root.get("stock"), 0));
        }
        if (filter.onSaleOnly()) {
            specs.add((root, query, cb) -> cb.isNotNull(root.get("compareAtPrice")));
        }
        return Specification.allOf(specs);
    }
}
