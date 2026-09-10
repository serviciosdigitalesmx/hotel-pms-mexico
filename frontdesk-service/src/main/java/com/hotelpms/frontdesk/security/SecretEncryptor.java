package com.hotelpms.frontdesk.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

/** Encrypts secrets stored in tenant settings. */
@Component
public class SecretEncryptor {
    private final TextEncryptor encryptor;

    public SecretEncryptor(
            @Value("${security.secrets.encryption-key:change-me}") final String key,
            @Value("${security.secrets.encryption-salt:00000000000000000000000000000000}") final String salt) {
        this.encryptor = Encryptors.delux(key, salt);
    }

    public String encrypt(final String plaintext) {
        return plaintext == null || plaintext.isBlank() ? null : encryptor.encrypt(plaintext);
    }

    public String decrypt(final String ciphertext) {
        return ciphertext == null || ciphertext.isBlank() ? null : encryptor.decrypt(ciphertext);
    }
}
