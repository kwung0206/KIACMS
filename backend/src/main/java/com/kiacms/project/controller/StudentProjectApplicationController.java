package com.kiacms.project.controller;

import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.global.response.ApiResponse;
import com.kiacms.global.security.CustomUserPrincipal;
import com.kiacms.project.dto.request.SubmitProjectApplicationRequest;
import com.kiacms.project.dto.response.ProjectApplicationResponse;
import com.kiacms.project.service.ProjectApplicationService;
import com.kiacms.user.entity.User;
import com.kiacms.user.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/project-applications")
@RequiredArgsConstructor
public class StudentProjectApplicationController {

    private final ProjectApplicationService projectApplicationService;
    private final UserRepository userRepository;

    @PostMapping("/positions/{positionId}")
    public ApiResponse<ProjectApplicationResponse> submitApplication(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID positionId,
            @Valid @RequestBody SubmitProjectApplicationRequest request
    ) {
        return ApiResponse.ok(projectApplicationService.submitApplication(positionId, request, getCurrentUser(principal)));
    }

    @GetMapping("/me")
    public ApiResponse<List<ProjectApplicationResponse>> getMyApplications(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.ok(projectApplicationService.getMyApplications(getCurrentUser(principal)));
    }

    @GetMapping("/{applicationId}")
    public ApiResponse<ProjectApplicationResponse> getMyApplication(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID applicationId
    ) {
        return ApiResponse.ok(projectApplicationService.getMyApplication(applicationId, getCurrentUser(principal)));
    }

    @PutMapping("/{applicationId}")
    public ApiResponse<ProjectApplicationResponse> updateApplication(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID applicationId,
            @Valid @RequestBody SubmitProjectApplicationRequest request
    ) {
        return ApiResponse.ok(projectApplicationService.updateApplication(applicationId, request, getCurrentUser(principal)));
    }

    @PatchMapping("/{applicationId}/withdraw")
    public ApiResponse<ProjectApplicationResponse> withdrawApplication(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID applicationId
    ) {
        return ApiResponse.ok(projectApplicationService.withdrawApplication(applicationId, getCurrentUser(principal)));
    }

    @DeleteMapping("/{applicationId}")
    public ApiResponse<Void> deleteApplication(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID applicationId
    ) {
        projectApplicationService.deleteApplication(applicationId, getCurrentUser(principal));
        return ApiResponse.ok();
    }

    private User getCurrentUser(CustomUserPrincipal principal) {
        return userRepository.findByIdAndDeletedAtIsNull(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found."));
    }
}
