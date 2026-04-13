package com.kiacms.project.dto.response;

import com.kiacms.project.entity.ProjectDeletionHistory;
import java.time.Instant;
import java.util.UUID;

public record ProjectDeletionHistoryResponse(
        UUID id,
        UUID projectPostId,
        String projectTitle,
        UUID projectOwnerId,
        String projectOwnerName,
        UUID deletedById,
        String deletedByName,
        String reason,
        Instant deletedAt
) {
    public static ProjectDeletionHistoryResponse from(ProjectDeletionHistory history) {
        return new ProjectDeletionHistoryResponse(
                history.getId(),
                history.getProjectPostId(),
                history.getProjectTitle(),
                history.getProjectOwnerId(),
                history.getProjectOwnerName(),
                history.getDeletedById(),
                history.getDeletedByName(),
                history.getReason(),
                history.getDeletedAt()
        );
    }
}
