package com.deskforge.notification;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.deskforge.order.Order;
import com.deskforge.order.OrderStatusChanged;
import com.deskforge.order.ShippingAddress;
import com.deskforge.user.User;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEmailNotifier {

    private final EmailOutbox outbox;
    private final EmailRenderer renderer;
    private final EmailProperties properties;

    public OrderEmailNotifier(EmailOutbox outbox, EmailRenderer renderer, EmailProperties properties) {
        this.outbox = outbox;
        this.renderer = renderer;
        this.properties = properties;
    }

    @EventListener
    public void on(OrderStatusChanged event) {
        Order order = event.order();
        User customer = order.getCustomer();
        if (customer.isAnonymized()) {
            return;
        }
        switch (order.getStatus()) {
            case PAID -> send(order, EmailTemplate.PAYMENT_CONFIRMED,
                    "Pagamento confirmado: pedido " + order.getNumber(), Map.of());
            case SHIPPED -> send(order, EmailTemplate.ORDER_SHIPPED,
                    "Seu pedido " + order.getNumber() + " foi enviado", Map.of());
            case CANCELED -> send(order, EmailTemplate.ORDER_CANCELED,
                    "Pedido " + order.getNumber() + " cancelado",
                    Map.of("refunded", event.previousStatus() != null && event.previousStatus().isPaid()));
            case AWAITING_PAYMENT, DELIVERED -> {
            }
        }
    }

    private void send(Order order, EmailTemplate template, String subject, Map<String, Object> extra) {
        Map<String, Object> variables = new HashMap<>(orderVariables(order));
        variables.putAll(extra);
        String dedupKey = "order:" + order.getId() + ":" + order.getStatus();
        outbox.enqueue(dedupKey, template, order.getCustomer().getEmail(), subject, variables);
    }

    private Map<String, Object> orderVariables(Order order) {
        List<Line> lines = order.getItems().stream()
                .map(item -> new Line(item.getProductName(), item.getQuantity(), EmailFormat.money(item.getLineTotal())))
                .toList();
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", order.getCustomer().getFirstName());
        variables.put("orderNumber", order.getNumber());
        variables.put("orderUrl", renderer.link("/conta/pedidos/" + order.getId()));
        variables.put("lines", lines);
        variables.put("subtotal", EmailFormat.money(order.getSubtotal()));
        variables.put("shipping", order.getShippingCost().compareTo(BigDecimal.ZERO) == 0
                ? "Grátis" : EmailFormat.money(order.getShippingCost()));
        variables.put("total", EmailFormat.money(order.getTotal()));
        variables.put("address", addressLines(order.getShippingAddress()));
        variables.put("paidAt", EmailFormat.dateTime(order.getPaidAt(), properties.zone()));
        variables.put("cancelReason", order.getCancelReason());
        return variables;
    }

    private static List<String> addressLines(ShippingAddress address) {
        String street = address.getStreet() + ", " + address.getNumber()
                + (address.getComplement() == null ? "" : " - " + address.getComplement());
        return List.of(address.getRecipientName(), street,
                address.getDistrict() + " - " + address.getCity() + "/" + address.getState(),
                "CEP " + address.getZipCode());
    }

    public record Line(String name, int quantity, String total) {
    }
}
