package com.deskforge.auth;

public enum RevokeReason {
    ROTATED,
    LOGOUT,
    LOGOUT_ALL,
    REUSE_DETECTED,
    PASSWORD_CHANGED,
    ACCOUNT_DELETED
}
