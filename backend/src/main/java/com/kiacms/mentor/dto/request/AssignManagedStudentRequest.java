package com.kiacms.mentor.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record AssignManagedStudentRequest(
        @NotNull
        UUID studentId,

        LocalDate startDate,

        @Size(max = 1000)
        String memo
) {
}
