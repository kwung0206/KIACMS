package com.kiacms.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmitProjectApplicationRequest(
        @NotBlank
        @Size(max = 5000)
        String motivation,

        @Size(max = 5000)
        String courseHistory,

        @Size(max = 3000)
        String certifications,

        @NotBlank
        @Size(max = 3000)
        String techStack,

        @Size(max = 500)
        String portfolioUrl,

        @NotBlank
        @Size(max = 5000)
        String selfIntroduction
) {
}
