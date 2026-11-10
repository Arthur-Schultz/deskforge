package com.deskforge.auth;

import java.util.UUID;

import com.deskforge.user.Role;

public record AuthenticatedUser(UUID id, String email, Role role) {
}
