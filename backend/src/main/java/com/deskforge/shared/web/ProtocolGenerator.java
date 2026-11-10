package com.deskforge.shared.web;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Year;

public final class ProtocolGenerator {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private ProtocolGenerator() {
    }

    public static String generate(String prefix, Clock clock) {
        StringBuilder code = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            code.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return prefix + "-" + Year.now(clock).getValue() + "-" + code;
    }
}
