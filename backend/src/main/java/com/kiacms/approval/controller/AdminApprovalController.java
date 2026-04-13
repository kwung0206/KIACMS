package com.kiacms.approval.controller;

import com.kiacms.approval.dto.request.RejectUserRequest;
import com.kiacms.approval.dto.response.PendingApprovalUserResponse;
import com.kiacms.approval.dto.response.UserApprovalDecisionResponse;
import com.kiacms.approval.service.AdminApprovalService;
import com.kiacms.global.response.ApiResponse;
import com.kiacms.global.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/approvals")
@RequiredArgsConstructor
public class AdminApprovalController {

    private final AdminApprovalService adminApprovalService;

    @GetMapping("/users/pending")
    public ApiResponse<List<PendingApprovalUserResponse>> getPendingUsers() {
        return ApiResponse.ok(adminApprovalService.getPendingUsers());
    }

    @PostMapping("/users/{userId}/approve")
    public ApiResponse<UserApprovalDecisionResponse> approveUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.ok(adminApprovalService.approveUser(userId, principal.getUserId()));
    }

    @PostMapping("/users/{userId}/reject")
    public ApiResponse<UserApprovalDecisionResponse> rejectUser(
            @PathVariable UUID userId,
            @Valid @RequestBody RejectUserRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.ok(adminApprovalService.rejectUser(userId, principal.getUserId(), request.reason()));
    }
}
