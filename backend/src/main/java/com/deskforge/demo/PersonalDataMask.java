package com.deskforge.demo;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class PersonalDataMask {

    private static final String HIDDEN = "•••";

    private PersonalDataMask() {
    }

    public static String name(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return fullName;
        }
        return Arrays.stream(fullName.strip().split("\\s+"))
                .map(part -> part.charAt(0) + "•".repeat(Math.max(2, part.length() - 1)))
                .collect(Collectors.joining(" "));
    }

    public static String email(String email) {
        if (email == null || !email.contains("@")) {
            return HIDDEN;
        }
        return email.charAt(0) + "•••@" + email.substring(email.indexOf('@') + 1);
    }

    public static String hidden(String value) {
        return value == null ? null : HIDDEN;
    }
}
