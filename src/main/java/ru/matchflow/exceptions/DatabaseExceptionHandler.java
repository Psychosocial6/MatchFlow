package ru.matchflow.exceptions;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DatabaseExceptionHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleConstraintViolation(DataIntegrityViolationException failure) {
        if (!(failure.getCause() instanceof ConstraintViolationException violation)
                || violation.getConstraintName() == null) {
            throw failure;
        }

        return switch (violation.getConstraintName()) {
            case "teams_name_key" -> ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Team name already exists");
            case "teams_tag_key" -> ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Team tag already exists");
            case "fk_tournaments_sport" -> ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Sport not found");
            case "fk_tournaments_admin", "fk_teams_captain" ->
                    ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Account no longer exists");
            case "chk_tournaments_dates_timeline" ->
                    ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid tournament dates");
            default -> throw failure;
        };
    }
}
