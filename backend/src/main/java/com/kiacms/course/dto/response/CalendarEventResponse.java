package com.kiacms.course.dto.response;

import com.kiacms.course.entity.CourseSession;
import java.time.ZoneId;
import java.util.UUID;

public record CalendarEventResponse(
        UUID id,
        String title,
        String start,
        String end,
        UUID courseId,
        String classroom,
        String courseTitle,
        String courseCode,
        Integer sessionOrder,
        String sessionTitle,
        UUID instructorId,
        String instructorName,
        String status,
        String zoomLink,
        String recordingLink,
        String summaryLink,
        String detailPath
) {
    public static CalendarEventResponse from(CourseSession session, SessionResourceResponse resource, ZoneId zoneId) {
        return new CalendarEventResponse(
                session.getId(),
                session.getCourse().getTitle() + " - " + session.getTitle(),
                session.getSessionDate().atTime(session.getStartTime()).atZone(zoneId).toOffsetDateTime().toString(),
                session.getSessionDate().atTime(session.getEndTime()).atZone(zoneId).toOffsetDateTime().toString(),
                session.getCourse().getId(),
                session.getClassroom(),
                session.getCourse().getTitle(),
                session.getCourse().getCourseCode(),
                session.getSessionOrder(),
                session.getTitle(),
                session.getInstructor().getId(),
                session.getInstructor().getName(),
                session.getStatus().name(),
                resource.zoomLink(),
                resource.recordingLink(),
                resource.summaryLink(),
                "/sessions/" + session.getId()
        );
    }
}
