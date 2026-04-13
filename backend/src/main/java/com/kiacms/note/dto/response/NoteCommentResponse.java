package com.kiacms.note.dto.response;

import com.kiacms.note.entity.NoteComment;
import com.kiacms.user.enums.RoleType;
import java.time.Instant;
import java.util.UUID;

public record NoteCommentResponse(
        UUID id,
        UUID authorId,
        String authorName,
        RoleType authorRole,
        String content,
        Instant createdAt,
        Instant editedAt
) {
    public static NoteCommentResponse from(NoteComment noteComment) {
        return new NoteCommentResponse(
                noteComment.getId(),
                noteComment.getAuthor().getId(),
                noteComment.getAuthor().getName(),
                noteComment.getAuthor().getRoleType(),
                noteComment.getContent(),
                noteComment.getCreatedAt(),
                noteComment.getEditedAt()
        );
    }
}
