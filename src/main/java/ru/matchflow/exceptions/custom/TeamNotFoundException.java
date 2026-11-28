package ru.matchflow.exceptions.custom;

public class TeamNotFoundException extends NotFoundException {
    public TeamNotFoundException(String message) {
        super(message);
    }
}
