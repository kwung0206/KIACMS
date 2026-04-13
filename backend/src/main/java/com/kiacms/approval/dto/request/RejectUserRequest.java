package com.kiacms.approval.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectUserRequest(
        @NotBlank
        @Size(max = 2000)
        String reason
) {
}
