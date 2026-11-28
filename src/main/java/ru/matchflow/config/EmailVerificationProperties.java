package ru.matchflow.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "matchflow.email")
public record EmailVerificationProperties(
        @NotBlank @Email String from,
        @NotNull URI verificationUrl,
        @NotNull @DurationMin(seconds = 1) Duration tokenTtl,
        @NotNull @DurationMin(seconds = 1) Duration resendInterval
) {
}
