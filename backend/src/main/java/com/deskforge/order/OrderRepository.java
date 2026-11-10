package com.deskforge.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query(value = "SELECT nextval('order_number_seq')", nativeQuery = true)
    long nextNumber();

    @EntityGraph(attributePaths = {"items"})
    List<Order> findAllByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    @EntityGraph(attributePaths = {"items", "customer"})
    Optional<Order> findWithItemsById(UUID id);

    @EntityGraph(attributePaths = {"customer"})
    Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"customer"})
    Page<Order> findAll(Pageable pageable);

    @Query("select o.id from Order o where o.status = com.deskforge.order.OrderStatus.AWAITING_PAYMENT and o.expiresAt < :now")
    List<UUID> findExpiredAwaitingPayment(@Param("now") Instant now);

    long countByStatus(OrderStatus status);

    @Query("select coalesce(sum(o.total), 0) from Order o where o.status in :statuses")
    BigDecimal sumTotalByStatusIn(@Param("statuses") Collection<OrderStatus> statuses);
}
