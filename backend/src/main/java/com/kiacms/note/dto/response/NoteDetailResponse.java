package com.kiacms.note.dto.response;

import com.kiacms.note.entity.Note;
import com.kiacms.note.entity.NoteComment;
import com.kiacms.note.entity.NoteTag;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record NoteDetailResponse(
        UUID id,
        String title,
        String content,
        UUID authorId,
        String authorName,
        UUID courseId,
        String courseCode,
        String courseTitle,
        UUID courseSessionId,
        Integer sessionOrder,
        String sessionTitle,
        UUID sessionInstructorId,
        String sessionInstructorName,
        Instant createdAt,
        Instant updatedAt,
        Instant lastTaggedAt,
        List<NoteTagResponse> tags,
        List<NoteCommentResponse> comments
) {
    public static NoteDetailResponse from(Note note, List<NoteTag> tags, List<NoteComment> comments) {
        return new NoteDetailResponse(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getAuthor().getId(),
                note.getAuthor().getName(),
                note.getCourse().getId(),
                note.getCourse().getCourseCode(),
                note.getCourse().getTitle(),
                note.getCourseSession() == null ? null : note.getCourseSession().getId(),
                note.getCourseSession() == null ? null : note.getCourseSession().getSessionOrder(),
                note.getCourseSession() == null ? null : note.getCourseSession().getTitle(),
                note.getCourseSession() == null ? null : note.getCourseSession().getInstructor().getId(),
                note.getCourseSession() == null ? null : note.getCourseSession().getInstructor().getName(),
                note.getCreatedAt(),
                note.getUpdatedAt(),
                note.getLastTaggedAt(),
                tags.stream().map(NoteTagResponse::from).toList(),
                comments.stream().map(NoteCommentResponse::from).toList()
        );
    }
}
