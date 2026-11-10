package com.deskforge.catalog;

import java.time.Clock;
import java.util.List;

import com.deskforge.catalog.dto.CategoryResponse;
import com.deskforge.catalog.dto.ProductDetailResponse;
import com.deskforge.catalog.dto.ProductRequest;
import com.deskforge.catalog.dto.ProductSummaryResponse;
import com.deskforge.shared.exception.ConflictException;
import com.deskforge.shared.exception.NotFoundException;
import com.deskforge.shared.web.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {

    private static final int MAX_PAGE_SIZE = 48;

    private final ProductRepository products;
    private final CategoryRepository categories;
    private final Clock clock;

    public CatalogService(ProductRepository products, CategoryRepository categories, Clock clock) {
        this.products = products;
        this.categories = categories;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categories.findAllByOrderByPositionAsc().stream().map(CategoryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> search(ProductFilter filter, ProductSort sort, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE), sort.sort());
        return PageResponse.of(products.findAll(ProductSpecifications.matching(filter), pageable),
                ProductSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> featured() {
        return products.findTop8ByFeaturedTrueAndActiveTrueOrderByCreatedAtDesc().stream()
                .map(ProductSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse findBySlug(String slug) {
        Product product = products.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado"));
        List<ProductSummaryResponse> related = products
                .findTop4ByCategoryIdAndIdNotAndActiveTrue(product.getCategory().getId(), product.getId()).stream()
                .map(ProductSummaryResponse::from)
                .toList();
        return ProductDetailResponse.from(product, related);
    }

    @Transactional
    public ProductDetailResponse create(ProductRequest request) {
        if (products.existsBySku(request.sku())) {
            throw new ConflictException("Já existe um produto com este SKU");
        }
        if (products.existsBySlug(request.slug())) {
            throw new ConflictException("Já existe um produto com este slug");
        }
        Product product = Product.create(request.sku(), request.slug(), request.name(), request.shortDescription(),
                request.description(), request.price(), request.compareAtPrice(), request.stock(), request.art(),
                request.accentColor(), request.featured(), category(request.categorySlug()), specs(request),
                clock.instant());
        return ProductDetailResponse.from(products.save(product), List.of());
    }

    @Transactional
    public ProductDetailResponse update(Long id, ProductRequest request) {
        Product product = product(id);
        product.update(request.name(), request.shortDescription(), request.description(), request.price(),
                request.compareAtPrice(), request.art(), request.accentColor(), request.featured(),
                category(request.categorySlug()), specs(request));
        product.adjustStock(request.stock());
        return ProductDetailResponse.from(product, List.of());
    }

    @Transactional
    public ProductSummaryResponse updateStock(Long id, int stock) {
        Product product = product(id);
        product.adjustStock(stock);
        return ProductSummaryResponse.from(product);
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        product(id).setActive(active);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> listForAdmin(String search, int page, int size) {
        ProductFilter filter = new ProductFilter(null, search, null, null, false, false);
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 100), ProductSort.NAME.sort());
        return PageResponse.of(products.findAll(ProductSpecifications.matchingIncludingInactive(filter), pageable),
                ProductSummaryResponse::from);
    }

    private Product product(Long id) {
        return products.findById(id).orElseThrow(() -> new NotFoundException("Produto não encontrado"));
    }

    private Category category(String slug) {
        return categories.findBySlug(slug).orElseThrow(() -> new NotFoundException("Categoria não encontrada"));
    }

    private List<ProductSpec> specs(ProductRequest request) {
        if (request.specs() == null) {
            return List.of();
        }
        return request.specs().stream().map(s -> new ProductSpec(s.label(), s.value())).toList();
    }
}
