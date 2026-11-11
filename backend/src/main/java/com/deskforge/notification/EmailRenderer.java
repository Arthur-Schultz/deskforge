package com.deskforge.notification;

import java.time.Clock;
import java.time.Year;
import java.util.Locale;
import java.util.Map;

import com.deskforge.store.StoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class EmailRenderer {

    static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final TemplateEngine templateEngine;
    private final StoreProperties store;
    private final EmailProperties properties;
    private final String frontendUrl;
    private final Clock clock;

    public EmailRenderer(TemplateEngine emailTemplateEngine, StoreProperties store, EmailProperties properties,
                         @Value("${deskforge.frontend-url}") String frontendUrl, Clock clock) {
        this.templateEngine = emailTemplateEngine;
        this.store = store;
        this.properties = properties;
        this.frontendUrl = frontendUrl;
        this.clock = clock;
    }

    public RenderedEmail render(EmailTemplate template, String subject, Map<String, Object> variables) {
        Context context = new Context(PT_BR);
        context.setVariable("subject", subject);
        context.setVariable("store", store);
        context.setVariable("siteUrl", frontendUrl);
        context.setVariable("year", Year.now(clock.withZone(properties.zone())).getValue());
        context.setVariables(variables);
        return new RenderedEmail(subject,
                templateEngine.process(template.htmlTemplate(), context),
                templateEngine.process(template.textTemplate(), context));
    }

    public String link(String path) {
        return frontendUrl + path;
    }
}
