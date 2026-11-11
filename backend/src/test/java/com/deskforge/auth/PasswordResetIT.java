package com.deskforge.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.deskforge.notification.EmailDispatcher;
import com.deskforge.notification.EmailMessage;
import com.deskforge.notification.EmailMessageRepository;
import com.deskforge.notification.EmailTemplate;
import com.deskforge.support.MailContent;
import com.deskforge.support.PostgresIntegrationTest;
import com.deskforge.user.User;
import com.deskforge.user.UserRepository;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

class PasswordResetIT extends PostgresIntegrationTest {

    @RegisterExtension
    static final GreenMailExtension GREEN_MAIL = new GreenMailExtension(ServerSetupTest.SMTP);

    private static final Pattern RESET_LINK = Pattern.compile("/redefinir-senha#token=([A-Za-z0-9_-]+)");

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository users;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailDispatcher dispatcher;

    @Autowired
    private EmailMessageRepository emails;

    @Autowired
    private PasswordResetTokenRepository tokens;

    private MockMvc mvc;
    private String email;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        email = "reset-" + UUID.randomUUID() + "@test.dev";
        users.save(User.newCustomer("Bruno Reset", email, passwordEncoder.encode("Antiga123"), false, "v1",
                Instant.now()));
    }

    @Test
    void resetsThePasswordWithTheLinkSentByEmail() throws Exception {
        forgot(email).andExpect(status().isAccepted());
        dispatcher.dispatchDue();

        MimeMessage resetEmail = only(receivedBy(email));
        assertThat(resetEmail.getSubject()).isEqualTo("Redefina sua senha da DeskForge");
        String token = tokenFrom(MailContent.text(resetEmail));
        assertThat(tokens.findByTokenHash(token)).isEmpty();
        assertThat(tokens.findByTokenHash(PasswordResetService.hash(token))).isPresent();

        reset(token, "Nova12345").andExpect(status().isNoContent());

        login(email, "Nova12345").andExpect(status().isOk());
        login(email, "Antiga123").andExpect(status().isUnauthorized());

        dispatcher.dispatchDue();
        List<MimeMessage> received = receivedBy(email);
        assertThat(received).hasSize(2);
        assertThat(received.get(1).getSubject()).isEqualTo("Sua senha da DeskForge foi alterada");
    }

    @Test
    void aLinkCanOnlyBeUsedOnce() throws Exception {
        forgot(email).andExpect(status().isAccepted());
        dispatcher.dispatchDue();
        String token = tokenFrom(MailContent.text(only(receivedBy(email))));

        reset(token, "Nova12345").andExpect(status().isNoContent());
        reset(token, "Outra12345")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Este link de redefinição é inválido ou expirou. Peça um novo."));

        login(email, "Nova12345").andExpect(status().isOk());
    }

    @Test
    void doesNotRevealWhetherAnEmailIsRegistered() throws Exception {
        String unknown = "ninguem-" + UUID.randomUUID() + "@test.dev";

        forgot(unknown).andExpect(status().isAccepted());

        assertThat(emails.findAllByRecipientOrderByCreatedAtAsc(unknown)).isEmpty();
    }

    @Test
    void repeatedRequestsWithinTheCooldownSendASingleEmail() throws Exception {
        forgot(email).andExpect(status().isAccepted());
        forgot(email.toUpperCase()).andExpect(status().isAccepted());

        List<EmailMessage> queued = emails.findAllByRecipientOrderByCreatedAtAsc(email);
        assertThat(queued).hasSize(1);
        assertThat(queued.getFirst().getTemplate()).isEqualTo(EmailTemplate.PASSWORD_RESET);
    }

    @Test
    void rejectsUnknownTokensAndWeakPasswords() throws Exception {
        reset("token-que-nao-existe", "Nova12345").andExpect(status().isUnprocessableContent());
        reset("qualquer", "fraca").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.newPassword").exists());
    }

    private ResultActions forgot(String address) throws Exception {
        return mvc.perform(post("/api/auth/password/forgot")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + address + "\"}"));
    }

    private ResultActions reset(String token, String newPassword) throws Exception {
        return mvc.perform(post("/api/auth/password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + token + "\",\"newPassword\":\"" + newPassword + "\"}"));
    }

    private ResultActions login(String address, String password) throws Exception {
        return mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + address + "\",\"password\":\"" + password + "\"}"));
    }

    private static String tokenFrom(String body) {
        Matcher matcher = RESET_LINK.matcher(body);
        assertThat(matcher.find()).as("reset link in %s", body).isTrue();
        return matcher.group(1);
    }

    private static MimeMessage only(List<MimeMessage> messages) {
        assertThat(messages).hasSize(1);
        return messages.getFirst();
    }

    private static List<MimeMessage> receivedBy(String address) {
        return Arrays.stream(GREEN_MAIL.getReceivedMessages())
                .filter(message -> {
                    try {
                        return Arrays.toString(message.getRecipients(Message.RecipientType.TO)).contains(address);
                    } catch (Exception ex) {
                        throw new IllegalStateException(ex);
                    }
                })
                .toList();
    }
}
