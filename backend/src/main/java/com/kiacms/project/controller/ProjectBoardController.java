package com.kiacms.project.controller;

import com.kiacms.global.response.ApiResponse;
import com.kiacms.project.dto.response.ProjectPostDetailResponse;
import com.kiacms.project.dto.response.ProjectPostSummaryResponse;
import com.kiacms.project.enums.ProjectPostStatus;
import com.kiacms.project.service.ProjectPostService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectBoardController {

    private final ProjectPostService projectPostService;

    @GetMapping
    public ApiResponse<List<ProjectPostSummaryResponse>> getProjectBoard(
            @RequestParam(required = false) ProjectPostStatus status
    ) {
        return ApiResponse.ok(projectPostService.getProjectBoard(status));
    }

    @GetMapping("/{postId}")
    public ApiResponse<ProjectPostDetailResponse> getProjectPostDetail(
            @PathVariable UUID postId
    ) {
        return ApiResponse.ok(projectPostService.getProjectPostDetail(postId));
    }
}
