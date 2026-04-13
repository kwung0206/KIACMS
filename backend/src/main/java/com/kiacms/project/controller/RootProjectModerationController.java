package com.kiacms.project.controller;

import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.global.response.ApiResponse;
import com.kiacms.global.security.CustomUserPrincipal;
import com.kiacms.project.dto.request.DeleteProjectRequest;
import com.kiacms.project.dto.response.ProjectDeletionHistoryResponse;
import com.kiacms.project.dto.response.ProjectPostSummaryResponse;
import com.kiacms.project.service.RootProjectModerationService;
import com.kiacms.user.entity.User;
import com.kiacms.user.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/root/projects")
@RequiredArgsConstructor
public class RootProjectModerationController {

    private final RootProjectModerationService rootProjectModerationService;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<List<ProjectPostSummaryResponse>> getProjectPosts(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.ok(rootProjectModerationService.getProjectPosts(getCurrentUser(principal)));
    }

    @GetMapping("/deletions")
    public ApiResponse<List<ProjectDeletionHistoryResponse>> getDeletionHistory(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.ok(rootProjectModerationService.getDeletionHistory(getCurrentUser(principal)));
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<ProjectDeletionHistoryResponse> deleteProject(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID postId,
            @Valid @RequestBody DeleteProjectRequest request
    ) {
        return ApiResponse.ok(
                rootProjectModerationService.deleteProject(postId, request.reason(), getCurrentUser(principal))
        );
    }

    private User getCurrentUser(CustomUserPrincipal principal) {
        return userRepository.findByIdAndDeletedAtIsNull(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found."));
    }
}
