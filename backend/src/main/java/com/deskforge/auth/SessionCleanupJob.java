package com.deskforge.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "deskforge.jobs.enabled", havingValue = "true", matchIfMissing = true)
public class SessionCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(SessionCleanupJob.class);

    private final SessionService sessions;

    public SessionCleanupJob(SessionService sessions) {
        this.sessions = sessions;
    }

    @Scheduled(cron = "${deskforge.security.session.cleanup-cron:0 15 4 * * *}")
    public void purgeExpiredSessions() {
        int purged = sessions.purgeExpired();
        if (purged > 0) {
            log.info("Purged {} expired refresh tokens", purged);
        }
    }
}
