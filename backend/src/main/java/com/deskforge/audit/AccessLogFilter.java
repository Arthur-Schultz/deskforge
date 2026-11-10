package com.deskforge.audit;

import java.io.IOException;
import java.time.Clock;
import java.util.UUID;

import com.deskforge.auth.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AccessLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AccessLogFilter.class);
    private static final int MAX_PATH_LENGTH = 255;

    private final AccessLogRepository repository;
    private final Clock clock;

    public AccessLogFilter(AccessLogRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/") || "OPTIONS".equals(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            chain.doFilter(request, response);
        } finally {
            record(request, response);
        }
    }

    private void record(HttpServletRequest request, HttpServletResponse response) {
        try {
            String path = request.getRequestURI();
            if (path.length() > MAX_PATH_LENGTH) {
                path = path.substring(0, MAX_PATH_LENGTH);
            }
            repository.save(new AccessLog(request.getRemoteAddr(), request.getMethod(), path,
                    response.getStatus(), currentUserId(), clock.instant()));
        } catch (RuntimeException ex) {
            log.warn("Could not persist access log entry", ex);
        }
    }

    private UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user.id();
        }
        return null;
    }
}
