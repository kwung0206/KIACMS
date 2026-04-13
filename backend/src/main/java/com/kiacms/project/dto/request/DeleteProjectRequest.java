package com.kiacms.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeleteProjectRequest(
        @NotBlank
        @Size(max = 2000)
        String reason
) {
}
