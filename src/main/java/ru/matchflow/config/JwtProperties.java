package ru.matchflow.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.validation.annotation.Validated;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.util.Base64;

@Validated
@ConfigurationProperties(prefix = "matchflow.auth")
public record JwtProperties(
        @NotBlank(message = "AUTH_JWT_SECRET must be set") String jwtSecret,
        @NotBlank String jwtIssuer,
        @NotNull @DurationMin(seconds = 1) Duration jwtTtl,
        @NotNull MacAlgorithm jwtAlgorithm
) {
    public SecretKey signingKey() {
        byte[] key = Base64.getDecoder().decode(jwtSecret);
        return switch (jwtAlgorithm) {
            case HS256 -> signingKey(key, "HmacSHA256", 32);
            case HS384 -> signingKey(key, "HmacSHA384", 48);
            case HS512 -> signingKey(key, "HmacSHA512", 64);
        };
    }

    private static SecretKey signingKey(byte[] key, String algorithm, int minBytes) {
        return new SecretKeySpec(key, algorithm);
    }

    @Override
    public String toString() {
        return "JwtProperties[jwtIssuer=%s, jwtTtl=%s, jwtAlgorithm=%s]".formatted(jwtIssuer, jwtTtl, jwtAlgorithm);
    }
}
