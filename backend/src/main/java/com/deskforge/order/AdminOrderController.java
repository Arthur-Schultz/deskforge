package com.deskforge.order;

import java.util.UUID;

import com.deskforge.order.dto.AdminOrderSummaryResponse;
import com.deskforge.order.dto.OrderResponse;
import com.deskforge.order.dto.OrderStatusUpdateRequest;
import com.deskforge.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@Tag(name = "Admin • Pedidos")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "Lista pedidos com filtro opcional por status")
    public PageResponse<AdminOrderSummaryResponse> list(@RequestParam(required = false) OrderStatus status,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        return orderService.listForAdmin(status, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhes de qualquer pedido")
    public OrderResponse one(@PathVariable UUID id) {
        return orderService.findForAdmin(id);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Avança o status do pedido respeitando a máquina de estados")
    public OrderResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody OrderStatusUpdateRequest request) {
        return orderService.updateStatus(id, request.status());
    }
}
