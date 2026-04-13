package com.kiacms.course.controller;

import com.kiacms.course.dto.request.UpdateSessionResourceRequest;
import com.kiacms.course.dto.response.CourseSessionResponse;
import com.kiacms.course.service.InstructorSessionService;
import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.global.response.ApiResponse;
import com.kiacms.global.security.CustomUserPrincipal;
import com.kiacms.user.entity.User;
import com.kiacms.user.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/instructor/sessions")
@RequiredArgsConstructor
public class InstructorSessionController {

    private final InstructorSessionService instructorSessionService;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<List<CourseSessionResponse>> getMySessions(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.ok(instructorSessionService.getAssignedSessions(getCurrentUser(principal)));
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<CourseSessionResponse> getMySessionDetail(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.ok(instructorSessionService.getAssignedSessionDetail(sessionId, getCurrentUser(principal)));
    }

    @PutMapping("/{sessionId}/resource")
    public ApiResponse<CourseSessionResponse> updateSessionResource(
            @PathVariable UUID sessionId,
            @Valid @RequestBody UpdateSessionResourceRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.ok(instructorSessionService.updateSessionResource(sessionId, getCurrentUser(principal), request));
    }

    private User getCurrentUser(CustomUserPrincipal principal) {
        return userRepository.findByIdAndDeletedAtIsNull(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found."));
    }
}
