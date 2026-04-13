package com.kiacms.mentor.dto.response;

import com.kiacms.course.entity.Enrollment;
import com.kiacms.course.enums.EnrollmentStatus;
import java.time.LocalDate;
import java.util.UUID;

public record ManagedStudentCourseResponse(
        UUID enrollmentId,
        UUID courseId,
        String courseCode,
        String courseTitle,
        EnrollmentStatus status,
        LocalDate startDate,
        LocalDate endDate
) {
    public static ManagedStudentCourseResponse from(Enrollment enrollment) {
        return new ManagedStudentCourseResponse(
                enrollment.getId(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getCourseCode(),
                enrollment.getCourse().getTitle(),
                enrollment.getStatus(),
                enrollment.getCourse().getStartDate(),
                enrollment.getCourse().getEndDate()
        );
    }
}
