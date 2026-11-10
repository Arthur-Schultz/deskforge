package com.deskforge.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "deskforge.jobs.enabled", havingValue = "true", matchIfMissing = true)
public class OrderExpirationJob {

    private static final Logger log = LoggerFactory.getLogger(OrderExpirationJob.class);

    private final OrderService orderService;

    public OrderExpirationJob(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(fixedDelayString = "${deskforge.jobs.order-expiration-interval:PT1M}")
    public void expireUnpaidOrders() {
        int expired = orderService.expireUnpaidOrders();
        if (expired > 0) {
            log.info("Canceled {} unpaid orders and released their stock", expired);
        }
    }
}
