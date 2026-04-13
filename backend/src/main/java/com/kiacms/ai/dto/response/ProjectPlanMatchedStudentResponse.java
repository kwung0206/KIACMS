package com.kiacms.ai.dto.response;

import java.util.UUID;

public record ProjectPlanMatchedStudentResponse(
        UUID studentId,
        String studentName,
        String matchedCourseTitle
) {
}
