package ru.matchflow.dto;

import lombok.Builder;

@Builder
public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
    public static LoginResponse of(String token, long expiresIn) {
        return builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .build();
    }
}
