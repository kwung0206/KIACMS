package com.kiacms.project.dto.response;

import java.util.List;

public record ProjectManagementOverviewResponse(
        ProjectPostDetailResponse post,
        List<ProjectApplicationResponse> projectApplications,
        List<MentorApplicationResponse> mentorApplications
) {
}
