package ru.matchflow.dto;

import lombok.Builder;
import ru.matchflow.entities.UserAccount;
import ru.matchflow.entities.UserRole;

@Builder
public record AccountResponse(int id, String username, String email, UserRole role,
                              boolean emailVerified, boolean vkLinked) {
    public static AccountResponse fromEntity(UserAccount user) {
        return builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .emailVerified(user.getEmailVerifiedAt() != null)
                .vkLinked(user.getVkId() != null)
                .build();
    }
}
