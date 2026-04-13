package com.kiacms.note.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateNoteCommentRequest(
        @NotBlank
        @Size(max = 5000)
        String content
) {
}
