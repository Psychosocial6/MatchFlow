package ru.matchflow.exceptions.custom;

public class ApplicationNotFoundException extends NotFoundException {
    public ApplicationNotFoundException(String message) {
        super(message);
    }
}
