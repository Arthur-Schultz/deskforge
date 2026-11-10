package com.deskforge.privacy.dto;

import java.time.Instant;
import java.util.List;

import com.deskforge.auth.dto.UserResponse;
import com.deskforge.order.dto.OrderResponse;

public record AccountDataExport(Instant generatedAt, String controller, String legalBasisNote, UserResponse profile,
                                Instant termsAcceptedAt, String privacyPolicyVersion, List<OrderResponse> orders) {
}
