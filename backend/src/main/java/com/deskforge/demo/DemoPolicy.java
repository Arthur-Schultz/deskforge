package com.deskforge.demo;

import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class DemoPolicy {

    private static final String SHARED_ACCOUNT =
            "Esta conta de demonstração é compartilhada por todos os visitantes e não pode fazer esta alteração. "
                    + "Crie sua própria conta para testar.";
    private static final String READ_ONLY_PANEL =
            "Na demonstração pública o painel administrativo é somente leitura. Rode o projeto localmente para testar as alterações.";

    private final DemoProperties properties;

    public DemoPolicy(DemoProperties properties) {
        this.properties = properties;
    }

    public boolean isPublicDemo() {
        return properties.publicDemo();
    }

    public boolean isProtectedAccount(String email) {
        return properties.publicDemo() && email != null
                && properties.sharedAccounts().contains(email.strip().toLowerCase(Locale.ROOT));
    }

    public void ensureAccountCanChange(String email) {
        if (isProtectedAccount(email)) {
            throw new DemoRestrictionException(SHARED_ACCOUNT);
        }
    }

    public void ensurePanelCanChange(String email) {
        if (isProtectedAccount(email)) {
            throw new DemoRestrictionException(READ_ONLY_PANEL);
        }
    }
}
