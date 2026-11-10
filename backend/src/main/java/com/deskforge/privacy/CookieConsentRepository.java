package com.deskforge.privacy;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CookieConsentRepository extends JpaRepository<CookieConsent, UUID> {
}
