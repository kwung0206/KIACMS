package com.kiacms.project.dto.response;

import com.kiacms.project.entity.ProjectApplication;
import com.kiacms.project.enums.ApplicationStatus;
import java.time.Instant;
import java.util.UUID;

public record ProjectApplicationResponse(
        UUID id,
        UUID projectPostId,
        String projectPostTitle,
        UUID projectPositionId,
        String projectPositionName,
        UUID applicantId,
        String applicantName,
        String motivation,
        String courseHistory,
        String certifications,
        String techStack,
        String portfolioUrl,
        String selfIntroduction,
        ApplicationStatus status,
        String decisionReason,
        UUID reviewedById,
        String reviewedByName,
        Instant reviewedAt,
        Instant withdrawnAt,
        Instant createdAt
) {
    public static ProjectApplicationResponse from(ProjectApplication application) {
        return new ProjectApplicationResponse(
                application.getId(),
                application.getProjectPosition().getProjectPost().getId(),
                application.getProjectPosition().getProjectPost().getTitle(),
                application.getProjectPosition().getId(),
                application.getProjectPosition().getName(),
                application.getApplicant().getId(),
                application.getApplicant().getName(),
                application.getMotivation(),
                application.getCourseHistory(),
                application.getCertifications(),
                application.getTechStack(),
                application.getPortfolioUrl(),
                application.getSelfIntroduction(),
                application.getStatus(),
                application.getDecisionReason(),
                application.getReviewedBy() == null ? null : application.getReviewedBy().getId(),
                application.getReviewedBy() == null ? null : application.getReviewedBy().getName(),
                application.getReviewedAt(),
                application.getWithdrawnAt(),
                application.getCreatedAt()
        );
    }
}
