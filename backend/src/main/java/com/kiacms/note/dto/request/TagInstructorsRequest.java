package com.kiacms.note.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record TagInstructorsRequest(
        @NotEmpty
        @Size(max = 10)
        List<@NotNull UUID> instructorIds
) {
}
