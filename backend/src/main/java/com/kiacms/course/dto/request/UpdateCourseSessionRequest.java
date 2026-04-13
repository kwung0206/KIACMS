package com.kiacms.course.dto.request;

import com.kiacms.course.enums.CourseSessionStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record UpdateCourseSessionRequest(
        @Positive
        Integer sessionOrder,

        @NotBlank
        @Size(max = 150)
        String title,

        @Size(max = 5000)
        String description,

        @NotBlank
        @Size(max = 100)
        String classroom,

        @NotNull
        @FutureOrPresent
        LocalDate sessionDate,

        @NotNull
        LocalTime startTime,

        @NotNull
        LocalTime endTime,

        @NotNull
        UUID instructorId,

        CourseSessionStatus status
) {
}
