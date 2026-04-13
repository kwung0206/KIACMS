package com.kiacms.project.service;

import com.kiacms.global.exception.AccessDeniedBusinessException;
import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.notification.service.NotificationService;
import com.kiacms.project.dto.response.ProjectDeletionHistoryResponse;
import com.kiacms.project.dto.response.ProjectPostSummaryResponse;
import com.kiacms.project.entity.ProjectDeletionHistory;
import com.kiacms.project.entity.ProjectPost;
import com.kiacms.project.enums.ProjectPostStatus;
import com.kiacms.project.repository.ProjectDeletionHistoryRepository;
import com.kiacms.project.repository.ProjectPostRepository;
import com.kiacms.user.entity.User;
import com.kiacms.user.enums.RoleType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RootProjectModerationService {

    private final ProjectPostRepository projectPostRepository;
    private final ProjectDeletionHistoryRepository projectDeletionHistoryRepository;
    private final ProjectResponseMapper projectResponseMapper;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<ProjectPostSummaryResponse> getProjectPosts(User rootUser) {
        validateRoot(rootUser);
        return projectPostRepository.findAllVisibleOrderByCreatedAtDesc().stream()
                .map(projectResponseMapper::toSummary)
                .toList();
    }

    @Transactional
    public ProjectDeletionHistoryResponse deleteProject(UUID postId, String reason, User rootUser) {
        validateRoot(rootUser);
        ProjectPost post = projectPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Project post not found."));

        if (post.getStatus() == ProjectPostStatus.DELETED) {
            throw new ResourceNotFoundException("Project post not found.");
        }

        String normalizedReason = reason.trim();
        post.markDeleted(rootUser, normalizedReason);

        ProjectDeletionHistory history = projectDeletionHistoryRepository.save(
                ProjectDeletionHistory.builder()
                        .projectPostId(post.getId())
                        .projectTitle(post.getTitle())
                        .projectOwnerId(post.getOwner().getId())
                        .projectOwnerName(post.getOwner().getName())
                        .deletedById(rootUser.getId())
                        .deletedByName(rootUser.getName())
                        .reason(normalizedReason)
                        .deletedAt(Instant.now())
                        .build()
        );

        notificationService.createProjectDeletedByRootNotification(post, rootUser, normalizedReason);
        return ProjectDeletionHistoryResponse.from(history);
    }

    @Transactional(readOnly = true)
    public List<ProjectDeletionHistoryResponse> getDeletionHistory(User rootUser) {
        validateRoot(rootUser);
        return projectDeletionHistoryRepository.findAllByOrderByDeletedAtDesc().stream()
                .map(ProjectDeletionHistoryResponse::from)
                .toList();
    }

    private void validateRoot(User user) {
        if (user.getRoleType() != RoleType.ROOT) {
            throw new AccessDeniedBusinessException("Only root administrators can moderate projects.");
        }
    }
}
