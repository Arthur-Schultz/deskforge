package com.deskforge.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "deskforge.jobs.enabled", havingValue = "true", matchIfMissing = true)
public class EmailDispatchJob {

    private static final Logger log = LoggerFactory.getLogger(EmailDispatchJob.class);

    private final EmailDispatcher dispatcher;

    public EmailDispatchJob(EmailDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Scheduled(fixedDelayString = "${deskforge.mail.dispatch-interval:PT5S}")
    public void dispatch() {
        int sent = dispatcher.dispatchDue();
        if (sent > 0) {
            log.info("Sent {} queued e-mails", sent);
        }
    }
}
