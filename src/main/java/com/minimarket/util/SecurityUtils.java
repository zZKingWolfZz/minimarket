package com.minimarket.util;

public final class SecurityUtils {
    private SecurityUtils() {
        // Prevent instantiation
    }

    /**
     * Sanitizes strings to prevent CRLF injection in logs by replacing newlines and carriage returns with underscores.
     * @param input The string to sanitize.
     * @return The sanitized string.
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("[\r\n]", "_");
    }

    private static final String ALGORITHM = "AES";
    private static final byte[] KEY_BYTES = new byte[] {
        'M', 'i', 'n', 'i', 'M', 'a', 'r', 'k', 'e', 't', 'Y', 'u', 'l', 'y', 'S', 'e'
    };

    public static String encrypt(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        try {
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(KEY_BYTES, ALGORITHM);
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(ALGORITHM);
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            String b64 = java.util.Base64.getEncoder().encodeToString(encrypted);
            return "ENC(" + b64 + ")";
        } catch (Exception e) {
            return value;
        }
    }

    public static String decrypt(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (value.startsWith("ENC(") && value.endsWith(")")) {
            try {
                String inner = value.substring(4, value.length() - 1);
                javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(KEY_BYTES, ALGORITHM);
                javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(ALGORITHM);
                cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey);
                byte[] decoded = java.util.Base64.getDecoder().decode(inner);
                return new String(cipher.doFinal(decoded), java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception e) {
                return value;
            }
        }
        return value;
    }
}
