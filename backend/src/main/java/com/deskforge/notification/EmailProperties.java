package com.deskforge.notification;

import java.time.Duration;
import java.time.ZoneId;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deskforge.mail")
public record EmailProperties(
        String from,
        int batchSize,
        Duration dispatchInterval,
        ZoneId zone) {
}
