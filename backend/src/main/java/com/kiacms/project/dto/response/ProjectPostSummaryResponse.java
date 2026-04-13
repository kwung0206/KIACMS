package com.kiacms.project.dto.response;

import com.kiacms.project.entity.ProjectPost;
import com.kiacms.project.enums.ContactMethodType;
import com.kiacms.project.enums.ProjectPostStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProjectPostSummaryResponse(
        UUID id,
        String title,
        String description,
        String techStack,
        String durationText,
        ProjectPostStatus status,
        LocalDate recruitUntil,
        UUID ownerId,
        String ownerName,
        ContactMethodType contactMethod,
        String contactValue,
        int positionCount,
        Instant createdAt
) {
    public static ProjectPostSummaryResponse from(ProjectPost post, int positionCount) {
        return new ProjectPostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getDescription(),
                post.getTechStack(),
                post.getDurationText(),
                post.getStatus(),
                post.getRecruitUntil(),
                post.getOwner().getId(),
                post.getOwner().getName(),
                post.getContactMethod(),
                post.getContactValue(),
                positionCount,
                post.getCreatedAt()
        );
    }
}
