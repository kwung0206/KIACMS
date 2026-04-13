package com.kiacms.course.controller;

import com.kiacms.course.dto.response.CourseSessionResponse;
import com.kiacms.course.dto.response.StudentCalendarResponse;
import com.kiacms.course.service.StudentCourseCalendarService;
import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.global.response.ApiResponse;
import com.kiacms.global.security.CustomUserPrincipal;
import com.kiacms.user.entity.User;
import com.kiacms.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentCalendarController {

    private final StudentCourseCalendarService studentCourseCalendarService;
    private final UserRepository userRepository;

    @GetMapping("/calendar")
    public ApiResponse<StudentCalendarResponse> getCalendar(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.ok(studentCourseCalendarService.getCalendar(getCurrentUser(principal), from, to));
    }

    @GetMapping("/sessions/{sessionId}")
    public ApiResponse<CourseSessionResponse> getSessionDetail(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID sessionId
    ) {
        return ApiResponse.ok(studentCourseCalendarService.getSessionDetail(getCurrentUser(principal), sessionId));
    }

    private User getCurrentUser(CustomUserPrincipal principal) {
        return userRepository.findByIdAndDeletedAtIsNull(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found."));
    }
}
