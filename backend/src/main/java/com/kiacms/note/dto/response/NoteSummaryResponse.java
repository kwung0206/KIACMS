package com.kiacms.note.dto.response;

import com.kiacms.note.entity.Note;
import java.time.Instant;
import java.util.UUID;

public record NoteSummaryResponse(
        UUID id,
        String title,
        UUID courseId,
        String courseCode,
        String courseTitle,
        UUID courseSessionId,
        Integer sessionOrder,
        String sessionTitle,
        Instant createdAt,
        Instant updatedAt,
        Instant lastTaggedAt,
        long tagCount,
        long commentCount
) {
    public static NoteSummaryResponse from(Note note, long tagCount, long commentCount) {
        return new NoteSummaryResponse(
                note.getId(),
                note.getTitle(),
                note.getCourse().getId(),
                note.getCourse().getCourseCode(),
                note.getCourse().getTitle(),
                note.getCourseSession() == null ? null : note.getCourseSession().getId(),
                note.getCourseSession() == null ? null : note.getCourseSession().getSessionOrder(),
                note.getCourseSession() == null ? null : note.getCourseSession().getTitle(),
                note.getCreatedAt(),
                note.getUpdatedAt(),
                note.getLastTaggedAt(),
                tagCount,
                commentCount
        );
    }
}
