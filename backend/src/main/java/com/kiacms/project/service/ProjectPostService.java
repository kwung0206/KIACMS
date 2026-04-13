package com.kiacms.project.service;

import com.kiacms.global.exception.AccessDeniedBusinessException;
import com.kiacms.global.exception.BusinessException;
import com.kiacms.global.exception.ConflictException;
import com.kiacms.global.exception.ErrorCode;
import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.project.dto.request.CreateProjectPostRequest;
import com.kiacms.project.dto.response.ProjectManagementOverviewResponse;
import com.kiacms.project.dto.response.ProjectPostDetailResponse;
import com.kiacms.project.dto.response.ProjectPostSummaryResponse;
import com.kiacms.project.entity.ProjectPosition;
import com.kiacms.project.entity.ProjectPost;
import com.kiacms.project.enums.ProjectPostStatus;
import com.kiacms.project.repository.ProjectPositionRepository;
import com.kiacms.project.repository.ProjectPostRepository;
import com.kiacms.user.entity.User;
import com.kiacms.user.enums.RoleType;
import java.util.HashSet;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectPostService {

    private final ProjectPostRepository projectPostRepository;
    private final ProjectPositionRepository projectPositionRepository;
    private final ProjectResponseMapper projectResponseMapper;

    @Transactional(readOnly = true)
    public List<ProjectPostSummaryResponse> getProjectBoard(ProjectPostStatus status) {
        List<ProjectPost> posts = status == null
                ? projectPostRepository.findAllVisibleOrderByCreatedAtDesc()
                : projectPostRepository.findAllVisibleByStatusOrderByCreatedAtDesc(status);

        return posts.stream()
                .map(projectResponseMapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectPostDetailResponse getProjectPostDetail(UUID postId) {
        ProjectPost post = getProjectPost(postId);
        if (post.getStatus() == ProjectPostStatus.DELETED) {
            throw new ResourceNotFoundException("프로젝트 모집글을 찾을 수 없습니다.");
        }
        return projectResponseMapper.toDetail(post);
    }

    @Transactional
    public ProjectPostDetailResponse createProjectPost(CreateProjectPostRequest request, User student) {
        validatePm(student);
        validatePositionNames(request);

        ProjectPost post = ProjectPost.builder()
                .owner(student)
                .title(request.title().trim())
                .description(request.description().trim())
                .goal(request.goal().trim())
                .techStack(request.techStack().trim())
                .durationText(request.durationText().trim())
                .contactMethod(request.contactMethod())
                .contactValue(request.contactValue().trim())
                .pmIntroduction(request.pmIntroduction().trim())
                .pmBackground(request.pmBackground().trim())
                .recruitUntil(request.recruitUntil())
                .build();

        ProjectPost savedPost = projectPostRepository.save(post);

        List<ProjectPosition> positions = request.positions().stream()
                .map(positionRequest -> ProjectPosition.builder()
                        .projectPost(savedPost)
                        .name(positionRequest.name().trim())
                        .description(blankToNull(positionRequest.description()))
                        .requiredSkills(blankToNull(positionRequest.requiredSkills()))
                        .capacity(positionRequest.capacity())
                        .build())
                .toList();
        projectPositionRepository.saveAll(positions);

        return projectResponseMapper.toDetail(savedPost);
    }

    @Transactional(readOnly = true)
    public List<ProjectPostSummaryResponse> getMyProjectPosts(User student) {
        validatePm(student);
        return projectPostRepository.findAllVisibleByOwnerOrderByCreatedAtDesc(student).stream()
                .map(projectResponseMapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectManagementOverviewResponse getManagementOverview(UUID postId, User student) {
        ProjectPost post = getOwnedProjectPost(postId, student);
        return projectResponseMapper.toManagementOverview(post);
    }

    @Transactional
    public void deleteOwnedProjectPost(UUID postId, User student) {
        ProjectPost post = getOwnedProjectPost(postId, student);

        if (post.getStatus() == ProjectPostStatus.DELETED) {
            throw new ResourceNotFoundException("프로젝트 모집글을 찾을 수 없습니다.");
        }

        post.setStatus(ProjectPostStatus.DELETED);
        post.setDeletedAt(Instant.now());
        post.setDeletedById(student.getId());
        post.setDeletionReason("작성자가 모집글을 삭제했습니다.");
    }

    public ProjectPost getProjectPost(UUID postId) {
        return projectPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("프로젝트 모집글을 찾을 수 없습니다."));
    }

    public ProjectPost getOwnedProjectPost(UUID postId, User student) {
        ProjectPost post = getProjectPost(postId);
        validatePm(student);
        if (!post.getOwner().getId().equals(student.getId())) {
            throw new AccessDeniedBusinessException("모집글 작성자만 이 프로젝트를 관리할 수 있습니다.");
        }
        return post;
    }

    public void validateProjectPostOpen(ProjectPost post) {
        if (post.getStatus() != ProjectPostStatus.OPEN) {
            throw new ConflictException("현재 이 모집글은 지원을 받고 있지 않습니다.");
        }

        if (post.getRecruitUntil() != null && post.getRecruitUntil().isBefore(java.time.LocalDate.now())) {
            throw new ConflictException("모집 기간이 이미 종료되었습니다.");
        }
    }

    private void validatePm(User student) {
        if (student.getRoleType() != RoleType.STUDENT) {
            throw new AccessDeniedBusinessException("학생만 모집글을 작성하고 관리할 수 있습니다.");
        }
    }

    private void validatePositionNames(CreateProjectPostRequest request) {
        Set<String> names = new HashSet<>();
        for (var position : request.positions()) {
            String normalized = position.name().trim().toLowerCase();
            if (!names.add(normalized)) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "한 모집글 안에서는 포지션명이 서로 달라야 합니다.");
            }
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
