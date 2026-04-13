package com.kiacms.note.dto.response;

import com.kiacms.note.entity.NoteTag;
import java.time.Instant;
import java.util.UUID;

public record TaggedNoteSummaryResponse(
        UUID noteId,
        String noteTitle,
        UUID authorId,
        String authorName,
        UUID courseId,
        String courseCode,
        String courseTitle,
        UUID courseSessionId,
        Integer sessionOrder,
        String sessionTitle,
        Instant taggedAt,
        Instant lastUpdatedAt,
        long commentCount
) {
    public static TaggedNoteSummaryResponse from(NoteTag noteTag, long commentCount) {
        return new TaggedNoteSummaryResponse(
                noteTag.getNote().getId(),
                noteTag.getNote().getTitle(),
                noteTag.getNote().getAuthor().getId(),
                noteTag.getNote().getAuthor().getName(),
                noteTag.getNote().getCourse().getId(),
                noteTag.getNote().getCourse().getCourseCode(),
                noteTag.getNote().getCourse().getTitle(),
                noteTag.getNote().getCourseSession() == null ? null : noteTag.getNote().getCourseSession().getId(),
                noteTag.getNote().getCourseSession() == null ? null : noteTag.getNote().getCourseSession().getSessionOrder(),
                noteTag.getNote().getCourseSession() == null ? null : noteTag.getNote().getCourseSession().getTitle(),
                noteTag.getCreatedAt(),
                noteTag.getNote().getUpdatedAt(),
                commentCount
        );
    }
}
