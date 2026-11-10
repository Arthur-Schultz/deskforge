package com.deskforge.support;

import java.time.Clock;
import java.time.Duration;

import com.deskforge.privacy.dto.ProtocolResponse;
import com.deskforge.shared.web.ProtocolGenerator;
import com.deskforge.store.StoreProperties;
import com.deskforge.support.dto.ContactRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
@Tag(name = "Atendimento (SAC)")
public class ContactController {

    private final ContactMessageRepository messages;
    private final StoreProperties store;
    private final Clock clock;

    public ContactController(ContactMessageRepository messages, StoreProperties store, Clock clock) {
        this.messages = messages;
        this.store = store;
        this.clock = clock;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @Operation(summary = "Registra um atendimento e devolve o protocolo com o prazo de resposta")
    public ProtocolResponse contact(@Valid @RequestBody ContactRequest request) {
        String orderNumber = request.orderNumber() == null || request.orderNumber().isBlank()
                ? null : request.orderNumber();
        ContactMessage message = messages.save(new ContactMessage(ProtocolGenerator.generate("SAC", clock),
                request.fullName().strip(), request.email().strip().toLowerCase(), request.subject().strip(),
                request.message().strip(), orderNumber, clock.instant(),
                Duration.ofDays(store.supportResponseDays())));
        return new ProtocolResponse(message.getProtocol(), message.getCreatedAt(), message.getRespondBy());
    }
}
