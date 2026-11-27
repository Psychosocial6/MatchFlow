package ru.matchflow.exceptions.custom;

public class SportNotFoundException extends RuntimeException {
    public SportNotFoundException(String message) {
        super(message);
    }
}
