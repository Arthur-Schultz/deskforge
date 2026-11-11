package com.deskforge.notification;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

final class EmailFormat {

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", EmailRenderer.PT_BR);

    private EmailFormat() {
    }

    static String money(BigDecimal value) {
        return NumberFormat.getCurrencyInstance(EmailRenderer.PT_BR).format(value).replace(' ', ' ');
    }

    static String dateTime(Instant instant, ZoneId zone) {
        return instant == null ? null : DATE_TIME.format(instant.atZone(zone));
    }
}
