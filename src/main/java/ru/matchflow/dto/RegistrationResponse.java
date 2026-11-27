package ru.matchflow.dto;

import lombok.Builder;
import ru.matchflow.entities.UserAccount;

@Builder
public record RegistrationResponse(int id, String username, String email, boolean emailVerified) {
    public static RegistrationResponse fromEntity(UserAccount user) {
        return builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .emailVerified(user.getEmailVerifiedAt() != null)
                .build();
    }
}
