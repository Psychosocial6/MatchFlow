package ru.matchflow.exceptions.custom;

public class TournamentNotFoundException extends NotFoundException {
    public TournamentNotFoundException(String message) {
        super(message);
    }
}
