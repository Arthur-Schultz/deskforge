package com.deskforge.admin.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.deskforge.catalog.dto.ProductSummaryResponse;
import com.deskforge.order.OrderStatus;

public record DashboardResponse(BigDecimal revenue, long customers, long openPrivacyRequests,
                                Map<OrderStatus, Long> ordersByStatus, List<ProductSummaryResponse> lowStock) {
}
