package com.deskforge.auth.dto;

import java.time.Instant;
import java.util.UUID;

import com.deskforge.user.Role;
import com.deskforge.user.User;

public record UserResponse(UUID id, String fullName, String email, Role role, boolean marketingConsent,
                           Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole(),
                user.isMarketingConsent(), user.getCreatedAt());
    }
}
