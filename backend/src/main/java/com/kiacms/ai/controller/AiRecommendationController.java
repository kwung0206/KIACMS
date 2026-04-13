package com.kiacms.ai.controller;

import com.kiacms.ai.dto.request.CareerCourseRecommendationRequest;
import com.kiacms.ai.dto.request.SimilarProjectRecommendationRequest;
import com.kiacms.ai.dto.response.CareerCourseRecommendationResponse;
import com.kiacms.ai.dto.response.SimilarProjectRecommendationResponse;
import com.kiacms.ai.service.CareerCourseRecommendationAiService;
import com.kiacms.ai.service.SimilarProjectRecommendationAiService;
import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.global.response.ApiResponse;
import com.kiacms.global.security.CustomUserPrincipal;
import com.kiacms.user.entity.User;
import com.kiacms.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiRecommendationController {

    private final CareerCourseRecommendationAiService careerCourseRecommendationAiService;
    private final SimilarProjectRecommendationAiService similarProjectRecommendationAiService;
    private final UserRepository userRepository;

    @PostMapping("/career-course-recommendations")
    public ApiResponse<CareerCourseRecommendationResponse> recommendCourses(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CareerCourseRecommendationRequest request
    ) {
        return ApiResponse.ok(careerCourseRecommendationAiService.recommendCourses(request, getCurrentUser(principal)));
    }

    @PostMapping("/similar-project-recommendations")
    public ApiResponse<SimilarProjectRecommendationResponse> recommendSimilarProjects(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody SimilarProjectRecommendationRequest request
    ) {
        return ApiResponse.ok(similarProjectRecommendationAiService.recommendProjects(request, getCurrentUser(principal)));
    }

    private User getCurrentUser(CustomUserPrincipal principal) {
        return userRepository.findByIdAndDeletedAtIsNull(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found."));
    }
}
