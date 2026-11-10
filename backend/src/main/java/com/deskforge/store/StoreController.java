package com.deskforge.store;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/store")
@Tag(name = "Loja", description = "Dados institucionais exigidos pelo Decreto 7.962/2013")
public class StoreController {

    private final StoreProperties properties;

    public StoreController(StoreProperties properties) {
        this.properties = properties;
    }

    @GetMapping
    @Operation(summary = "Informações do fornecedor, prazos e regras de frete")
    public StoreInfoResponse info() {
        return StoreInfoResponse.from(properties);
    }
}
