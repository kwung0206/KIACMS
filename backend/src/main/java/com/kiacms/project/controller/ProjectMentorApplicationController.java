package com.kiacms.project.controller;

import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.global.response.ApiResponse;
import com.kiacms.global.security.CustomUserPrincipal;
import com.kiacms.project.dto.request.SubmitMentorApplicationRequest;
import com.kiacms.project.dto.response.MentorApplicationResponse;
import com.kiacms.project.service.MentorApplicationService;
import com.kiacms.user.entity.User;
import com.kiacms.user.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectMentorApplicationController {

    private final MentorApplicationService mentorApplicationService;
    private final UserRepository userRepository;

    @PostMapping("/projects/{postId}/mentor-applications")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MENTOR')")
    public ApiResponse<MentorApplicationResponse> submitApplication(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID postId,
            @Valid @RequestBody SubmitMentorApplicationRequest request
    ) {
        return ApiResponse.ok(mentorApplicationService.submitApplication(postId, request, getCurrentUser(principal)));
    }

    @GetMapping("/project-mentor-applications/me")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MENTOR')")
    public ApiResponse<List<MentorApplicationResponse>> getMyApplications(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.ok(mentorApplicationService.getMyApplications(getCurrentUser(principal)));
    }

    @PatchMapping("/project-mentor-applications/{applicationId}/withdraw")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MENTOR')")
    public ApiResponse<MentorApplicationResponse> withdrawApplication(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID applicationId
    ) {
        return ApiResponse.ok(mentorApplicationService.withdrawApplication(applicationId, getCurrentUser(principal)));
    }

    private User getCurrentUser(CustomUserPrincipal principal) {
        return userRepository.findByIdAndDeletedAtIsNull(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found."));
    }
}
