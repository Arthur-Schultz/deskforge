package com.deskforge.catalog;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.deskforge.shared.exception.BusinessRuleException;
import com.deskforge.shared.exception.ConflictException;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String shortDescription;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private BigDecimal compareAtPrice;

    @Column(nullable = false)
    private int stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArtKind art;

    @Column(nullable = false)
    private String accentColor;

    @Column(nullable = false)
    private boolean featured;

    @Column(nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @ElementCollection
    @CollectionTable(name = "product_specs", joinColumns = @JoinColumn(name = "product_id"))
    @OrderColumn(name = "position")
    private List<ProductSpec> specs = new ArrayList<>();

    @Version
    private long version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Product() {
    }

    public static Product create(String sku, String slug, String name, String shortDescription, String description,
                                 BigDecimal price, BigDecimal compareAtPrice, int stock, ArtKind art,
                                 String accentColor, boolean featured, Category category, List<ProductSpec> specs,
                                 Instant now) {
        Product product = new Product();
        product.sku = sku;
        product.slug = slug;
        product.active = true;
        product.createdAt = now;
        product.update(name, shortDescription, description, price, compareAtPrice, art, accentColor, featured,
                category, specs);
        product.adjustStock(stock);
        return product;
    }

    public void update(String name, String shortDescription, String description, BigDecimal price,
                       BigDecimal compareAtPrice, ArtKind art, String accentColor, boolean featured,
                       Category category, List<ProductSpec> specs) {
        if (compareAtPrice != null && compareAtPrice.compareTo(price) <= 0) {
            throw new BusinessRuleException("O preço \"de\" deve ser maior que o preço de venda");
        }
        this.name = name;
        this.shortDescription = shortDescription;
        this.description = description;
        this.price = price;
        this.compareAtPrice = compareAtPrice;
        this.art = art;
        this.accentColor = accentColor;
        this.featured = featured;
        this.category = category;
        this.specs.clear();
        this.specs.addAll(specs);
    }

    public void adjustStock(int stock) {
        if (stock < 0) {
            throw new BusinessRuleException("O estoque não pode ser negativo");
        }
        this.stock = stock;
    }

    public void reserve(int quantity) {
        if (!active) {
            throw new BusinessRuleException("O produto " + name + " não está mais disponível");
        }
        if (quantity > stock) {
            throw new ConflictException("Estoque insuficiente para " + name + ". Disponível: " + stock);
        }
        this.stock -= quantity;
    }

    public void release(int quantity) {
        this.stock += quantity;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getCompareAtPrice() {
        return compareAtPrice;
    }

    public int getStock() {
        return stock;
    }

    public ArtKind getArt() {
        return art;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public boolean isFeatured() {
        return featured;
    }

    public boolean isActive() {
        return active;
    }

    public Category getCategory() {
        return category;
    }

    public List<ProductSpec> getSpecs() {
        return specs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
