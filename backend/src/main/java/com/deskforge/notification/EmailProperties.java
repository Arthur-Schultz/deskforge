package com.deskforge.notification;

import java.time.Duration;
import java.time.ZoneId;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deskforge.mail")
public record EmailProperties(
        String from,
        int batchSize,
        Duration dispatchInterval,
        ZoneId zone,
        List<String> suppressedDomains) {

    public EmailProperties {
        suppressedDomains = suppressedDomains == null ? List.of()
                : suppressedDomains.stream().map(String::strip).filter(domain -> !domain.isEmpty())
                        .map(String::toLowerCase).toList();
    }
}
