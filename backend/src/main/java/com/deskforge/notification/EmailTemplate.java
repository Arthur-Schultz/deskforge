package com.deskforge.notification;

public enum EmailTemplate {
    PAYMENT_CONFIRMED("pagamento-confirmado"),
    ORDER_SHIPPED("pedido-enviado"),
    ORDER_CANCELED("pedido-cancelado"),
    PASSWORD_RESET("redefinir-senha"),
    PASSWORD_CHANGED("senha-alterada");

    private final String fileName;

    EmailTemplate(String fileName) {
        this.fileName = fileName;
    }

    public String htmlTemplate() {
        return "html/" + fileName;
    }

    public String textTemplate() {
        return "text/" + fileName;
    }
}
