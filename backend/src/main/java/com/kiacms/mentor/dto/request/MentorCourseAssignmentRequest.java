package com.kiacms.mentor.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MentorCourseAssignmentRequest(
        @NotNull
        UUID courseId
) {
}
