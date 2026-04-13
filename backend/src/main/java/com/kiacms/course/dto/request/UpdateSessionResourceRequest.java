package com.kiacms.course.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateSessionResourceRequest(
        @Size(max = 500)
        String zoomLink,

        @Size(max = 500)
        String recordingLink,

        @Size(max = 500)
        String summaryLink,

        @Size(max = 5000)
        String additionalNotice
) {
}
