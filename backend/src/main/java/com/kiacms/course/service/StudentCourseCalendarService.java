package com.kiacms.course.service;

import com.kiacms.course.dto.response.CalendarEventResponse;
import com.kiacms.course.dto.response.CourseSessionResponse;
import com.kiacms.course.dto.response.SessionResourceResponse;
import com.kiacms.course.dto.response.StudentCalendarResponse;
import com.kiacms.course.entity.Course;
import com.kiacms.course.entity.CourseSession;
import com.kiacms.course.enums.EnrollmentStatus;
import com.kiacms.course.repository.CourseSessionRepository;
import com.kiacms.course.repository.EnrollmentRepository;
import com.kiacms.course.repository.SessionResourceRepository;
import com.kiacms.global.exception.AccessDeniedBusinessException;
import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.user.entity.User;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentCourseCalendarService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    private final EnrollmentRepository enrollmentRepository;
    private final CourseSessionRepository courseSessionRepository;
    private final SessionResourceRepository sessionResourceRepository;

    @Transactional(readOnly = true)
    public StudentCalendarResponse getCalendar(User student, LocalDate from, LocalDate to) {
        List<Course> courses = enrollmentRepository.findAllByStudentOrderByCreatedAtDesc(student).stream()
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ENROLLED || enrollment.getStatus() == EnrollmentStatus.COMPLETED)
                .map(enrollment -> enrollment.getCourse())
                .toList();

        if (courses.isEmpty()) {
            return new StudentCalendarResponse(DEFAULT_ZONE.getId(), List.of());
        }

        List<CourseSession> sessions = courseSessionRepository.findAllByCourseInOrderBySessionDateAscStartTimeAsc(courses).stream()
                .filter(session -> !session.getSessionDate().isBefore(from) && !session.getSessionDate().isAfter(to))
                .toList();

        List<CalendarEventResponse> events = sessions.stream()
                .map(session -> {
                    SessionResourceResponse resource = sessionResourceRepository.findByCourseSession(session)
                            .map(SessionResourceResponse::from)
                            .orElseGet(() -> SessionResourceResponse.from(null));
                    return CalendarEventResponse.from(session, resource, DEFAULT_ZONE);
                })
                .toList();

        return new StudentCalendarResponse(DEFAULT_ZONE.getId(), events);
    }

    @Transactional(readOnly = true)
    public CourseSessionResponse getSessionDetail(User student, UUID sessionId) {
        CourseSession session = courseSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found."));

        Set<UUID> enrolledCourseIds = enrollmentRepository.findAllByStudentOrderByCreatedAtDesc(student).stream()
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ENROLLED || enrollment.getStatus() == EnrollmentStatus.COMPLETED)
                .map(enrollment -> enrollment.getCourse().getId())
                .collect(Collectors.toSet());

        if (!enrolledCourseIds.contains(session.getCourse().getId())) {
            throw new AccessDeniedBusinessException("You are not enrolled in the course for this session.");
        }

        SessionResourceResponse resource = sessionResourceRepository.findByCourseSession(session)
                .map(SessionResourceResponse::from)
                .orElseGet(() -> SessionResourceResponse.from(null));

        return CourseSessionResponse.from(session, resource);
    }
}
