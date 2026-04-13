package com.kiacms.course.dto.response;

import com.kiacms.course.entity.Course;
import com.kiacms.course.enums.CourseStatus;
import java.time.LocalDate;
import java.util.UUID;

public record CourseResponse(
        UUID id,
        String courseCode,
        String title,
        String description,
        String trackName,
        CourseStatus status,
        LocalDate startDate,
        LocalDate endDate,
        Integer maxCapacity,
        UUID createdById
) {
    public static CourseResponse from(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getCourseCode(),
                course.getTitle(),
                course.getDescription(),
                course.getTrackName(),
                course.getStatus(),
                course.getStartDate(),
                course.getEndDate(),
                course.getMaxCapacity(),
                course.getCreatedBy().getId()
        );
    }
}
