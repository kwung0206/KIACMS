package com.kiacms.course.service;

import com.kiacms.course.dto.request.UpdateSessionResourceRequest;
import com.kiacms.course.dto.response.CourseSessionResponse;
import com.kiacms.course.dto.response.SessionResourceResponse;
import com.kiacms.course.entity.CourseSession;
import com.kiacms.course.entity.Enrollment;
import com.kiacms.course.entity.SessionResource;
import com.kiacms.course.enums.EnrollmentStatus;
import com.kiacms.course.repository.CourseSessionRepository;
import com.kiacms.course.repository.EnrollmentRepository;
import com.kiacms.course.repository.SessionResourceRepository;
import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.notification.service.NotificationService;
import com.kiacms.user.entity.User;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InstructorSessionService {

    private final CourseSessionRepository courseSessionRepository;
    private final SessionResourceRepository sessionResourceRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<CourseSessionResponse> getAssignedSessions(User instructor) {
        return courseSessionRepository.findAllByInstructorOrderBySessionDateAscStartTimeAsc(instructor).stream()
                .map(this::toSessionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseSessionResponse getAssignedSessionDetail(java.util.UUID sessionId, User instructor) {
        CourseSession session = courseSessionRepository.findByIdAndInstructor(sessionId, instructor)
                .orElseThrow(() -> new ResourceNotFoundException("Assigned session not found."));
        return toSessionResponse(session);
    }

    @Transactional
    public CourseSessionResponse updateSessionResource(java.util.UUID sessionId, User instructor, UpdateSessionResourceRequest request) {
        CourseSession session = courseSessionRepository.findByIdAndInstructor(sessionId, instructor)
                .orElseThrow(() -> new ResourceNotFoundException("Assigned session not found."));

        SessionResource resource = sessionResourceRepository.findByCourseSession(session)
                .orElseGet(() -> SessionResource.builder().courseSession(session).build());
        String beforeZoomLink = resource.getZoomLink();
        String beforeRecordingLink = resource.getRecordingLink();
        String beforeSummaryLink = resource.getSummaryLink();

        Instant now = Instant.now();
        if (request.zoomLink() != null) {
            resource.setZoomLink(blankToNull(request.zoomLink()));
            resource.setZoomLinkUpdatedAt(now);
        }
        if (request.recordingLink() != null) {
            resource.setRecordingLink(blankToNull(request.recordingLink()));
            resource.setRecordingLinkUpdatedAt(now);
        }
        if (request.summaryLink() != null) {
            resource.setSummaryLink(blankToNull(request.summaryLink()));
            resource.setSummaryLinkUpdatedAt(now);
        }
        if (request.additionalNotice() != null) {
            resource.setAdditionalNotice(blankToNull(request.additionalNotice()));
        }
        resource.setLastUpdatedBy(instructor);

        SessionResource savedResource = sessionResourceRepository.save(resource);
        notifyStudentsIfNeeded(session, beforeZoomLink, beforeRecordingLink, beforeSummaryLink, savedResource);
        return CourseSessionResponse.from(session, SessionResourceResponse.from(savedResource));
    }

    private CourseSessionResponse toSessionResponse(CourseSession session) {
        SessionResourceResponse resource = sessionResourceRepository.findByCourseSession(session)
                .map(SessionResourceResponse::from)
                .orElseGet(() -> SessionResourceResponse.from(null));
        return CourseSessionResponse.from(session, resource);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private void notifyStudentsIfNeeded(
            CourseSession session,
            String beforeZoomLink,
            String beforeRecordingLink,
            String beforeSummaryLink,
            SessionResource savedResource
    ) {
        List<User> recipients = enrollmentRepository.findAllByCourseOrderByCreatedAtAsc(session.getCourse()).stream()
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ENROLLED || enrollment.getStatus() == EnrollmentStatus.COMPLETED)
                .map(Enrollment::getStudent)
                .toList();

        if (recipients.isEmpty()) {
            return;
        }

        boolean zoomChanged = hasChanged(beforeZoomLink, savedResource.getZoomLink());
        boolean recordingChanged = hasChanged(beforeRecordingLink, savedResource.getRecordingLink());
        boolean summaryChanged = hasChanged(beforeSummaryLink, savedResource.getSummaryLink());

        for (User recipient : recipients) {
            if (zoomChanged && savedResource.getZoomLink() != null) {
                notificationService.createSessionZoomUpdatedNotification(recipient, session);
            }
            if (recordingChanged && savedResource.getRecordingLink() != null) {
                notificationService.createSessionRecordingUpdatedNotification(recipient, session);
            }
            if (summaryChanged && savedResource.getSummaryLink() != null) {
                notificationService.createSessionSummaryUpdatedNotification(recipient, session);
            }
        }
    }

    private boolean hasChanged(String before, String after) {
        return before == null ? after != null : !before.equals(after);
    }
}
