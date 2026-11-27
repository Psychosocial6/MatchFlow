package ru.matchflow.services;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Service;
import ru.matchflow.config.JwtProperties;
import ru.matchflow.dto.LoginResponse;
import ru.matchflow.entities.UserRole;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService implements JwtDecoder {
    private static final String ROLE_CLAIM = "role";

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final JwsHeader header;
    private final String issuer;
    private final long ttlSeconds;

    public JwtService(JwtProperties properties) {
        SecretKey signingKey = properties.signingKey();
        this.issuer = properties.jwtIssuer();
        this.ttlSeconds = properties.jwtTtl().toSeconds();
        this.header = JwsHeader.with(properties.jwtAlgorithm()).build();
        this.encoder = NimbusJwtEncoder.withSecretKey(signingKey).algorithm(properties.jwtAlgorithm()).build();

        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(signingKey)
                .macAlgorithm(properties.jwtAlgorithm())
                .build();
        jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        this.decoder = jwtDecoder;
    }

    public LoginResponse issueToken(int userId, UserRole role) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(Integer.toString(userId))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ttlSeconds))
                .claim(ROLE_CLAIM, role.name())
                .build();
        String accessToken = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType(OAuth2AccessToken.TokenType.BEARER.getValue())
                .expiresIn(ttlSeconds)
                .build();
    }

    public JwtAuthenticationConverter authenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = jwt.getClaimAsString(ROLE_CLAIM);
            String effectiveRole = role == null ? UserRole.USER.name() : role;
            return AuthorityUtils.createAuthorityList("ROLE_" + effectiveRole);
        });
        return converter;
    }

    @Override
    public Jwt decode(String token) {
        return decoder.decode(token);
    }
}
