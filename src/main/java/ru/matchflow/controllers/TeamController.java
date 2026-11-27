package ru.matchflow.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.matchflow.dto.TeamRequest;
import ru.matchflow.dto.TeamResponse;
import ru.matchflow.services.TeamService;

import java.security.Principal;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse create(Principal principal, @Valid @RequestBody TeamRequest request) {
        int captainId = Integer.parseInt(principal.getName());
        return TeamResponse.from(teamService.create(captainId, request));
    }
}
