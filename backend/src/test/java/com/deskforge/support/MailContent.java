package com.deskforge.support;

import java.util.ArrayList;
import java.util.List;

import jakarta.mail.Multipart;
import jakarta.mail.Part;

public final class MailContent {

    private MailContent() {
    }

    public static String text(Part part) {
        return String.join("\n", textParts(part, "text/plain"));
    }

    public static String html(Part part) {
        return String.join("\n", textParts(part, "text/html"));
    }

    private static List<String> textParts(Part part, String mimeType) {
        try {
            List<String> found = new ArrayList<>();
            if (part.isMimeType(mimeType)) {
                found.add((String) part.getContent());
            } else if (part.isMimeType("multipart/*")) {
                Multipart multipart = (Multipart) part.getContent();
                for (int i = 0; i < multipart.getCount(); i++) {
                    found.addAll(textParts(multipart.getBodyPart(i), mimeType));
                }
            }
            return found;
        } catch (Exception ex) {
            throw new IllegalStateException("Não foi possível ler o e-mail", ex);
        }
    }
}
