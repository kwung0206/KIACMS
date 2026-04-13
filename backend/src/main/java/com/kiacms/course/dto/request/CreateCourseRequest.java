package com.kiacms.course.dto.request;

import com.kiacms.course.enums.CourseStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateCourseRequest(
        @NotBlank
        @Size(max = 50)
        String courseCode,

        @NotBlank
        @Size(max = 150)
        String title,

        @Size(max = 5000)
        String description,

        @Size(max = 100)
        String trackName,

        @NotNull
        @FutureOrPresent
        LocalDate startDate,

        @NotNull
        LocalDate endDate,

        Integer maxCapacity,

        CourseStatus status
) {
}
