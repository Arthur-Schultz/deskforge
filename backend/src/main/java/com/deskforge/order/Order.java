package com.deskforge.order;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.deskforge.catalog.Product;
import com.deskforge.shared.exception.BusinessRuleException;
import com.deskforge.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String number;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(nullable = false)
    private BigDecimal shippingCost;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(nullable = false)
    private BigDecimal approximateTaxes;

    @Embedded
    private ShippingAddress shippingAddress;

    private String paymentReference;

    private String cancelReason;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant paidAt;

    private Instant shippedAt;

    private Instant deliveredAt;

    private Instant canceledAt;

    @Column(nullable = false)
    private Instant expiresAt;

    protected Order() {
    }

    public static Order place(String number, User customer, ShippingAddress address, Instant now,
                              Duration paymentWindow) {
        Order order = new Order();
        order.id = UUID.randomUUID();
        order.number = number;
        order.customer = customer;
        order.shippingAddress = address;
        order.status = OrderStatus.AWAITING_PAYMENT;
        order.createdAt = now;
        order.expiresAt = now.plus(paymentWindow);
        order.subtotal = BigDecimal.ZERO;
        order.shippingCost = BigDecimal.ZERO;
        order.total = BigDecimal.ZERO;
        order.approximateTaxes = BigDecimal.ZERO;
        return order;
    }

    public void addItem(Product product, int quantity) {
        product.reserve(quantity);
        OrderItem item = new OrderItem(this, product, quantity);
        items.add(item);
        subtotal = subtotal.add(item.getLineTotal());
    }

    public void applyCharges(BigDecimal shippingCost, BigDecimal approximateTaxes) {
        this.shippingCost = shippingCost;
        this.total = subtotal.add(shippingCost);
        this.approximateTaxes = approximateTaxes;
    }

    public void markPaid(String paymentReference, Instant now) {
        if (status != OrderStatus.AWAITING_PAYMENT) {
            throw new BusinessRuleException("Este pedido não está aguardando pagamento");
        }
        if (now.isAfter(expiresAt)) {
            throw new BusinessRuleException("O prazo para pagamento deste pedido expirou");
        }
        this.status = OrderStatus.PAID;
        this.paymentReference = paymentReference;
        this.paidAt = now;
    }

    public void advanceTo(OrderStatus target, Instant now) {
        if (target == OrderStatus.CANCELED || target == OrderStatus.PAID) {
            throw new BusinessRuleException("Use o fluxo específico de pagamento ou cancelamento");
        }
        if (!status.canTransitionTo(target)) {
            throw new BusinessRuleException("Não é possível mudar de " + status + " para " + target);
        }
        this.status = target;
        if (target == OrderStatus.SHIPPED) {
            this.shippedAt = now;
        } else if (target == OrderStatus.DELIVERED) {
            this.deliveredAt = now;
        }
    }

    public boolean canBeCanceledByCustomer(Instant now, int withdrawalDays) {
        if (status == OrderStatus.CANCELED) {
            return false;
        }
        if (status == OrderStatus.DELIVERED) {
            return !now.isAfter(withdrawalDeadline(withdrawalDays));
        }
        return true;
    }

    public Instant withdrawalDeadline(int withdrawalDays) {
        return deliveredAt == null ? null : deliveredAt.plus(Duration.ofDays(withdrawalDays));
    }

    public boolean cancel(String reason, Instant now) {
        if (status == OrderStatus.CANCELED) {
            throw new BusinessRuleException("Este pedido já foi cancelado");
        }
        boolean requiresRefund = status.isPaid();
        this.status = OrderStatus.CANCELED;
        this.cancelReason = reason;
        this.canceledAt = now;
        items.forEach(item -> item.getProduct().release(item.getQuantity()));
        return requiresRefund;
    }

    public boolean isPaymentExpired(Instant now) {
        return status == OrderStatus.AWAITING_PAYMENT && now.isAfter(expiresAt);
    }

    public UUID getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public User getCustomer() {
        return customer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getApproximateTaxes() {
        return approximateTaxes;
    }

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public Instant getShippedAt() {
        return shippedAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public Instant getCanceledAt() {
        return canceledAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
