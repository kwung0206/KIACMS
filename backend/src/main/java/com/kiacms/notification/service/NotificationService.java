package com.kiacms.notification.service;

import com.kiacms.course.entity.Course;
import com.kiacms.course.entity.CourseSession;
import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.note.entity.Note;
import com.kiacms.notification.dto.response.NotificationResponse;
import com.kiacms.notification.dto.response.UnreadNotificationCountResponse;
import com.kiacms.notification.entity.Notification;
import com.kiacms.notification.enums.NotificationTargetType;
import com.kiacms.notification.enums.NotificationType;
import com.kiacms.notification.repository.NotificationRepository;
import com.kiacms.project.entity.MentorApplication;
import com.kiacms.project.entity.ProjectApplication;
import com.kiacms.project.entity.ProjectPost;
import com.kiacms.user.entity.User;
import com.kiacms.user.enums.UserStatus;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createApprovalResultNotification(User user) {
        String message = user.getStatus() == UserStatus.APPROVED
                ? "회원가입 승인이 완료되었습니다. 이제 서비스를 이용할 수 있습니다."
                : "회원가입 신청이 반려되었습니다. 사유를 확인해 주세요.";

        createNotification(
                user,
                NotificationType.APPROVAL_RESULT,
                "회원 승인 결과",
                user.getAccountStatusReason() == null || user.getAccountStatusReason().isBlank()
                        ? message
                        : "%s 사유: %s".formatted(message, user.getAccountStatusReason()),
                NotificationTargetType.USER_PROFILE,
                user.getId(),
                "/me"
        );
    }

    @Transactional
    public void createSessionZoomUpdatedNotification(User recipient, CourseSession session) {
        createNotification(
                recipient,
                NotificationType.SESSION_ZOOM_UPDATED,
                "수업 Zoom 링크 등록",
                "\"%s\" %d회차 Zoom 링크가 등록되었습니다."
                        .formatted(session.getCourse().getTitle(), session.getSessionOrder()),
                NotificationTargetType.COURSE_SESSION,
                session.getId(),
                "/student/sessions/" + session.getId()
        );
    }

    @Transactional
    public void createSessionRecordingUpdatedNotification(User recipient, CourseSession session) {
        createNotification(
                recipient,
                NotificationType.SESSION_RECORDING_UPDATED,
                "녹화본 링크 등록",
                "\"%s\" %d회차 녹화본 링크가 등록되었습니다."
                        .formatted(session.getCourse().getTitle(), session.getSessionOrder()),
                NotificationTargetType.COURSE_SESSION,
                session.getId(),
                "/student/sessions/" + session.getId()
        );
    }

    @Transactional
    public void createSessionSummaryUpdatedNotification(User recipient, CourseSession session) {
        createNotification(
                recipient,
                NotificationType.SESSION_SUMMARY_UPDATED,
                "수업 정리 링크 등록",
                "\"%s\" %d회차 정리 링크가 등록되었습니다."
                        .formatted(session.getCourse().getTitle(), session.getSessionOrder()),
                NotificationTargetType.COURSE_SESSION,
                session.getId(),
                "/student/sessions/" + session.getId()
        );
    }

    @Transactional
    public void createNoteTaggedNotification(Note note, User recipient, User taggedBy) {
        createNotification(
                recipient,
                NotificationType.NOTE_TAGGED,
                "새 정리글 태그",
                "%s 학생이 \"%s\" 정리글에서 회원님을 태그했습니다."
                        .formatted(taggedBy.getName(), note.getTitle()),
                NotificationTargetType.NOTE,
                note.getId(),
                "/instructor/tagged-notes/" + note.getId()
        );
    }

    @Transactional
    public void createNoteCommentedNotification(Note note, User recipient, User commenter) {
        if (recipient.getId().equals(commenter.getId())) {
            return;
        }

        createNotification(
                recipient,
                NotificationType.NOTE_COMMENTED,
                "정리글 코멘트 도착",
                "%s 강사가 \"%s\" 정리글에 코멘트를 남겼습니다."
                        .formatted(commenter.getName(), note.getTitle()),
                NotificationTargetType.NOTE,
                note.getId(),
                "/student/notes/" + note.getId()
        );
    }

    @Transactional
    public void createProjectApplicationReceivedNotification(ProjectApplication application) {
        createNotification(
                application.getProjectPosition().getProjectPost().getOwner(),
                NotificationType.PROJECT_APPLICATION_RECEIVED,
                "새 프로젝트 지원서 도착",
                "%s 님이 \"%s\" 프로젝트의 %s 포지션에 지원했습니다."
                        .formatted(
                                application.getApplicant().getName(),
                                application.getProjectPosition().getProjectPost().getTitle(),
                                application.getProjectPosition().getName()
                        ),
                NotificationTargetType.PROJECT_APPLICATION,
                application.getId(),
                "/student/projects/" + application.getProjectPosition().getProjectPost().getId() + "/manage"
        );
    }

    @Transactional
    public void createProjectApplicationResultNotification(ProjectApplication application) {
        createNotification(
                application.getApplicant(),
                NotificationType.PROJECT_APPLICATION_RESULT,
                "프로젝트 지원 결과",
                "\"%s\" 프로젝트 %s 포지션 지원 결과가 %s 처리되었습니다."
                        .formatted(
                                application.getProjectPosition().getProjectPost().getTitle(),
                                application.getProjectPosition().getName(),
                                application.getStatus().name()
                        ),
                NotificationTargetType.PROJECT_APPLICATION,
                application.getId(),
                "/student/project-applications/me"
        );
    }

    @Transactional
    public void createMentorApplicationReceivedNotification(MentorApplication application) {
        createNotification(
                application.getProjectPost().getOwner(),
                NotificationType.MENTOR_APPLICATION_RECEIVED,
                "새 멘토 지원 도착",
                "%s 님이 \"%s\" 프로젝트에 멘토로 지원했습니다."
                        .formatted(application.getApplicant().getName(), application.getProjectPost().getTitle()),
                NotificationTargetType.MENTOR_APPLICATION,
                application.getId(),
                "/student/projects/" + application.getProjectPost().getId() + "/manage"
        );
    }

    @Transactional
    public void createMentorApplicationResultNotification(MentorApplication application) {
        createNotification(
                application.getApplicant(),
                NotificationType.MENTOR_APPLICATION_RESULT,
                "멘토 지원 결과",
                "\"%s\" 프로젝트 멘토 지원 결과가 %s 처리되었습니다."
                        .formatted(application.getProjectPost().getTitle(), application.getStatus().name()),
                NotificationTargetType.MENTOR_APPLICATION,
                application.getId(),
                "/project-mentor-applications/me"
        );
    }

    @Transactional
    public void createProjectDeletedByRootNotification(com.kiacms.project.entity.ProjectPost post, User rootUser, String reason) {
        createNotification(
                post.getOwner(),
                NotificationType.PROJECT_DELETED_BY_ROOT,
                "프로젝트 삭제 안내",
                "Root 관리자 %s 님이 \"%s\" 프로젝트를 삭제했습니다. 사유: %s"
                        .formatted(rootUser.getName(), post.getTitle(), reason),
                NotificationTargetType.PROJECT_POST,
                post.getId(),
                "/notifications"
        );
    }

    @Transactional
    public void createProjectAiRecommendedNotification(
            User recipient,
            ProjectPost post,
            Course matchedCourse,
            List<String> keywords,
            String notificationMessage
    ) {
        String keywordText = keywords == null || keywords.isEmpty()
                ? ""
                : " 주요 키워드: %s".formatted(String.join(", ", keywords));

        String defaultMessage = "\"%s\" 프로젝트가 현재 수강 중인 \"%s\" 과정과 잘 맞아 보여 추천드립니다.%s"
                .formatted(post.getTitle(), matchedCourse.getTitle(), keywordText);

        createNotification(
                recipient,
                NotificationType.PROJECT_AI_RECOMMENDED,
                "AI 프로젝트 추천",
                notificationMessage == null || notificationMessage.isBlank()
                        ? defaultMessage
                        : "\"%s\" 프로젝트 추천: %s (%s 과정 기준 추천)"
                                .formatted(post.getTitle(), notificationMessage, matchedCourse.getTitle()),
                NotificationTargetType.PROJECT_POST,
                post.getId(),
                "/projects/" + post.getId()
        );
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(User user, boolean unreadOnly) {
        List<Notification> notifications = unreadOnly
                ? notificationRepository.findAllByRecipientAndIsReadFalseOrderByCreatedAtDesc(user)
                : notificationRepository.findAllByRecipientOrderByCreatedAtDesc(user);

        return notifications.stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UnreadNotificationCountResponse getUnreadCount(User user) {
        return new UnreadNotificationCountResponse(notificationRepository.countByRecipientAndIsReadFalse(user));
    }

    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, User user) {
        Notification notification = notificationRepository.findByIdAndRecipient(notificationId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found."));
        notification.markAsRead();
        return NotificationResponse.from(notification);
    }

    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.findAllByRecipientAndIsReadFalseOrderByCreatedAtDesc(user).stream()
                .forEach(Notification::markAsRead);
    }

    @Transactional
    public void deleteNotification(UUID notificationId, User user) {
        Notification notification = notificationRepository.findByIdAndRecipient(notificationId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found."));
        notificationRepository.delete(notification);
    }

    @Transactional
    public void deleteAllNotifications(User user) {
        notificationRepository.deleteAllByRecipient(user);
    }

    private void createNotification(
            User recipient,
            NotificationType type,
            String title,
            String message,
            NotificationTargetType targetType,
            UUID targetId,
            String targetUrl
    ) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .message(message)
                .targetType(targetType)
                .targetId(targetId)
                .targetUrl(targetUrl)
                .build();
        notificationRepository.save(notification);
    }
}
