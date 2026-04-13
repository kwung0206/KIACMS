package com.kiacms.note.dto.response;

import com.kiacms.note.entity.NoteTag;
import java.time.Instant;
import java.util.UUID;

public record NoteTagResponse(
        UUID id,
        UUID instructorId,
        String instructorName,
        UUID taggedById,
        String taggedByName,
        Instant taggedAt
) {
    public static NoteTagResponse from(NoteTag noteTag) {
        return new NoteTagResponse(
                noteTag.getId(),
                noteTag.getTaggedInstructor().getId(),
                noteTag.getTaggedInstructor().getName(),
                noteTag.getTaggedBy().getId(),
                noteTag.getTaggedBy().getName(),
                noteTag.getCreatedAt()
        );
    }
}
