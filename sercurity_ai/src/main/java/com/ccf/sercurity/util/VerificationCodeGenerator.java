package com.ccf.sercurity.util;

import java.security.SecureRandom;

public class VerificationCodeGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNPQRSTUVWXYZ123456789";

    public static String generateVerificationCode(int codeLength) {
        StringBuilder sb = new StringBuilder(codeLength);
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < codeLength; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }

}