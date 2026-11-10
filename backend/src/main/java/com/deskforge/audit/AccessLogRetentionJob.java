package com.deskforge.audit;

import java.time.Clock;
import java.time.Period;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "deskforge.jobs.enabled", havingValue = "true", matchIfMissing = true)
public class AccessLogRetentionJob {

    private static final Logger log = LoggerFactory.getLogger(AccessLogRetentionJob.class);

    private final AccessLogRepository repository;
    private final Clock clock;
    private final Period retention;

    public AccessLogRetentionJob(AccessLogRepository repository, Clock clock,
                                 @Value("${deskforge.audit.access-log-retention:P6M}") Period retention) {
        this.repository = repository;
        this.clock = clock;
        this.retention = retention;
    }

    @Scheduled(cron = "${deskforge.audit.retention-cron:0 30 3 * * *}")
    public void purgeExpiredLogs() {
        var limit = clock.instant().atZone(ZoneOffset.UTC).minus(retention).toInstant();
        int removed = repository.deleteOlderThan(limit);
        log.info("Access log retention: removed {} entries older than {}", removed, limit);
    }
}
