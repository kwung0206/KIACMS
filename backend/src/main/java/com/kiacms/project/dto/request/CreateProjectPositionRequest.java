package com.kiacms.project.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectPositionRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @Size(max = 2000)
        String description,

        @Size(max = 2000)
        String requiredSkills,

        @Min(1)
        Integer capacity
) {
}
