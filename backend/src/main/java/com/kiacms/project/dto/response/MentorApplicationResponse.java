package com.kiacms.project.dto.response;

import com.kiacms.project.entity.MentorApplication;
import com.kiacms.project.enums.ApplicationStatus;
import java.time.Instant;
import java.util.UUID;

public record MentorApplicationResponse(
        UUID id,
        UUID projectPostId,
        String projectPostTitle,
        UUID applicantId,
        String applicantName,
        String expertiseSummary,
        String mentoringExperience,
        String portfolioUrl,
        String supportPlan,
        ApplicationStatus status,
        String decisionReason,
        UUID reviewedById,
        String reviewedByName,
        Instant reviewedAt,
        Instant withdrawnAt,
        Instant createdAt
) {
    public static MentorApplicationResponse from(MentorApplication application) {
        return new MentorApplicationResponse(
                application.getId(),
                application.getProjectPost().getId(),
                application.getProjectPost().getTitle(),
                application.getApplicant().getId(),
                application.getApplicant().getName(),
                application.getExpertiseSummary(),
                application.getMentoringExperience(),
                application.getPortfolioUrl(),
                application.getSupportPlan(),
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
