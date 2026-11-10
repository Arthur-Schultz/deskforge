package com.deskforge.privacy.dto;

import java.time.Instant;

public record ProtocolResponse(String protocol, Instant createdAt, Instant respondBy) {
}
