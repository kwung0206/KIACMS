package com.kiacms.project.service;

import com.kiacms.global.exception.AccessDeniedBusinessException;
import com.kiacms.global.exception.BusinessException;
import com.kiacms.global.exception.ConflictException;
import com.kiacms.global.exception.ErrorCode;
import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.notification.service.NotificationService;
import com.kiacms.project.dto.request.ApplicationDecisionRequest;
import com.kiacms.project.dto.request.SubmitMentorApplicationRequest;
import com.kiacms.project.dto.response.MentorApplicationResponse;
import com.kiacms.project.entity.MentorApplication;
import com.kiacms.project.entity.ProjectPost;
import com.kiacms.project.enums.ApplicationStatus;
import com.kiacms.project.repository.MentorApplicationRepository;
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
public class MentorApplicationService {

    private final MentorApplicationRepository mentorApplicationRepository;
    private final ProjectPostService projectPostService;
    private final ProjectResponseMapper projectResponseMapper;
    private final NotificationService notificationService;

    @Transactional
    public MentorApplicationResponse submitApplication(UUID postId, SubmitMentorApplicationRequest request, User applicant) {
        validateMentorApplicant(applicant);

        ProjectPost post = projectPostService.getProjectPost(postId);
        projectPostService.validateProjectPostOpen(post);

        if (post.getOwner().getId().equals(applicant.getId())) {
            throw new AccessDeniedBusinessException("자신이 작성한 모집글에는 지원 요청을 보낼 수 없습니다.");
        }

        if (mentorApplicationRepository.findByProjectPostAndApplicant(post, applicant).isPresent()) {
            throw new ConflictException("이미 이 프로젝트에 지원 요청을 제출한 상태입니다.");
        }

        MentorApplication application = MentorApplication.builder()
                .projectPost(post)
                .applicant(applicant)
                .expertiseSummary(request.expertiseSummary().trim())
                .mentoringExperience(blankToNull(request.mentoringExperience()))
                .portfolioUrl(blankToNull(request.portfolioUrl()))
                .supportPlan(request.supportPlan().trim())
                .build();

        MentorApplication saved = mentorApplicationRepository.save(application);
        notificationService.createMentorApplicationReceivedNotification(saved);
        return projectResponseMapper.toMentorApplication(saved);
    }

    @Transactional(readOnly = true)
    public List<MentorApplicationResponse> getMyApplications(User applicant) {
        validateMentorApplicant(applicant);
        return mentorApplicationRepository.findAllByApplicantOrderByCreatedAtDesc(applicant).stream()
                .map(projectResponseMapper::toMentorApplication)
                .toList();
    }

    @Transactional
    public MentorApplicationResponse withdrawApplication(UUID applicationId, User applicant) {
        validateMentorApplicant(applicant);
        MentorApplication application = mentorApplicationRepository.findByIdAndApplicant(applicationId, applicant)
                .orElseThrow(() -> new ResourceNotFoundException("지원 요청을 찾을 수 없습니다."));

        if (application.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new ConflictException("제출 상태인 지원 요청만 철회할 수 있습니다.");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        application.setWithdrawnAt(Instant.now());
        application.setDecisionReason(null);
        return projectResponseMapper.toMentorApplication(application);
    }

    @Transactional
    public MentorApplicationResponse decideApplication(UUID postId, UUID applicationId, ApplicationDecisionRequest request, User pm) {
        ProjectPost post = projectPostService.getOwnedProjectPost(postId, pm);
        MentorApplication application = mentorApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("지원 요청을 찾을 수 없습니다."));

        if (!application.getProjectPost().getId().equals(post.getId())) {
            throw new AccessDeniedBusinessException("선택한 모집글에 속한 지원 요청이 아닙니다.");
        }
        if (application.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new ConflictException("제출 상태인 지원 요청만 검토할 수 있습니다.");
        }

        if (request.status() == ApplicationStatus.ACCEPTED) {
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
        notificationService.createMentorApplicationResultNotification(application);
        return projectResponseMapper.toMentorApplication(application);
    }

    private void validateMentorApplicant(User user) {
        if (user.getRoleType() != RoleType.INSTRUCTOR && user.getRoleType() != RoleType.MENTOR) {
            throw new AccessDeniedBusinessException("강사 또는 멘토 권한 사용자만 지원 요청을 제출할 수 있습니다.");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
