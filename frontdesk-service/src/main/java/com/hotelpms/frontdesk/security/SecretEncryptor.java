package com.hotelpms.frontdesk.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

/** Encrypts secrets stored in tenant settings. */
@Component
public final class SecretEncryptor {
    private final TextEncryptor encryptor;

    /**
     * Creates an encryptor using the configured key and salt.
     *
     * @param key encryption key
     * @param salt encryption salt
     */
    public SecretEncryptor(
            @Value("${security.secrets.encryption-key:change-me}") final String key,
            @Value("${security.secrets.encryption-salt:00000000000000000000000000000000}") final String salt) {
        this.encryptor = Encryptors.delux(key, salt);
    }

    /**
     * Encrypts a non-blank secret.
     *
     * @param plaintext secret in plain text
     * @return encrypted secret, or null for blank input
     */
    public String encrypt(final String plaintext) {
        return plaintext == null || plaintext.isBlank() ? null : encryptor.encrypt(plaintext);
    }

    /**
     * Decrypts a non-blank secret.
     *
     * @param ciphertext encrypted secret
     * @return plain text secret, or null for blank input
     */
    public String decrypt(final String ciphertext) {
        return ciphertext == null || ciphertext.isBlank() ? null : encryptor.decrypt(ciphertext);
    }
}
