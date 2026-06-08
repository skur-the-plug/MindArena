package com.mindarena.identity.service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RsaKeyService {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public RsaKeyService(
            @Value("${mindarena.jwt.private-key}") String privateKey,
            @Value("${mindarena.jwt.public-key}") String publicKey
    ) {
        this.privateKey = parsePrivateKey(privateKey);
        this.publicKey = parsePublicKey(publicKey);
    }

    public PrivateKey privateKey() {
        return privateKey;
    }

    public PublicKey publicKey() {
        return publicKey;
    }

    private PrivateKey parsePrivateKey(String value) {
        try {
            byte[] encoded = decodePemOrBase64(value, "PRIVATE KEY");
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(encoded));
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid JWT RSA private key", ex);
        }
    }

    private PublicKey parsePublicKey(String value) {
        try {
            byte[] encoded = decodePemOrBase64(value, "PUBLIC KEY");
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid JWT RSA public key", ex);
        }
    }

    private byte[] decodePemOrBase64(String value, String label) {
        String normalized = value.replace("\\n", "\n")
                .replace("-----BEGIN " + label + "-----", "")
                .replace("-----END " + label + "-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(normalized.getBytes(StandardCharsets.UTF_8));
    }
}
