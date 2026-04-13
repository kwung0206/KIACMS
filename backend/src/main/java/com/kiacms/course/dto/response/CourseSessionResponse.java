package com.kiacms.course.dto.response;

import com.kiacms.course.entity.CourseSession;
import com.kiacms.course.enums.CourseSessionStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CourseSessionResponse(
        UUID id,
        UUID courseId,
        String courseCode,
        String courseTitle,
        Integer sessionOrder,
        String title,
        String description,
        String classroom,
        LocalDate sessionDate,
        LocalTime startTime,
        LocalTime endTime,
        CourseSessionStatus status,
        UUID instructorId,
        String instructorName,
        SessionResourceResponse resource
) {
    public static CourseSessionResponse from(CourseSession session, SessionResourceResponse resource) {
        return new CourseSessionResponse(
                session.getId(),
                session.getCourse().getId(),
                session.getCourse().getCourseCode(),
                session.getCourse().getTitle(),
                session.getSessionOrder(),
                session.getTitle(),
                session.getDescription(),
                session.getClassroom(),
                session.getSessionDate(),
                session.getStartTime(),
                session.getEndTime(),
                session.getStatus(),
                session.getInstructor().getId(),
                session.getInstructor().getName(),
                resource
        );
    }
}
