package com.deskforge.auth;

import org.springframework.security.core.AuthenticationException;

public class InvalidSessionException extends AuthenticationException {

    public InvalidSessionException() {
        super("Sessão expirada. Entre novamente.");
    }
}
