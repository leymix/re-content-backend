package com.recontent.backend.health.controller;

import com.recontent.backend.health.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@Tag(name = "Health")
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {
    private final String serviceName;

    public HealthController(@Value("${spring.application.name}") String serviceName) {
        this.serviceName = serviceName;
    }

    @Operation(summary = "Application health")
    @GetMapping
    public HealthResponse health() {
        return new HealthResponse("UP", serviceName, "0.0.1-SNAPSHOT", Instant.now());
    }
}
