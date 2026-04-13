package com.kiacms.course.dto.response;

import com.kiacms.course.entity.Enrollment;
import com.kiacms.course.enums.EnrollmentStatus;
import java.time.Instant;
import java.util.UUID;

public record EnrollmentResponse(
        UUID id,
        UUID studentId,
        String studentName,
        UUID courseId,
        String courseCode,
        String courseTitle,
        EnrollmentStatus status,
        Instant enrolledAt
) {
    public static EnrollmentResponse from(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getName(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getCourseCode(),
                enrollment.getCourse().getTitle(),
                enrollment.getStatus(),
                enrollment.getCreatedAt()
        );
    }
}
