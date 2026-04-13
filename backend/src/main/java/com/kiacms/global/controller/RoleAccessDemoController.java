package com.kiacms.global.controller;

import com.kiacms.global.response.ApiResponse;
import com.kiacms.global.security.CustomUserPrincipal;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RoleAccessDemoController {

    @GetMapping("/student/dashboard")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<Map<String, Object>> studentDashboard(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ApiResponse.ok(Map.of("role", "STUDENT", "userId", principal.getUserId(), "email", principal.getEmail()));
    }

    @GetMapping("/instructor/dashboard")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ApiResponse<Map<String, Object>> instructorDashboard(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ApiResponse.ok(Map.of("role", "INSTRUCTOR", "userId", principal.getUserId(), "email", principal.getEmail()));
    }

    @GetMapping("/mentor/dashboard")
    @PreAuthorize("hasRole('MENTOR')")
    public ApiResponse<Map<String, Object>> mentorDashboard(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ApiResponse.ok(Map.of("role", "MENTOR", "userId", principal.getUserId(), "email", principal.getEmail()));
    }

    @GetMapping("/root/dashboard")
    @PreAuthorize("hasRole('ROOT')")
    public ApiResponse<Map<String, Object>> rootDashboard(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ApiResponse.ok(Map.of("role", "ROOT", "userId", principal.getUserId(), "email", principal.getEmail()));
    }
}
