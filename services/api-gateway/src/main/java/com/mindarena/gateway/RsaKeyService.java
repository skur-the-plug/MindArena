package com.mindarena.gateway;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RsaKeyService {

    private final PublicKey publicKey;

    public RsaKeyService(@Value("${mindarena.jwt.public-key}") String publicKey) {
        this.publicKey = parsePublicKey(publicKey);
    }

    public PublicKey publicKey() {
        return publicKey;
    }

    private PublicKey parsePublicKey(String value) {
        try {
            String normalized = value.replace("\\n", "\n")
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] encoded = Base64.getDecoder().decode(normalized.getBytes(StandardCharsets.UTF_8));
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid JWT RSA public key", ex);
        }
    }
}
