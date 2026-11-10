package com.deskforge.privacy;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DataSubjectRequestRepository extends JpaRepository<DataSubjectRequest, UUID> {

    long countByStatus(String status);
}
