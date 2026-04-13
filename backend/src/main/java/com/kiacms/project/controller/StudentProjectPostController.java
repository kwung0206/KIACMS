package com.kiacms.project.controller;

import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.global.response.ApiResponse;
import com.kiacms.global.security.CustomUserPrincipal;
import com.kiacms.project.dto.request.ApplicationDecisionRequest;
import com.kiacms.project.dto.request.CreateProjectPostRequest;
import com.kiacms.project.dto.response.ProjectApplicationResponse;
import com.kiacms.project.dto.response.ProjectManagementOverviewResponse;
import com.kiacms.project.dto.response.ProjectPostDetailResponse;
import com.kiacms.project.dto.response.ProjectPostSummaryResponse;
import com.kiacms.project.dto.response.MentorApplicationResponse;
import com.kiacms.project.service.MentorApplicationService;
import com.kiacms.project.service.ProjectApplicationService;
import com.kiacms.project.service.ProjectPostService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/projects")
@RequiredArgsConstructor
public class StudentProjectPostController {

    private final ProjectPostService projectPostService;
    private final ProjectApplicationService projectApplicationService;
    private final MentorApplicationService mentorApplicationService;
    private final UserRepository userRepository;

    @PostMapping
    public ApiResponse<ProjectPostDetailResponse> createProjectPost(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CreateProjectPostRequest request
    ) {
        return ApiResponse.ok(projectPostService.createProjectPost(request, getCurrentUser(principal)));
    }

    @GetMapping("/me")
    public ApiResponse<List<ProjectPostSummaryResponse>> getMyProjectPosts(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.ok(projectPostService.getMyProjectPosts(getCurrentUser(principal)));
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deleteMyProjectPost(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID postId
    ) {
        projectPostService.deleteOwnedProjectPost(postId, getCurrentUser(principal));
        return ApiResponse.ok();
    }

    @GetMapping("/{postId}/manage")
    public ApiResponse<ProjectManagementOverviewResponse> getManagementOverview(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID postId
    ) {
        return ApiResponse.ok(projectPostService.getManagementOverview(postId, getCurrentUser(principal)));
    }

    @PatchMapping("/{postId}/applications/{applicationId}/decision")
    public ApiResponse<ProjectApplicationResponse> decideProjectApplication(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID postId,
            @PathVariable UUID applicationId,
            @Valid @RequestBody ApplicationDecisionRequest request
    ) {
        return ApiResponse.ok(projectApplicationService.decideApplication(postId, applicationId, request, getCurrentUser(principal)));
    }

    @PatchMapping("/{postId}/mentor-applications/{applicationId}/decision")
    public ApiResponse<MentorApplicationResponse> decideMentorApplication(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID postId,
            @PathVariable UUID applicationId,
            @Valid @RequestBody ApplicationDecisionRequest request
    ) {
        return ApiResponse.ok(mentorApplicationService.decideApplication(postId, applicationId, request, getCurrentUser(principal)));
    }

    private User getCurrentUser(CustomUserPrincipal principal) {
        return userRepository.findByIdAndDeletedAtIsNull(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found."));
    }
}
