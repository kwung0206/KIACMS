package com.kiacms.project.dto.response;

import com.kiacms.project.entity.ProjectPost;
import com.kiacms.project.enums.ContactMethodType;
import com.kiacms.project.enums.ProjectPostStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjectPostDetailResponse(
        UUID id,
        UUID ownerId,
        String ownerName,
        String title,
        String description,
        String goal,
        String techStack,
        String durationText,
        ContactMethodType contactMethod,
        String contactValue,
        String pmIntroduction,
        String pmBackground,
        ProjectPostStatus status,
        LocalDate recruitUntil,
        Instant closedAt,
        Instant createdAt,
        List<ProjectPositionResponse> positions
) {
    public static ProjectPostDetailResponse from(ProjectPost post, List<ProjectPositionResponse> positions) {
        return new ProjectPostDetailResponse(
                post.getId(),
                post.getOwner().getId(),
                post.getOwner().getName(),
                post.getTitle(),
                post.getDescription(),
                post.getGoal(),
                post.getTechStack(),
                post.getDurationText(),
                post.getContactMethod(),
                post.getContactValue(),
                post.getPmIntroduction(),
                post.getPmBackground(),
                post.getStatus(),
                post.getRecruitUntil(),
                post.getClosedAt(),
                post.getCreatedAt(),
                positions
        );
    }
}
