package com.deskforge.notification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmailMessageRepository extends JpaRepository<EmailMessage, UUID> {

    boolean existsByDedupKey(String dedupKey);

    long countByStatus(EmailStatus status);

    Optional<EmailMessage> findByDedupKey(String dedupKey);

    List<EmailMessage> findAllByRecipientOrderByCreatedAtAsc(String recipient);

    @NativeQuery("""
            SELECT * FROM email_outbox
            WHERE status = 'PENDING' AND next_attempt_at <= :now
            ORDER BY next_attempt_at
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """)
    List<EmailMessage> claimDue(@Param("now") Instant now, @Param("limit") int limit);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from EmailMessage m where m.recipient = :recipient and m.status <> com.deskforge.notification.EmailStatus.SENT")
    int deleteUnsentTo(@Param("recipient") String recipient);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update EmailMessage m set m.recipient = :replacement where m.recipient = :recipient")
    int replaceRecipient(@Param("recipient") String recipient, @Param("replacement") String replacement);
}
