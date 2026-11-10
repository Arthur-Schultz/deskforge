package com.deskforge.audit;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

    @Modifying
    @Transactional
    @Query("delete from AccessLog a where a.createdAt < :limit")
    int deleteOlderThan(@Param("limit") Instant limit);
}
