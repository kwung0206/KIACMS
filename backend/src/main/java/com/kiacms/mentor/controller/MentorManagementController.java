package com.kiacms.mentor.controller;

import com.kiacms.course.dto.response.CourseResponse;
import com.kiacms.course.dto.response.EnrollmentResponse;
import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.global.response.ApiResponse;
import com.kiacms.global.security.CustomUserPrincipal;
import com.kiacms.mentor.dto.request.AssignManagedStudentRequest;
import com.kiacms.mentor.dto.request.MentorCourseAssignmentRequest;
import com.kiacms.mentor.dto.response.MentorManagedStudentResponse;
import com.kiacms.mentor.dto.response.MentorStudentOptionResponse;
import com.kiacms.mentor.service.MentorManagementService;
import com.kiacms.user.entity.User;
import com.kiacms.user.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mentor")
@RequiredArgsConstructor
public class MentorManagementController {

    private final MentorManagementService mentorManagementService;
    private final UserRepository userRepository;

    @GetMapping("/students")
    public ApiResponse<List<MentorStudentOptionResponse>> searchStudents(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(mentorManagementService.searchStudents(getCurrentUser(principal), keyword));
    }

    @GetMapping("/managed-students")
    public ApiResponse<List<MentorManagedStudentResponse>> getManagedStudents(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.ok(mentorManagementService.getManagedStudents(getCurrentUser(principal)));
    }

    @GetMapping("/courses")
    public ApiResponse<List<CourseResponse>> getAvailableCourses(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.ok(mentorManagementService.getAvailableCourses(getCurrentUser(principal)));
    }

    @PostMapping("/student-mappings")
    public ApiResponse<MentorManagedStudentResponse> assignStudent(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody AssignManagedStudentRequest request
    ) {
        return ApiResponse.ok(mentorManagementService.assignStudent(getCurrentUser(principal), request));
    }

    @PatchMapping("/student-mappings/{mappingId}/end")
    public ApiResponse<MentorManagedStudentResponse> endStudentMapping(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID mappingId
    ) {
        return ApiResponse.ok(mentorManagementService.endStudentMapping(getCurrentUser(principal), mappingId));
    }

    @PostMapping("/student-mappings/{mappingId}/course-enrollments")
    public ApiResponse<EnrollmentResponse> assignCourseToStudent(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID mappingId,
            @Valid @RequestBody MentorCourseAssignmentRequest request
    ) {
        return ApiResponse.ok(
                mentorManagementService.assignCourseToManagedStudent(getCurrentUser(principal), mappingId, request.courseId())
        );
    }

    @DeleteMapping("/student-mappings/{mappingId}/course-enrollments/{courseId}")
    public ApiResponse<EnrollmentResponse> dropCourseFromStudent(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID mappingId,
            @PathVariable UUID courseId
    ) {
        return ApiResponse.ok(
                mentorManagementService.dropCourseFromManagedStudent(getCurrentUser(principal), mappingId, courseId)
        );
    }

    private User getCurrentUser(CustomUserPrincipal principal) {
        return userRepository.findByIdAndDeletedAtIsNull(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found."));
    }
}
