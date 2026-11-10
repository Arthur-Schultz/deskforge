package com.deskforge.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ProductSpec {

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String value;

    protected ProductSpec() {
    }

    public ProductSpec(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }
}
