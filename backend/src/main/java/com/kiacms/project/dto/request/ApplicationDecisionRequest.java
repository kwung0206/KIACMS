package com.kiacms.project.dto.request;

import com.kiacms.project.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplicationDecisionRequest(
        @NotNull
        ApplicationStatus status,

        @Size(max = 5000)
        String rejectionReason
) {
}
