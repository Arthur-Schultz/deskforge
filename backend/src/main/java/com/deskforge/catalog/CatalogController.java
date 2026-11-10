package com.deskforge.catalog;

import java.math.BigDecimal;
import java.util.List;

import com.deskforge.catalog.dto.CategoryResponse;
import com.deskforge.catalog.dto.ProductDetailResponse;
import com.deskforge.catalog.dto.ProductSummaryResponse;
import com.deskforge.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Catálogo")
public class CatalogController {

    private final CatalogService catalog;

    public CatalogController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/api/categories")
    @Operation(summary = "Lista as categorias da loja")
    public List<CategoryResponse> categories() {
        return catalog.listCategories();
    }

    @GetMapping("/api/products")
    @Operation(summary = "Busca produtos com filtros, ordenação e paginação")
    public PageResponse<ProductSummaryResponse> search(@RequestParam(required = false) String category,
                                                       @RequestParam(required = false) String q,
                                                       @RequestParam(required = false) BigDecimal minPrice,
                                                       @RequestParam(required = false) BigDecimal maxPrice,
                                                       @RequestParam(defaultValue = "false") boolean inStock,
                                                       @RequestParam(defaultValue = "false") boolean onSale,
                                                       @RequestParam(defaultValue = "FEATURED") ProductSort sort,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "12") int size) {
        ProductFilter filter = new ProductFilter(category, q, minPrice, maxPrice, inStock, onSale);
        return catalog.search(filter, sort, page, size);
    }

    @GetMapping("/api/products/featured")
    @Operation(summary = "Produtos em destaque na vitrine")
    public List<ProductSummaryResponse> featured() {
        return catalog.featured();
    }

    @GetMapping("/api/products/{slug}")
    @Operation(summary = "Detalhes de um produto, especificações e relacionados")
    public ProductDetailResponse product(@PathVariable String slug) {
        return catalog.findBySlug(slug);
    }
}
