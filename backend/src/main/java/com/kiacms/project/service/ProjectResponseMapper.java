package com.kiacms.project.service;

import com.kiacms.project.dto.response.MentorApplicationResponse;
import com.kiacms.project.dto.response.ProjectApplicationResponse;
import com.kiacms.project.dto.response.ProjectManagementOverviewResponse;
import com.kiacms.project.dto.response.ProjectPositionResponse;
import com.kiacms.project.dto.response.ProjectPostDetailResponse;
import com.kiacms.project.dto.response.ProjectPostSummaryResponse;
import com.kiacms.project.entity.MentorApplication;
import com.kiacms.project.entity.ProjectApplication;
import com.kiacms.project.entity.ProjectPosition;
import com.kiacms.project.entity.ProjectPost;
import com.kiacms.project.enums.ApplicationStatus;
import com.kiacms.project.repository.MentorApplicationRepository;
import com.kiacms.project.repository.ProjectApplicationRepository;
import com.kiacms.project.repository.ProjectPositionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectResponseMapper {

    private final ProjectPositionRepository projectPositionRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final MentorApplicationRepository mentorApplicationRepository;

    public ProjectPostSummaryResponse toSummary(ProjectPost post) {
        int positionCount = projectPositionRepository.findAllByProjectPostOrderByCreatedAtAsc(post).size();
        return ProjectPostSummaryResponse.from(post, positionCount);
    }

    public ProjectPostDetailResponse toDetail(ProjectPost post) {
        List<ProjectPositionResponse> positions = projectPositionRepository.findAllByProjectPostOrderByCreatedAtAsc(post).stream()
                .map(this::toPosition)
                .toList();
        return ProjectPostDetailResponse.from(post, positions);
    }

    public ProjectPositionResponse toPosition(ProjectPosition position) {
        long acceptedCount = projectApplicationRepository.countByProjectPositionAndStatus(position, ApplicationStatus.ACCEPTED);
        return ProjectPositionResponse.from(position, acceptedCount);
    }

    public ProjectApplicationResponse toProjectApplication(ProjectApplication application) {
        return ProjectApplicationResponse.from(application);
    }

    public MentorApplicationResponse toMentorApplication(MentorApplication application) {
        return MentorApplicationResponse.from(application);
    }

    public ProjectManagementOverviewResponse toManagementOverview(ProjectPost post) {
        List<ProjectApplicationResponse> projectApplications = projectApplicationRepository
                .findAllByProjectPosition_ProjectPostOrderByCreatedAtAsc(post).stream()
                .map(this::toProjectApplication)
                .toList();

        List<MentorApplicationResponse> mentorApplications = mentorApplicationRepository
                .findAllByProjectPostOrderByCreatedAtAsc(post).stream()
                .map(this::toMentorApplication)
                .toList();

        return new ProjectManagementOverviewResponse(toDetail(post), projectApplications, mentorApplications);
    }
}
