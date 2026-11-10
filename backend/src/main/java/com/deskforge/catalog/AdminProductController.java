package com.deskforge.catalog;

import com.deskforge.catalog.dto.ProductDetailResponse;
import com.deskforge.catalog.dto.ProductRequest;
import com.deskforge.catalog.dto.ProductSummaryResponse;
import com.deskforge.catalog.dto.StockUpdateRequest;
import com.deskforge.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products")
@Tag(name = "Admin • Produtos")
public class AdminProductController {

    private final CatalogService catalog;

    public AdminProductController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    @Operation(summary = "Lista todos os produtos, inclusive inativos")
    public PageResponse<ProductSummaryResponse> list(@RequestParam(required = false) String q,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "50") int size) {
        return catalog.listForAdmin(q, page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um produto")
    public ProductDetailResponse create(@Valid @RequestBody ProductRequest request) {
        return catalog.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um produto")
    public ProductDetailResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return catalog.update(id, request);
    }

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Ajusta o estoque de um produto")
    public ProductSummaryResponse updateStock(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest request) {
        return catalog.updateStock(id, request.stock());
    }

    @PostMapping("/{id}/activation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Reativa um produto")
    public void activate(@PathVariable Long id) {
        catalog.setActive(id, true);
    }

    @DeleteMapping("/{id}/activation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desativa um produto sem apagar o histórico de pedidos")
    public void deactivate(@PathVariable Long id) {
        catalog.setActive(id, false);
    }
}
