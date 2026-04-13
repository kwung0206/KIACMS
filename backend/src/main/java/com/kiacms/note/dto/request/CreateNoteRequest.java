package com.kiacms.note.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record CreateNoteRequest(
        @NotBlank
        @Size(max = 200)
        String title,

        @NotBlank
        @Size(max = 20000)
        String content,

        @NotNull
        UUID courseId,

        UUID courseSessionId,

        @Size(max = 10)
        List<@NotNull UUID> taggedInstructorIds
) {
}
