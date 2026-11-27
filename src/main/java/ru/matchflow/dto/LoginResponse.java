package ru.matchflow.dto;

import lombok.Builder;

@Builder
public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
}
