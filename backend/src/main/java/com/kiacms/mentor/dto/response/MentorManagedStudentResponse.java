package com.kiacms.mentor.dto.response;

import com.kiacms.mentor.entity.MentorStudentMapping;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record MentorManagedStudentResponse(
        UUID mappingId,
        UUID studentId,
        String studentName,
        String studentEmail,
        String phoneNumber,
        LocalDate startDate,
        LocalDate endDate,
        String memo,
        List<ManagedStudentCourseResponse> enrolledCourses
) {
    public static MentorManagedStudentResponse from(
            MentorStudentMapping mapping,
            List<ManagedStudentCourseResponse> enrolledCourses
    ) {
        return new MentorManagedStudentResponse(
                mapping.getId(),
                mapping.getStudent().getId(),
                mapping.getStudent().getName(),
                mapping.getStudent().getEmail(),
                mapping.getStudent().getPhoneNumber(),
                mapping.getStartDate(),
                mapping.getEndDate(),
                mapping.getMemo(),
                enrolledCourses
        );
    }
}
