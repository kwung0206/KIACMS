package com.kiacms.global.controller;

import com.kiacms.global.response.ApiResponse;
import com.kiacms.global.response.HealthCheckResponse;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthCheckController {

    private final String applicationName;

    public HealthCheckController(@Value("${spring.application.name}") String applicationName) {
        this.applicationName = applicationName;
    }

    @GetMapping("/health")
    public ApiResponse<HealthCheckResponse> health() {
        return ApiResponse.ok(new HealthCheckResponse(applicationName, "UP", Instant.now()));
    }
}
