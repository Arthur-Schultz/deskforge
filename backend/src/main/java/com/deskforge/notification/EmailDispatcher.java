package com.deskforge.notification;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailDispatcher {

    private static final Logger log = LoggerFactory.getLogger(EmailDispatcher.class);

    private final EmailMessageRepository messages;
    private final JavaMailSender mailSender;
    private final EmailProperties properties;
    private final Clock clock;

    public EmailDispatcher(EmailMessageRepository messages, JavaMailSender mailSender, EmailProperties properties,
                           Clock clock) {
        this.messages = messages;
        this.mailSender = mailSender;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public int dispatchDue() {
        List<EmailMessage> due = messages.claimDue(clock.instant(), properties.batchSize());
        int sent = 0;
        for (EmailMessage message : due) {
            Instant now = clock.instant();
            try {
                mailSender.send(toMime(message));
                message.markSent(now);
                sent++;
            } catch (MessagingException | RuntimeException ex) {
                message.registerFailure(ex.getMessage(), now);
                log.warn("Could not send e-mail {} ({} of {} attempts): {}", message.getDedupKey(),
                        message.getAttempts(), EmailMessage.MAX_ATTEMPTS, ex.getMessage());
            }
        }
        return sent;
    }

    private MimeMessage toMime(EmailMessage message) throws MessagingException {
        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());
        helper.setFrom(properties.from());
        helper.setTo(message.getRecipient());
        helper.setSubject(message.getSubject());
        helper.setText(message.getTextBody(), message.getHtmlBody());
        mime.setHeader("X-DeskForge-Message-Id", message.getId().toString());
        return mime;
    }
}
