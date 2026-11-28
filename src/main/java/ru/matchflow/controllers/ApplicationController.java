package ru.matchflow.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.matchflow.dto.ApplicationRequest;
import ru.matchflow.dto.ApplicationResponse;
import ru.matchflow.dto.TeamRequest;
import ru.matchflow.services.ApplicationService;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    public final ApplicationService applicationService;

    //TODO: get applications with sorting, filtering and paging

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(
            @Valid
            @RequestBody
            ApplicationRequest request
    ) {
        ApplicationResponse response = applicationService.createApplication(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
