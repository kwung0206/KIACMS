package com.kiacms.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmitMentorApplicationRequest(
        @NotBlank
        @Size(max = 5000)
        String expertiseSummary,

        @Size(max = 5000)
        String mentoringExperience,

        @Size(max = 500)
        String portfolioUrl,

        @NotBlank
        @Size(max = 5000)
        String supportPlan
) {
}
