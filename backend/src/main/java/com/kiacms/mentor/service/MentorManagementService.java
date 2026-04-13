package com.kiacms.mentor.service;

import com.kiacms.course.dto.response.CourseResponse;
import com.kiacms.course.dto.response.EnrollmentResponse;
import com.kiacms.course.entity.Course;
import com.kiacms.course.entity.Enrollment;
import com.kiacms.course.enums.EnrollmentStatus;
import com.kiacms.course.repository.CourseRepository;
import com.kiacms.course.repository.EnrollmentRepository;
import com.kiacms.global.exception.AccessDeniedBusinessException;
import com.kiacms.global.exception.ConflictException;
import com.kiacms.global.exception.ErrorCode;
import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.mentor.dto.request.AssignManagedStudentRequest;
import com.kiacms.mentor.dto.response.ManagedStudentCourseResponse;
import com.kiacms.mentor.dto.response.MentorManagedStudentResponse;
import com.kiacms.mentor.dto.response.MentorStudentOptionResponse;
import com.kiacms.mentor.entity.MentorStudentMapping;
import com.kiacms.mentor.enums.MentorStudentMappingStatus;
import com.kiacms.mentor.repository.MentorStudentMappingRepository;
import com.kiacms.user.entity.User;
import com.kiacms.user.enums.RoleType;
import com.kiacms.user.enums.UserStatus;
import com.kiacms.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MentorManagementService {

    private final MentorStudentMappingRepository mentorStudentMappingRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional(readOnly = true)
    public List<MentorStudentOptionResponse> searchStudents(User mentor, String keyword) {
        validateMentor(mentor);

        List<User> students = keyword == null || keyword.isBlank()
                ? userRepository.findAllByRoleTypeAndStatusAndDeletedAtIsNullOrderByNameAsc(RoleType.STUDENT, UserStatus.APPROVED)
                : userRepository.searchUsersByRoleAndStatus(RoleType.STUDENT, UserStatus.APPROVED, keyword.trim());

        return students.stream()
                .map(MentorStudentOptionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MentorManagedStudentResponse> getManagedStudents(User mentor) {
        validateMentor(mentor);
        return mentorStudentMappingRepository
                .findAllByMentorAndStatusOrderByStartDateDesc(mentor, MentorStudentMappingStatus.ACTIVE)
                .stream()
                .map(mapping -> MentorManagedStudentResponse.from(
                        mapping,
                        enrollmentRepository.findAllByStudentOrderByCreatedAtDesc(mapping.getStudent()).stream()
                                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ENROLLED
                                        || enrollment.getStatus() == EnrollmentStatus.COMPLETED)
                                .map(ManagedStudentCourseResponse::from)
                                .toList()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAvailableCourses(User mentor) {
        validateMentor(mentor);
        return courseRepository.findAll(Sort.by(Sort.Direction.ASC, "startDate")).stream()
                .map(CourseResponse::from)
                .toList();
    }

    @Transactional
    public MentorManagedStudentResponse assignStudent(User mentor, AssignManagedStudentRequest request) {
        validateMentor(mentor);
        User student = getApprovedStudent(request.studentId());

        mentorStudentMappingRepository.findByMentorAndStudentAndStatus(mentor, student, MentorStudentMappingStatus.ACTIVE)
                .ifPresent(existing -> {
                    throw new ConflictException("This student is already assigned to you.");
                });

        MentorStudentMapping mapping = mentorStudentMappingRepository.save(
                MentorStudentMapping.builder()
                        .mentor(mentor)
                        .student(student)
                        .assignedBy(mentor)
                        .status(MentorStudentMappingStatus.ACTIVE)
                        .startDate(request.startDate() == null ? LocalDate.now() : request.startDate())
                        .memo(blankToNull(request.memo()))
                        .build()
        );

        return MentorManagedStudentResponse.from(mapping, List.of());
    }

    @Transactional
    public MentorManagedStudentResponse endStudentMapping(User mentor, UUID mappingId) {
        validateMentor(mentor);
        MentorStudentMapping mapping = getManagedMapping(mentor, mappingId);
        mapping.setStatus(MentorStudentMappingStatus.ENDED);
        mapping.setEndDate(LocalDate.now());
        return MentorManagedStudentResponse.from(
                mapping,
                enrollmentRepository.findAllByStudentOrderByCreatedAtDesc(mapping.getStudent()).stream()
                        .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ENROLLED
                                || enrollment.getStatus() == EnrollmentStatus.COMPLETED)
                        .map(ManagedStudentCourseResponse::from)
                        .toList()
        );
    }

    @Transactional
    public EnrollmentResponse assignCourseToManagedStudent(User mentor, UUID mappingId, UUID courseId) {
        validateMentor(mentor);
        MentorStudentMapping mapping = getManagedMapping(mentor, mappingId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found."));

        enrollmentRepository.findByStudentAndCourse(mapping.getStudent(), course)
                .ifPresent(existing -> {
                    if (existing.getStatus() != EnrollmentStatus.DROPPED) {
                        throw new ConflictException("The student is already mapped to this course.");
                    }
                    existing.setStatus(EnrollmentStatus.ENROLLED);
                    existing.setEnrolledBy(mentor);
                });

        Enrollment existingEnrollment = enrollmentRepository.findByStudentAndCourse(mapping.getStudent(), course).orElse(null);
        if (existingEnrollment != null) {
            return EnrollmentResponse.from(existingEnrollment);
        }

        Enrollment enrollment = enrollmentRepository.save(
                Enrollment.builder()
                        .student(mapping.getStudent())
                        .course(course)
                        .status(EnrollmentStatus.ENROLLED)
                        .enrolledBy(mentor)
                        .build()
        );
        return EnrollmentResponse.from(enrollment);
    }

    @Transactional
    public EnrollmentResponse dropCourseFromManagedStudent(User mentor, UUID mappingId, UUID courseId) {
        validateMentor(mentor);
        MentorStudentMapping mapping = getManagedMapping(mentor, mappingId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found."));

        Enrollment enrollment = enrollmentRepository.findByStudentAndCourse(mapping.getStudent(), course)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found."));

        enrollment.setStatus(EnrollmentStatus.DROPPED);
        return EnrollmentResponse.from(enrollment);
    }

    private MentorStudentMapping getManagedMapping(User mentor, UUID mappingId) {
        MentorStudentMapping mapping = mentorStudentMappingRepository.findByIdAndMentor(mappingId, mentor)
                .orElseThrow(() -> new ResourceNotFoundException("Managed student mapping not found."));
        if (mapping.getStatus() != MentorStudentMappingStatus.ACTIVE) {
            throw new AccessDeniedBusinessException("Only active student mappings can be managed.");
        }
        return mapping;
    }

    private User getApprovedStudent(UUID studentId) {
        User student = userRepository.findByIdAndDeletedAtIsNull(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found."));
        if (student.getRoleType() != RoleType.STUDENT || student.getStatus() != UserStatus.APPROVED) {
            throw new AccessDeniedBusinessException("Only approved students can be assigned.");
        }
        return student;
    }

    private void validateMentor(User mentor) {
        if (mentor.getRoleType() != RoleType.MENTOR) {
            throw new AccessDeniedBusinessException("Only mentor managers can access this resource.");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
