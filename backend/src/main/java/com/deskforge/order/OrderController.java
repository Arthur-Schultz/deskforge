package com.deskforge.order;

import java.util.List;
import java.util.UUID;

import com.deskforge.auth.CurrentUser;
import com.deskforge.order.dto.CancelRequest;
import com.deskforge.order.dto.CheckoutRequest;
import com.deskforge.order.dto.OrderResponse;
import com.deskforge.order.dto.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Pedidos")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Fecha o pedido reservando o estoque com bloqueio pessimista")
    public OrderResponse checkout(@Valid @RequestBody CheckoutRequest request) {
        return orderService.checkout(CurrentUser.get().id(), request);
    }

    @GetMapping
    @Operation(summary = "Pedidos do cliente autenticado")
    public List<OrderResponse> mine() {
        return orderService.listForCustomer(CurrentUser.get().id());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhes de um pedido do cliente")
    public OrderResponse one(@PathVariable UUID id) {
        return orderService.findForCustomer(CurrentUser.get().id(), id);
    }

    @PostMapping("/{id}/payment")
    @Operation(summary = "Inicia o pagamento do pedido pelo gateway configurado")
    public PaymentResponse pay(@PathVariable UUID id) {
        return orderService.pay(CurrentUser.get().id(), id);
    }

    @PostMapping("/{id}/cancellation")
    @Operation(summary = "Cancela o pedido exercendo o direito de arrependimento (art. 49 do CDC)")
    public OrderResponse cancel(@PathVariable UUID id, @Valid @RequestBody(required = false) CancelRequest request) {
        return orderService.cancelByCustomer(CurrentUser.get().id(), id, request == null ? null : request.reason());
    }
}
