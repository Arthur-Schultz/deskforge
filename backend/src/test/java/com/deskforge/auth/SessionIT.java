package com.deskforge.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import com.deskforge.support.PostgresIntegrationTest;
import com.deskforge.user.User;
import com.deskforge.user.UserRepository;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = {
        "deskforge.security.session.rotation-grace=PT0S",
        "deskforge.security.rate-limits.login-per-account.max-attempts=3",
        "deskforge.security.rate-limits.login-per-account.window=PT15M"
})
class SessionIT extends PostgresIntegrationTest {

    private static final String COOKIE = "df_session";
    private static final String PASSWORD = "Sessao2026";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository users;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokens;

    private MockMvc mvc;
    private String email;
    private UUID userId;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        email = "sessao-" + UUID.randomUUID() + "@test.dev";
        userId = users.save(User.newCustomer("Carla Sessao", email, passwordEncoder.encode(PASSWORD), false, "v1",
                Instant.now())).getId();
    }

    @Test
    void loginStoresTheSessionInAHardenedCookieAndReturnsAShortAccessToken() throws Exception {
        MvcResult result = login(PASSWORD).andExpect(status().isOk()).andReturn();

        String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).startsWith(COOKIE + "=")
                .contains("HttpOnly", "Secure", "SameSite=Strict", "Path=/api/auth", "Max-Age=1209600");
        String body = result.getResponse().getContentAsString();
        assertThat(body).doesNotContain(cookieValue(result));
        Instant expiresAt = Instant.parse(JsonPath.read(body, "$.expiresAt"));
        assertThat(expiresAt).isBefore(Instant.now().plusSeconds(16 * 60));
    }

    @Test
    void refreshRotatesTheCookieAndReturnsANewAccessToken() throws Exception {
        String first = cookieValue(login(PASSWORD).andReturn());

        MvcResult refreshed = refresh(first).andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value(email))
                .andReturn();
        String second = cookieValue(refreshed);
        String accessToken = JsonPath.read(refreshed.getResponse().getContentAsString(), "$.accessToken");

        assertThat(second).isNotEqualTo(first);
        mvc.perform(get("/api/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());
        refresh(second).andExpect(status().isOk());
    }

    @Test
    void reusingARotatedTokenEndsTheWholeSession() throws Exception {
        String first = cookieValue(login(PASSWORD).andReturn());
        String second = cookieValue(refresh(first).andReturn());

        refresh(first)
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
        refresh(second).andExpect(status().isUnauthorized());

        assertThat(refreshTokens.findAllByUserId(userId))
                .anyMatch(token -> token.getRevokeReason() == RevokeReason.REUSE_DETECTED);
    }

    @Test
    void logoutEndsOnlyThisBrowserAndLogoutAllEndsEverySession() throws Exception {
        String laptop = cookieValue(login(PASSWORD).andReturn());
        MvcResult phoneLogin = login(PASSWORD).andReturn();
        String phone = cookieValue(phoneLogin);
        String tablet = cookieValue(login(PASSWORD).andReturn());

        mvc.perform(post("/api/auth/logout").cookie(new Cookie(COOKIE, laptop)))
                .andExpect(status().isNoContent());
        refresh(laptop).andExpect(status().isUnauthorized());
        String phoneRotated = cookieValue(refresh(phone).andExpect(status().isOk()).andReturn());

        String accessToken = JsonPath.read(phoneLogin.getResponse().getContentAsString(), "$.accessToken");
        mvc.perform(post("/api/auth/logout-all").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNoContent());
        refresh(phoneRotated).andExpect(status().isUnauthorized());
        refresh(tablet).andExpect(status().isUnauthorized());
    }

    @Test
    void refreshWithoutACookieIsRejected() throws Exception {
        mvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Sessão expirada. Entre novamente."));
        refresh("nao-existe").andExpect(status().isUnauthorized());
    }

    @Test
    void deletingTheAccountEndsItsSessions() throws Exception {
        MvcResult login = login(PASSWORD).andReturn();
        String accessToken = JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");

        mvc.perform(delete("/api/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        refresh(cookieValue(login)).andExpect(status().isUnauthorized());
    }

    @Test
    void repeatedWrongPasswordsLockTheAccountForAWhile() throws Exception {
        for (int i = 0; i < 3; i++) {
            login("Errada123").andExpect(status().isUnauthorized());
        }

        login(PASSWORD)
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(jsonPath("$.detail").value("Muitas tentativas. Tente de novo em 15 minutos."));
    }

    private ResultActions login(String password) throws Exception {
        return mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }

    private ResultActions refresh(String cookie) throws Exception {
        return mvc.perform(post("/api/auth/refresh").cookie(new Cookie(COOKIE, cookie)));
    }

    private static String cookieValue(MvcResult result) {
        String header = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(header).isNotNull();
        return header.substring(COOKIE.length() + 1, header.indexOf(';'));
    }
}
