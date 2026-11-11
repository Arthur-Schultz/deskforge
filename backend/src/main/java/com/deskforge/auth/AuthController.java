package com.deskforge.auth;

import java.net.URI;

import com.deskforge.auth.dto.AuthResponse;
import com.deskforge.auth.dto.ForgotPasswordRequest;
import com.deskforge.auth.dto.LoginRequest;
import com.deskforge.auth.dto.RegisterRequest;
import com.deskforge.auth.dto.ResetPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação")
public class AuthController {

    private static final String REFRESH_COOKIE = "${deskforge.security.session.cookie-name}";

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final SessionCookies cookies;

    public AuthController(AuthService authService, PasswordResetService passwordResetService, SessionCookies cookies) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
        this.cookies = cookies;
    }

    @PostMapping("/register")
    @Operation(summary = "Cria uma conta de cliente coletando apenas os dados necessários")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                                 HttpServletRequest http) {
        return withSession(HttpStatus.CREATED, authService.register(request, http.getRemoteAddr()));
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica, devolve um token de acesso curto e grava a sessão num cookie HttpOnly")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        return withSession(HttpStatus.OK, authService.login(request, http.getRemoteAddr()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Troca o cookie de sessão por um novo token de acesso, girando o refresh token")
    public ResponseEntity<AuthResponse> refresh(@CookieValue(name = REFRESH_COOKIE, required = false) String token) {
        if (token == null || token.isBlank() || token.length() > 100) {
            throw new InvalidSessionException();
        }
        return withSession(HttpStatus.OK, authService.refresh(token));
    }

    @PostMapping("/logout")
    @Operation(summary = "Encerra a sessão deste navegador")
    public ResponseEntity<Void> logout(@CookieValue(name = REFRESH_COOKIE, required = false) String token) {
        if (token != null && !token.isBlank() && token.length() <= 100) {
            authService.logout(token);
        }
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookies.clear().toString()).build();
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Encerra as sessões da conta em todos os dispositivos")
    public ResponseEntity<Void> logoutEverywhere() {
        AuthenticatedUser user = CurrentUser.get();
        authService.logoutEverywhere(user.id(), user.email());
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookies.clear().toString()).build();
    }

    @PostMapping("/password/forgot")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Envia um link de redefinição de senha, sem revelar se o e-mail está cadastrado")
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, HttpServletRequest http) {
        passwordResetService.requestReset(request.email(), http.getRemoteAddr());
    }

    @PostMapping("/password/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Define uma nova senha usando o link recebido por e-mail e encerra todas as sessões")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest http) {
        passwordResetService.resetPassword(request.token(), request.newPassword(), http.getRemoteAddr());
    }

    @ExceptionHandler(InvalidSessionException.class)
    public ResponseEntity<ProblemDetail> handleInvalidSession(InvalidSessionException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problem.setType(URI.create("https://deskforge.dev/problems/session-expired"));
        problem.setTitle("Sessão expirada");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.SET_COOKIE, cookies.clear().toString())
                .body(problem);
    }

    private ResponseEntity<AuthResponse> withSession(HttpStatus status, AuthService.SignedIn signedIn) {
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookies.issue(signedIn.refreshToken()).toString())
                .body(signedIn.body());
    }
}
