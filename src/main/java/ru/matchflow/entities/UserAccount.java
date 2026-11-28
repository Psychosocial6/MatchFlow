package ru.matchflow.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String username;

    private String email;

    @Column(name = "password")
    private String passwordHash;

    @Column(name = "vk_id")
    private String vkId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private UserRole role = UserRole.USER;

    @Column(name = "email_verification_required", nullable = false)
    private boolean emailVerificationRequired;

    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    public boolean isEmailVerificationPending() {
        return emailVerificationRequired && emailVerifiedAt == null;
    }
}
