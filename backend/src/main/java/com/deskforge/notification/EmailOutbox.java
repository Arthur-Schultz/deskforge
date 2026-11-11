package com.deskforge.notification;

import java.time.Clock;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailOutbox {

    private static final Logger log = LoggerFactory.getLogger(EmailOutbox.class);

    private final EmailMessageRepository messages;
    private final EmailRenderer renderer;
    private final EmailProperties properties;
    private final Clock clock;

    public EmailOutbox(EmailMessageRepository messages, EmailRenderer renderer, EmailProperties properties,
                       Clock clock) {
        this.messages = messages;
        this.renderer = renderer;
        this.properties = properties;
        this.clock = clock;
    }

    private boolean isSuppressed(String recipient) {
        String domain = recipient.substring(recipient.indexOf('@') + 1).toLowerCase(Locale.ROOT);
        return properties.suppressedDomains().contains(domain);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void enqueue(String dedupKey, EmailTemplate template, String recipient, String subject,
                        Map<String, Object> variables) {
        if (isSuppressed(recipient)) {
            log.debug("Recipient domain of {} is suppressed, skipping", dedupKey);
            return;
        }
        if (messages.existsByDedupKey(dedupKey)) {
            log.debug("E-mail {} already queued, skipping", dedupKey);
            return;
        }
        RenderedEmail content = renderer.render(template, subject, variables);
        messages.save(EmailMessage.queue(dedupKey, template, recipient, content, clock.instant()));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void forgetRecipient(String recipient, String replacement) {
        messages.deleteUnsentTo(recipient);
        messages.replaceRecipient(recipient, replacement);
    }
}
