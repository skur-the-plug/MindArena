package com.mindarena.submission.security;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RsaPublicKeyService {

    private final RSAPublicKey publicKey;

    public RsaPublicKeyService(@Value("${mindarena.jwt.public-key}") String publicKey) {
        try {
            String normalized = publicKey.replace("\\n", "\n")
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] encoded = Base64.getDecoder().decode(normalized.getBytes(StandardCharsets.UTF_8));
            this.publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(encoded));
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid JWT RSA public key", exception);
        }
    }

    public RSAPublicKey publicKey() {
        return publicKey;
    }
}
