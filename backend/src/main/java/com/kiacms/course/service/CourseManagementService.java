package com.kiacms.course.service;

import com.kiacms.course.dto.request.CreateCourseRequest;
import com.kiacms.course.dto.request.CreateCourseSessionRequest;
import com.kiacms.course.dto.request.EnrollStudentRequest;
import com.kiacms.course.dto.request.UpdateCourseSessionRequest;
import com.kiacms.course.dto.response.CourseResponse;
import com.kiacms.course.dto.response.CourseSessionResponse;
import com.kiacms.course.dto.response.EnrollmentResponse;
import com.kiacms.course.dto.response.InstructorOptionResponse;
import com.kiacms.course.dto.response.SessionResourceResponse;
import com.kiacms.course.dto.response.StudentCalendarResponse;
import com.kiacms.course.entity.Course;
import com.kiacms.course.entity.CourseSession;
import com.kiacms.course.entity.Enrollment;
import com.kiacms.course.enums.CourseStatus;
import com.kiacms.course.repository.SessionResourceRepository;
import com.kiacms.course.repository.CourseRepository;
import com.kiacms.course.repository.CourseSessionRepository;
import com.kiacms.course.repository.EnrollmentRepository;
import com.kiacms.global.exception.BusinessException;
import com.kiacms.global.exception.ConflictException;
import com.kiacms.global.exception.ErrorCode;
import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.user.entity.User;
import com.kiacms.user.enums.RoleType;
import com.kiacms.user.enums.UserStatus;
import com.kiacms.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseManagementService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    private final CourseRepository courseRepository;
    private final CourseSessionRepository courseSessionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SessionResourceRepository sessionResourceRepository;
    private final UserRepository userRepository;

    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request, User rootUser) {
        validateRoot(rootUser);
        validateCourseDateRange(request.startDate(), request.endDate());

        if (courseRepository.existsByCourseCode(request.courseCode())) {
            throw new ConflictException("Course code is already in use.");
        }

        Course course = Course.builder()
                .courseCode(request.courseCode())
                .title(request.title())
                .description(blankToNull(request.description()))
                .trackName(blankToNull(request.trackName()))
                .status(request.status() == null ? CourseStatus.PLANNED : request.status())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .maxCapacity(request.maxCapacity())
                .createdBy(rootUser)
                .build();

        return CourseResponse.from(courseRepository.save(course));
    }

    @Transactional
    public CourseSessionResponse createCourseSession(CreateCourseSessionRequest request, java.util.UUID courseId, User rootUser) {
        validateRoot(rootUser);
        validateSessionTimeRange(request);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found."));

        User instructor = getApprovedInstructor(request.instructorId());

        CourseSession session = CourseSession.builder()
                .course(course)
                .sessionOrder(request.sessionOrder())
                .title(request.title())
                .description(blankToNull(request.description()))
                .classroom(request.classroom().trim())
                .sessionDate(request.sessionDate())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .status(request.status() == null ? com.kiacms.course.enums.CourseSessionStatus.SCHEDULED : request.status())
                .instructor(instructor)
                .build();

        CourseSession savedSession = courseSessionRepository.save(session);
        return CourseSessionResponse.from(savedSession, SessionResourceResponse.from(null));
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getCourses(User rootUser) {
        validateRoot(rootUser);
        return courseRepository.findAll(Sort.by(Sort.Direction.ASC, "startDate")).stream()
                .map(CourseResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InstructorOptionResponse> getApprovedInstructors(User rootUser) {
        validateRoot(rootUser);
        return userRepository.findAllByRoleTypeAndStatusAndDeletedAtIsNullOrderByNameAsc(RoleType.INSTRUCTOR, UserStatus.APPROVED)
                .stream()
                .map(InstructorOptionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentCalendarResponse getSessionCalendar(User rootUser, LocalDate from, LocalDate to) {
        validateRoot(rootUser);
        List<com.kiacms.course.dto.response.CalendarEventResponse> events = courseSessionRepository
                .findAllBySessionDateBetweenOrderBySessionDateAscStartTimeAsc(from, to)
                .stream()
                .map(session -> {
                    SessionResourceResponse resource = sessionResourceRepository.findByCourseSession(session)
                            .map(SessionResourceResponse::from)
                            .orElseGet(() -> SessionResourceResponse.from(null));
                    return com.kiacms.course.dto.response.CalendarEventResponse.from(session, resource, DEFAULT_ZONE);
                })
                .toList();
        return new StudentCalendarResponse(DEFAULT_ZONE.getId(), events);
    }

    @Transactional
    public CourseSessionResponse updateCourseSession(UpdateCourseSessionRequest request, java.util.UUID sessionId, User rootUser) {
        validateRoot(rootUser);
        validateSessionTimeRange(request.startTime(), request.endTime());

        CourseSession session = courseSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Course session not found."));

        User instructor = getApprovedInstructor(request.instructorId());

        session.setSessionOrder(request.sessionOrder());
        session.setTitle(request.title().trim());
        session.setDescription(blankToNull(request.description()));
        session.setClassroom(request.classroom().trim());
        session.setSessionDate(request.sessionDate());
        session.setStartTime(request.startTime());
        session.setEndTime(request.endTime());
        session.setInstructor(instructor);
        session.setStatus(request.status() == null ? session.getStatus() : request.status());

        SessionResourceResponse resource = sessionResourceRepository.findByCourseSession(session)
                .map(SessionResourceResponse::from)
                .orElseGet(() -> SessionResourceResponse.from(null));
        return CourseSessionResponse.from(session, resource);
    }

    @Transactional
    public void deleteCourseSession(java.util.UUID sessionId, User rootUser) {
        validateRoot(rootUser);
        CourseSession session = courseSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Course session not found."));
        sessionResourceRepository.findByCourseSession(session).ifPresent(sessionResourceRepository::delete);
        courseSessionRepository.delete(session);
    }

    @Transactional
    public EnrollmentResponse enrollStudent(java.util.UUID courseId, EnrollStudentRequest request, User rootUser) {
        validateRoot(rootUser);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found."));

        User student = userRepository.findByIdAndDeletedAtIsNull(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found."));

        if (student.getRoleType() != RoleType.STUDENT || student.getStatus() != UserStatus.APPROVED) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "The selected user is not an approved student.");
        }

        if (enrollmentRepository.findByStudentAndCourse(student, course).isPresent()) {
            throw new ConflictException("The student is already enrolled in this course.");
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .enrolledBy(rootUser)
                .build();

        return EnrollmentResponse.from(enrollmentRepository.save(enrollment));
    }

    private void validateRoot(User user) {
        if (user.getRoleType() != RoleType.ROOT) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Only root administrators can manage courses.");
        }
    }

    private void validateCourseDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Course end date must be on or after the start date.");
        }
    }

    private void validateSessionTimeRange(CreateCourseSessionRequest request) {
        validateSessionTimeRange(request.startTime(), request.endTime());
    }

    private void validateSessionTimeRange(java.time.LocalTime startTime, java.time.LocalTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Session end time must be after the start time.");
        }
    }

    private User getApprovedInstructor(java.util.UUID instructorId) {
        User instructor = userRepository.findByIdAndDeletedAtIsNull(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found."));

        if (instructor.getRoleType() != RoleType.INSTRUCTOR || instructor.getStatus() != UserStatus.APPROVED) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "The selected user is not an approved instructor.");
        }
        return instructor;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
