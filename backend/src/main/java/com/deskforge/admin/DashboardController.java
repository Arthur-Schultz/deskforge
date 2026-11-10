package com.deskforge.admin;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import com.deskforge.admin.dto.DashboardResponse;
import com.deskforge.catalog.ProductRepository;
import com.deskforge.catalog.dto.ProductSummaryResponse;
import com.deskforge.order.OrderRepository;
import com.deskforge.order.OrderStatus;
import com.deskforge.privacy.DataSubjectRequestRepository;
import com.deskforge.user.Role;
import com.deskforge.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@Tag(name = "Admin • Painel")
public class DashboardController {

    private static final int LOW_STOCK_THRESHOLD = 10;

    private final OrderRepository orders;
    private final ProductRepository products;
    private final UserRepository users;
    private final DataSubjectRequestRepository privacyRequests;

    public DashboardController(OrderRepository orders, ProductRepository products, UserRepository users,
                               DataSubjectRequestRepository privacyRequests) {
        this.orders = orders;
        this.products = products;
        this.users = users;
        this.privacyRequests = privacyRequests;
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Indicadores de vendas, estoque e privacidade")
    public DashboardResponse dashboard() {
        Map<OrderStatus, Long> byStatus = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) {
            byStatus.put(status, orders.countByStatus(status));
        }
        return new DashboardResponse(
                orders.sumTotalByStatusIn(EnumSet.of(OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.DELIVERED)),
                users.countByRole(Role.CUSTOMER),
                privacyRequests.countByStatus("OPEN"),
                byStatus,
                products.findTop5ByActiveTrueAndStockLessThanEqualOrderByStockAsc(LOW_STOCK_THRESHOLD).stream()
                        .map(ProductSummaryResponse::from)
                        .toList());
    }
}
