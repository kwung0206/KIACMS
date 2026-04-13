package com.kiacms.project.service;

import com.kiacms.global.exception.AccessDeniedBusinessException;
import com.kiacms.global.exception.BusinessException;
import com.kiacms.global.exception.ConflictException;
import com.kiacms.global.exception.ErrorCode;
import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.notification.service.NotificationService;
import com.kiacms.project.dto.request.ApplicationDecisionRequest;
import com.kiacms.project.dto.request.SubmitProjectApplicationRequest;
import com.kiacms.project.dto.response.ProjectApplicationResponse;
import com.kiacms.project.entity.ProjectApplication;
import com.kiacms.project.entity.ProjectPosition;
import com.kiacms.project.entity.ProjectPost;
import com.kiacms.project.enums.ApplicationStatus;
import com.kiacms.project.repository.ProjectApplicationRepository;
import com.kiacms.project.repository.ProjectPositionRepository;
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
public class ProjectApplicationService {

    private final ProjectApplicationRepository projectApplicationRepository;
    private final ProjectPositionRepository projectPositionRepository;
    private final ProjectPostService projectPostService;
    private final ProjectResponseMapper projectResponseMapper;
    private final NotificationService notificationService;

    @Transactional
    public ProjectApplicationResponse submitApplication(UUID positionId, SubmitProjectApplicationRequest request, User student) {
        validateStudent(student);

        ProjectPosition position = projectPositionRepository.findById(positionId)
                .orElseThrow(() -> new ResourceNotFoundException("프로젝트 포지션을 찾을 수 없습니다."));
        ProjectPost post = position.getProjectPost();
        projectPostService.validateProjectPostOpen(post);

        if (post.getOwner().getId().equals(student.getId())) {
            throw new AccessDeniedBusinessException("자신이 작성한 모집글에는 지원할 수 없습니다.");
        }

        if (projectApplicationRepository.findByProjectPositionAndApplicant(position, student).isPresent()) {
            throw new ConflictException("이미 이 포지션에 지원한 상태입니다.");
        }

        ProjectApplication application = ProjectApplication.builder()
                .projectPosition(position)
                .applicant(student)
                .motivation(request.motivation().trim())
                .courseHistory(blankToNull(request.courseHistory()))
                .certifications(blankToNull(request.certifications()))
                .techStack(request.techStack().trim())
                .portfolioUrl(blankToNull(request.portfolioUrl()))
                .selfIntroduction(request.selfIntroduction().trim())
                .build();

        ProjectApplication saved = projectApplicationRepository.save(application);
        notificationService.createProjectApplicationReceivedNotification(saved);
        return projectResponseMapper.toProjectApplication(saved);
    }

    @Transactional(readOnly = true)
    public List<ProjectApplicationResponse> getMyApplications(User student) {
        validateStudent(student);
        return projectApplicationRepository.findAllByApplicantOrderByCreatedAtDesc(student).stream()
                .map(projectResponseMapper::toProjectApplication)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectApplicationResponse getMyApplication(UUID applicationId, User student) {
        validateStudent(student);
        ProjectApplication application = projectApplicationRepository.findByIdAndApplicant(applicationId, student)
                .orElseThrow(() -> new ResourceNotFoundException("지원서를 찾을 수 없습니다."));
        return projectResponseMapper.toProjectApplication(application);
    }

    @Transactional
    public ProjectApplicationResponse updateApplication(
            UUID applicationId,
            SubmitProjectApplicationRequest request,
            User student
    ) {
        validateStudent(student);
        ProjectApplication application = projectApplicationRepository.findByIdAndApplicant(applicationId, student)
                .orElseThrow(() -> new ResourceNotFoundException("지원서를 찾을 수 없습니다."));

        validateEditableApplication(application);

        application.setMotivation(request.motivation().trim());
        application.setCourseHistory(blankToNull(request.courseHistory()));
        application.setCertifications(blankToNull(request.certifications()));
        application.setTechStack(request.techStack().trim());
        application.setPortfolioUrl(blankToNull(request.portfolioUrl()));
        application.setSelfIntroduction(request.selfIntroduction().trim());

        if (application.getStatus() != ApplicationStatus.SUBMITTED) {
            application.setStatus(ApplicationStatus.SUBMITTED);
            application.setDecisionReason(null);
            application.setReviewedBy(null);
            application.setReviewedAt(null);
            application.setWithdrawnAt(null);
        }

        return projectResponseMapper.toProjectApplication(application);
    }

    @Transactional
    public void deleteApplication(UUID applicationId, User student) {
        validateStudent(student);
        ProjectApplication application = projectApplicationRepository.findByIdAndApplicant(applicationId, student)
                .orElseThrow(() -> new ResourceNotFoundException("지원서를 찾을 수 없습니다."));

        validateEditableApplication(application);
        projectApplicationRepository.delete(application);
    }

    @Transactional
    public ProjectApplicationResponse withdrawApplication(UUID applicationId, User student) {
        validateStudent(student);
        ProjectApplication application = projectApplicationRepository.findByIdAndApplicant(applicationId, student)
                .orElseThrow(() -> new ResourceNotFoundException("지원서를 찾을 수 없습니다."));

        if (application.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new ConflictException("제출 상태인 지원서만 철회할 수 있습니다.");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        application.setWithdrawnAt(Instant.now());
        application.setDecisionReason(null);
        return projectResponseMapper.toProjectApplication(application);
    }

    @Transactional
    public ProjectApplicationResponse decideApplication(UUID postId, UUID applicationId, ApplicationDecisionRequest request, User pm) {
        ProjectPost post = projectPostService.getOwnedProjectPost(postId, pm);
        ProjectApplication application = projectApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("지원서를 찾을 수 없습니다."));

        if (!application.getProjectPosition().getProjectPost().getId().equals(post.getId())) {
            throw new AccessDeniedBusinessException("선택한 모집글에 속한 지원서가 아닙니다.");
        }
        if (application.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new ConflictException("제출 상태인 지원서만 검토할 수 있습니다.");
        }

        if (request.status() == ApplicationStatus.ACCEPTED) {
            long acceptedCount = projectApplicationRepository.countByProjectPositionAndStatus(
                    application.getProjectPosition(),
                    ApplicationStatus.ACCEPTED
            );
            if (acceptedCount >= application.getProjectPosition().getCapacity()) {
                throw new ConflictException("이 포지션은 이미 모집이 완료되었습니다.");
            }

            application.setStatus(ApplicationStatus.ACCEPTED);
            application.setDecisionReason(null);
        } else if (request.status() == ApplicationStatus.REJECTED) {
            if (request.rejectionReason() == null || request.rejectionReason().isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "반려 사유를 입력해 주세요.");
            }

            application.setStatus(ApplicationStatus.REJECTED);
            application.setDecisionReason(request.rejectionReason().trim());
        } else {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "검토 상태는 ACCEPTED 또는 REJECTED만 사용할 수 있습니다.");
        }

        application.setReviewedBy(pm);
        application.setReviewedAt(Instant.now());
        notificationService.createProjectApplicationResultNotification(application);
        return projectResponseMapper.toProjectApplication(application);
    }

    private void validateStudent(User user) {
        if (user.getRoleType() != RoleType.STUDENT) {
            throw new AccessDeniedBusinessException("학생만 프로젝트 지원서를 제출할 수 있습니다.");
        }
    }

    private void validateEditableApplication(ProjectApplication application) {
        if (application.getStatus() == ApplicationStatus.ACCEPTED) {
            throw new ConflictException("수락된 지원서는 수정하거나 삭제할 수 없습니다.");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
