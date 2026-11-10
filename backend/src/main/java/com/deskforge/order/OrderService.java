package com.deskforge.order;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.deskforge.catalog.Product;
import com.deskforge.catalog.ProductRepository;
import com.deskforge.order.dto.AdminOrderSummaryResponse;
import com.deskforge.order.dto.CheckoutRequest;
import com.deskforge.order.dto.OrderResponse;
import com.deskforge.order.dto.PaymentResponse;
import com.deskforge.payment.PaymentGateway;
import com.deskforge.payment.PaymentResult;
import com.deskforge.shared.exception.BusinessRuleException;
import com.deskforge.shared.exception.NotFoundException;
import com.deskforge.shared.web.PageResponse;
import com.deskforge.store.PricingPolicy;
import com.deskforge.store.StoreProperties;
import com.deskforge.user.User;
import com.deskforge.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private static final String DEFAULT_CANCEL_REASON = "Cancelado pelo cliente (direito de arrependimento)";
    private static final String EXPIRED_REASON = "Pagamento não confirmado dentro do prazo";

    private final OrderRepository orders;
    private final ProductRepository products;
    private final UserRepository users;
    private final PaymentGateway paymentGateway;
    private final PricingPolicy pricing;
    private final StoreProperties store;
    private final Clock clock;

    public OrderService(OrderRepository orders, ProductRepository products, UserRepository users,
                        PaymentGateway paymentGateway, PricingPolicy pricing, StoreProperties store, Clock clock) {
        this.orders = orders;
        this.products = products;
        this.users = users;
        this.paymentGateway = paymentGateway;
        this.pricing = pricing;
        this.store = store;
        this.clock = clock;
    }

    @Transactional
    public OrderResponse checkout(UUID customerId, CheckoutRequest request) {
        User customer = users.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));
        Map<Long, Integer> quantities = request.items().stream()
                .collect(Collectors.toMap(CheckoutRequest.Item::productId, CheckoutRequest.Item::quantity,
                        Integer::sum, LinkedHashMap::new));
        Map<Long, Product> locked = products.findAllByIdForUpdate(quantities.keySet()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        if (locked.size() != quantities.size()) {
            throw new NotFoundException("Um ou mais produtos do carrinho não existem mais");
        }

        Instant now = clock.instant();
        CheckoutRequest.Address a = request.shippingAddress();
        ShippingAddress address = new ShippingAddress(a.recipientName().strip(), normalizeZip(a.zipCode()),
                a.street().strip(), a.number().strip(), blankToNull(a.complement()), a.district().strip(),
                a.city().strip(), a.state());
        Order order = Order.place("DF-" + orders.nextNumber(), customer, address, now,
                Duration.ofMinutes(store.paymentWindowMinutes()));
        quantities.forEach((productId, quantity) -> order.addItem(locked.get(productId), quantity));

        BigDecimal shipping = pricing.shippingFor(order.getSubtotal());
        order.applyCharges(shipping, pricing.approximateTaxesFor(order.getSubtotal().add(shipping)));
        return toResponse(orders.save(order), now);
    }

    @Transactional
    public PaymentResponse pay(UUID customerId, UUID orderId) {
        Order order = ownedOrder(customerId, orderId);
        Instant now = clock.instant();
        if (order.isPaymentExpired(now)) {
            throw new BusinessRuleException("O prazo para pagamento deste pedido expirou");
        }
        PaymentResult result = paymentGateway.charge(order);
        if (result.approved()) {
            order.markPaid(result.reference(), now);
        } else {
            order.attachPaymentSession(result.reference(), result.expiresAt());
        }
        return new PaymentResponse(toResponse(order, now), result.redirectUrl());
    }

    @Transactional
    public void confirmExternalPayment(UUID orderId, String paymentReference) {
        Order order = orderWithItems(orderId);
        Instant now = clock.instant();
        if (order.confirmExternalPayment(paymentReference, now)) {
            log.info("Payment confirmed for order {}", order.getNumber());
        } else if (order.getStatus() == OrderStatus.CANCELED) {
            log.warn("Payment received for canceled order {}, refunding", order.getNumber());
            paymentGateway.refund(paymentReference, order.getNumber());
        }
    }

    @Transactional
    public void expireExternalPayment(UUID orderId) {
        Order order = orderWithItems(orderId);
        if (order.getStatus() == OrderStatus.AWAITING_PAYMENT) {
            cancel(order, EXPIRED_REASON, clock.instant(), false);
        }
    }

    @Transactional
    public OrderResponse cancelByCustomer(UUID customerId, UUID orderId, String reason) {
        Order order = ownedOrder(customerId, orderId);
        Instant now = clock.instant();
        if (!order.canBeCanceledByCustomer(now, store.withdrawalDays())) {
            throw new BusinessRuleException("O prazo de " + store.withdrawalDays()
                    + " dias para desistência deste pedido terminou. Fale com o SAC para trocas e garantia.");
        }
        cancel(order, reason == null || reason.isBlank() ? DEFAULT_CANCEL_REASON : reason.strip(), now, true);
        return toResponse(order, now);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listForCustomer(UUID customerId) {
        Instant now = clock.instant();
        return orders.findAllByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(order -> toResponse(order, now))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findForCustomer(UUID customerId, UUID orderId) {
        return toResponse(ownedOrder(customerId, orderId), clock.instant());
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminOrderSummaryResponse> listForAdmin(OrderStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = status == null ? orders.findAll(pageable) : orders.findAllByStatus(status, pageable);
        return PageResponse.of(result, AdminOrderSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse findForAdmin(UUID orderId) {
        return toResponse(orderWithItems(orderId), clock.instant());
    }

    @Transactional
    public OrderResponse updateStatus(UUID orderId, OrderStatus target) {
        Order order = orderWithItems(orderId);
        Instant now = clock.instant();
        if (target == OrderStatus.CANCELED) {
            cancel(order, "Cancelado pela loja", now, true);
        } else {
            order.advanceTo(target, now);
        }
        return toResponse(order, now);
    }

    @Transactional
    public int expireUnpaidOrders() {
        Instant now = clock.instant();
        List<UUID> expired = orders.findExpiredAwaitingPayment(now);
        for (UUID id : expired) {
            Order order = orderWithItems(id);
            if (order.isPaymentExpired(now)) {
                cancel(order, EXPIRED_REASON, now, false);
            }
        }
        return expired.size();
    }

    private void cancel(Order order, String reason, Instant now, boolean closePendingPayment) {
        if (closePendingPayment && order.getStatus() == OrderStatus.AWAITING_PAYMENT) {
            paymentGateway.cancelPending(order);
        }
        List<Long> productIds = order.getItems().stream().map(item -> item.getProduct().getId()).toList();
        products.findAllByIdForUpdate(productIds);
        boolean requiresRefund = order.cancel(reason, now);
        if (requiresRefund) {
            paymentGateway.refund(order.getPaymentReference(), order.getNumber());
        }
    }

    private Order ownedOrder(UUID customerId, UUID orderId) {
        Order order = orderWithItems(orderId);
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new NotFoundException("Pedido não encontrado");
        }
        return order;
    }

    private Order orderWithItems(UUID orderId) {
        return orders.findWithItemsById(orderId).orElseThrow(() -> new NotFoundException("Pedido não encontrado"));
    }

    private OrderResponse toResponse(Order order, Instant now) {
        return OrderResponse.from(order, now, store.withdrawalDays());
    }

    private static String normalizeZip(String zip) {
        String digits = zip.replaceAll("\\D", "");
        return digits.substring(0, 5) + "-" + digits.substring(5);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
