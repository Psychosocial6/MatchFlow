package ru.matchflow.events;

public record EmailVerificationRequested(String email, String token) {
}
