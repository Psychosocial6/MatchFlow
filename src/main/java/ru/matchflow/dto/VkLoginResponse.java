package ru.matchflow.dto;

import lombok.Builder;

@Builder
public record VkLoginResponse(Status status, LoginResponse login, String registrationTicket) {
    public enum Status {
        SIGNED_IN,
        REGISTRATION_REQUIRED,
        LINKED
    }
}
