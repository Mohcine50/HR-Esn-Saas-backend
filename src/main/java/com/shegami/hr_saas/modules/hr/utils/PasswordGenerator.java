package com.shegami.hr_saas.modules.hr.utils;

import lombok.experimental.UtilityClass;

import java.security.SecureRandom;

@UtilityClass
public class PasswordGenerator {

    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{}|;:,.<>?";
    private static final int DEFAULT_LENGTH = 14;

    public static String generatePassword() {

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(DEFAULT_LENGTH);

        for (int i = 0; i < DEFAULT_LENGTH; i++) {
            int randomIndex = random.nextInt(CHAR_POOL.length());
            password.append(CHAR_POOL.charAt(randomIndex));
        }

        return password.toString();
    }
}
