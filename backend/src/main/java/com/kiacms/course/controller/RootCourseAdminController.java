package com.kiacms.course.controller;

import com.kiacms.course.dto.request.CreateCourseRequest;
import com.kiacms.course.dto.request.CreateCourseSessionRequest;
import com.kiacms.course.dto.request.UpdateCourseSessionRequest;
import com.kiacms.course.dto.response.InstructorOptionResponse;
import com.kiacms.course.dto.response.StudentCalendarResponse;
import com.kiacms.course.dto.response.CourseResponse;
import com.kiacms.course.dto.response.CourseSessionResponse;
import com.kiacms.course.service.CourseManagementService;
import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.global.response.ApiResponse;
import com.kiacms.global.security.CustomUserPrincipal;
import com.kiacms.user.entity.User;
import com.kiacms.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/root/courses")
@RequiredArgsConstructor
public class RootCourseAdminController {

    private final CourseManagementService courseManagementService;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<List<CourseResponse>> getCourses(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.ok(courseManagementService.getCourses(getCurrentUser(principal)));
    }

    @GetMapping("/instructors")
    public ApiResponse<List<InstructorOptionResponse>> getApprovedInstructors(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.ok(courseManagementService.getApprovedInstructors(getCurrentUser(principal)));
    }

    @GetMapping("/sessions/calendar")
    public ApiResponse<StudentCalendarResponse> getSessionCalendar(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.ok(courseManagementService.getSessionCalendar(getCurrentUser(principal), from, to));
    }

    @PostMapping
    public ApiResponse<CourseResponse> createCourse(
            @Valid @RequestBody CreateCourseRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.ok(courseManagementService.createCourse(request, getCurrentUser(principal)));
    }

    @PostMapping("/{courseId}/sessions")
    public ApiResponse<CourseSessionResponse> createCourseSession(
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateCourseSessionRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.ok(courseManagementService.createCourseSession(request, courseId, getCurrentUser(principal)));
    }

    @PutMapping("/sessions/{sessionId}")
    public ApiResponse<CourseSessionResponse> updateCourseSession(
            @PathVariable UUID sessionId,
            @Valid @RequestBody UpdateCourseSessionRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.ok(courseManagementService.updateCourseSession(request, sessionId, getCurrentUser(principal)));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<Void> deleteCourseSession(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        courseManagementService.deleteCourseSession(sessionId, getCurrentUser(principal));
        return ApiResponse.ok(null);
    }

    private User getCurrentUser(CustomUserPrincipal principal) {
        return userRepository.findByIdAndDeletedAtIsNull(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found."));
    }
}
